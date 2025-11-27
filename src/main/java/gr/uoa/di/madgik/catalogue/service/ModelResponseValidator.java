package gr.uoa.di.madgik.catalogue.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uoa.di.madgik.catalogue.config.CatalogueLibProperties;
import gr.uoa.di.madgik.catalogue.exception.ValidationException;
import gr.uoa.di.madgik.catalogue.ui.domain.Model;
import gr.uoa.di.madgik.catalogue.ui.domain.Section;
import gr.uoa.di.madgik.catalogue.ui.domain.UiField;
import gr.uoa.di.madgik.registry.domain.FacetFilter;
import gr.uoa.di.madgik.registry.exception.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

import static gr.uoa.di.madgik.catalogue.ui.domain.FieldType.EMAIL;
import static gr.uoa.di.madgik.catalogue.ui.domain.FieldType.PHONENUMBER;

@Service
public class ModelResponseValidator {

    private static final Logger logger = LoggerFactory.getLogger(ModelResponseValidator.class);

    private final ModelService modelService;
    private final CatalogueLibProperties properties;
    private final ObjectMapper objectMapper;

    public ModelResponseValidator(ModelService modelService,
                                  CatalogueLibProperties properties,
                                  ObjectMapper objectMapper) {
        this.modelService = modelService;
        this.properties = properties;
        this.objectMapper = objectMapper;
        }


    /**
     * Validates a {@link T resource} based on its {@link Model}.
     *
     * @param resource         The {@link Object resource} to validate.
     * @param resourceTypeName The resourceType name to search for the resource's {@link Model}.
     * @return {@link T}
     */
    public <T> T validate(T resource, String resourceTypeName) {
        if (properties.getValidation().isEnabled()) {

            if (logger.isDebugEnabled()) {
                try {
                    logger.debug("Validating resource: {}", objectMapper.writeValueAsString(resource));
                } catch (JsonProcessingException ignore) {
                }
            }

            FacetFilter ff = new FacetFilter();
            ff.addFilter("resourceType", resourceTypeName);
            List<Model> models = modelService.browse(ff).getResults();
            if (models == null || models.isEmpty()) {
                logger.warn("Could not find model to validate resource : [resourceType={}]", resourceTypeName);
                return resource;
            } else if (models.size() != 1) {
                throw new ResourceException(String.format("Found more than one models : [resourceType=%s]", resourceTypeName), HttpStatus.CONFLICT);
            }
            Model model = models.getFirst();
            return validateSections(resource, model.getSections());
        }
        return resource;
    }

    /**
     * Validates the {@link Object obj} against a list of sections.
     *
     * @param obj      The {@link Object object} to validate.
     * @param sections {@link List}<{@link Section}>
     * @param <T>
     * @return {@link T}
     */
    private <T> T validateSections(T obj, List<Section> sections) {
        return objectMapper.convertValue(validateSections(objectMapper.convertValue(obj, LinkedHashMap.class), sections, null), new TypeReference<T>() {
        });
    }

    /**
     * Validates the {@link Object obj} against a list of sections, keeping track of the current path followed.
     *
     * @param obj      The {@link Object object} to validate.
     * @param sections {@link List}<{@link Section}>
     * @param path     keeps track of the path followed in the {@link Object obj} (for displaying more detailed errors).
     * @param <T>
     * @return {@link T}
     */
    private <T> T validateSections(T obj, List<Section> sections, Deque<String> path) {
        if (path == null) {
            path = new ArrayDeque<>();
        }
        for (Section section : sections) {
            path.push(section.getName());
            if (section.getSubSections() != null) {
                // if section is contained as a field in the data object
                if (obj instanceof LinkedHashMap<?, ?> data && data.get(section.getName()) != null) {
                    validateSections(data.get(section.getName()), section.getSubSections(), path);
                } else {
                    validateSections(obj, section.getSubSections(), path);
                }
            }
            if (section.getFields() != null) {
                validateFields(obj, section.getFields(), true, path);
            }
            path.pop();
        }
        return obj;
    }

    /**
     * Validates that a list of fields is present in an object.
     *
     * @param object    The {@link Object object} to validate.
     * @param fields    {@link List}<{@link UiField}>
     * @param mandatory Initial mandatory value.
     * @param path      keeps track of the path of every field for displaying errors.
     * @return if a field is empty
     */
    private boolean validateFields(Object object, List<UiField> fields, boolean mandatory, Deque<String> path) {
        boolean empty = true;
        if (object != null) {
            for (UiField field : fields) {
                path.push(field.getLabel().getText());
                if (field.getSubFields() != null && !field.getSubFields().isEmpty()) {
                    if (object instanceof List) {
                        for (Object ans : (List<?>) object) {
                            empty = validateFields(((LinkedHashMap<?, ?>) ans).get(field.getName()), field.getSubFields(), mandatory && field.getForm().getMandatory(), path);
                            removeCompositeFieldIfEmpty(field, ans, empty);
                        }
                    } else {
                        empty = validateFields(((LinkedHashMap<?, ?>) object).get(field.getName()), field.getSubFields(), mandatory && field.getForm().getMandatory(), path);
                    }
                    removeCompositeFieldIfEmpty(field, object, empty);
                }
                if (mandatory && Boolean.TRUE.equals(field.getForm().getMandatory())) {
                    checkMandatoryField(object, field, path);
                }

                checkFieldType(object, field, path);

                if (containsValue(object)) {
                    empty = false;
                }
                path.pop();
            }
        }
        return empty;
    }

    // Email regex pattern (RFC 5322 simplified)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Phone number pattern (supports various formats)
    // Matches: +30 123 456 7890, (123) 456-7890, 123-456-7890, 1234567890, etc.
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[(]?[0-9]{1,4}[)]?[-\\s\\.]?[(]?[0-9]{1,4}[)]?[-\\s\\.]?[0-9]{1,4}[-\\s\\.]?[0-9]{1,9}$"
    );

    void checkFieldType(Object object, UiField field, Deque<String> path) throws ValidationException {
        if (object == null || field == null) {
            throw new IllegalArgumentException("Object and field cannot be null");
        }

        try {
            // Get the field value from the object using reflection
            Field declaredField = object.getClass().getDeclaredField(field.getName());
            declaredField.setAccessible(true);
            Object value = declaredField.get(object);

            if (value == null) {
                throw new ValidationException("Field '" + field.getName() + "' cannot be null");
            }

            String stringValue = value.toString().trim();

            if (stringValue.isEmpty()) {
                throw new ValidationException("Field '" + field.getName() + "' cannot be empty");
            }


            // Validate based on field type
            switch (field.getTypeInfo().getType()) {
                case "email":
                    if (!EMAIL_PATTERN.matcher(stringValue).matches()) {
                        throw new ValidationException("Invalid email format for field '" + field.getName() + "'");
                    }
                    break;

                case "phonenumber":
                    if (!PHONE_PATTERN.matcher(stringValue).matches()) {
                        throw new ValidationException("Invalid phone number format for field '" + field.getName() + "'");
                    }
                    break;

                default:
                    throw new ValidationException("Unsupported field type: " + field.getTypeInfo().getType());
            }

        } catch (NoSuchFieldException e) {
            throw new ValidationException(String.format("Field '" + field.getName() + "' not found in object", e, prettyPrintPath(path)));
        } catch (IllegalAccessException e) {
            throw new ValidationException(String.format("Cannot access field '" + field.getName() + "'", e, prettyPrintPath(path)));
        }
    }

    /**
     * Replaces the value found in {@link Object object}[{@link UiField field}.getName()] with null.
     *
     * @param field  the field information
     * @param object the object to modify
     * @param empty  condition to remove field
     */
    void removeCompositeFieldIfEmpty(UiField field, Object object, boolean empty) {
        if ("composite".equals(field.getTypeInfo().getType()) && empty) {
            if (object instanceof LinkedHashMap) {
                ((LinkedHashMap) object).put(field.getName(), null);
            }
        }
    }

    /**
     * Checks if a specific field exists in {@link Object obj}.
     * In case {@link Object obj} is a {@link List}, the method checks if every list entry contains a value for this field.
     *
     * @param obj   the object to check
     * @param field information about the field
     * @param path  the complete access path to find the field, saved as a {@link Deque}
     * @throws ValidationException containing a detailed message for the missing field
     */
    private void checkMandatoryField(Object obj, UiField field, Deque<String> path) throws ValidationException {
        if (obj == null) {
            throw new ValidationException(String.format("Mandatory field '%s' is empty.", prettyPrintPath(path)));
        } else if (obj instanceof List) {
            if (((List<?>) obj).isEmpty()) {
                throw new ValidationException(String.format("Mandatory field '%s' is empty.", prettyPrintPath(path)));
            } else if (((List<?>) obj).stream().allMatch(item -> item == null || "".equals(item))) {
                throw new ValidationException(String.format("Mandatory field '%s' has only null entries.", prettyPrintPath(path)));
            } else {
                for (int i = 0; i < ((List<?>) obj).size(); i++) {
                    // check each list entry for the required field
                    if (((List<?>) obj).get(i) instanceof LinkedHashMap) {
                        LinkedHashMap<?, ?> entry = (LinkedHashMap<?, ?>) ((List<?>) obj).get(i);
                        if (entry.get(field.getName()) == null || "".equals(entry.get(field.getName()))) {
                            throw new ValidationException(String.format("Mandatory field '%s' is missing.", prettyPrintPath(path)));
                        }
                    }
                }
            }
        } else if (obj instanceof LinkedHashMap && (((LinkedHashMap<?, ?>) obj).get(field.getName()) == null || "".equals(((LinkedHashMap<?, ?>) obj).get(field.getName())))) {
            throw new ValidationException(String.format("Mandatory field '%s' is empty.", prettyPrintPath(path)));
        }
    }

    /**
     * Creates a prettified representation of an object's access path.
     * Transforms a {@link Deque} of entries to a path. (e.g. Deque [object, to, path] becomes 'path -> to -> object')
     *
     * @param path a {@link Deque} containing entries of each relative location (in reverse order)
     * @return {@link String}
     */
    private String prettyPrintPath(Deque<String> path) {
        StringBuilder pathBuilder = new StringBuilder();
        // iterate using descending iterator because elements in the deck are added using the push() method.
        for (Iterator<String> iter = path.descendingIterator(); iter.hasNext(); ) {
            pathBuilder.append(iter.next());
            if (iter.hasNext()) {
                pathBuilder.append(" -> ");
            }
        }
        return pathBuilder.toString();
    }

    /**
     * Checks whether an {@link Object obj} contains anything other than null value/values.
     *
     * @param obj the object to validate
     * @return {@link Boolean}
     */
    private boolean containsValue(Object obj) {
        boolean contains = false;
        if (obj instanceof LinkedHashMap) {
            for (Object key : ((LinkedHashMap<?, ?>) obj).keySet()) {
                if (((LinkedHashMap<?, ?>) obj).get(key) instanceof LinkedHashMap || ((LinkedHashMap<?, ?>) obj).get(key) instanceof List) {
                    contains = contains || containsValue(((LinkedHashMap<?, ?>) obj).get(key));
                } else if (((LinkedHashMap<?, ?>) obj).get(key) != null && !((LinkedHashMap<?, ?>) obj).get(key).equals("")) {
                    return true;
                }
            }
        } else if (obj instanceof List) {
            for (Object item : (List<?>) obj) {
                contains = containsValue(item);
                if (contains) {
                    break;
                }
            }
        }
        return contains;
    }

}
