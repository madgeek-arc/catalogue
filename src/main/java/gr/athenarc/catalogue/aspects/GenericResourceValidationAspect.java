package gr.athenarc.catalogue.aspects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openminted.registry.core.domain.FacetFilter;
import gr.athenarc.catalogue.config.CatalogueLibProperties;
import gr.athenarc.catalogue.exception.ResourceException;
import gr.athenarc.catalogue.exception.ValidationException;
import gr.athenarc.catalogue.ui.domain.Model;
import gr.athenarc.catalogue.ui.domain.Section;
import gr.athenarc.catalogue.ui.domain.UiField;
import gr.athenarc.catalogue.ui.service.ModelService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Aspect validating the given resources before adding/updating them in the catalogue.
 */
@Aspect
@Component
public class GenericResourceValidationAspect {

    private static final Logger logger = LoggerFactory.getLogger(GenericResourceValidationAspect.class);
    private final ModelService modelService;
    private final CatalogueLibProperties properties;


    GenericResourceValidationAspect(ModelService modelService, CatalogueLibProperties properties) {
        this.modelService = modelService;
        this.properties = properties;
    }

    @Before("execution(* gr.athenarc.catalogue.service.AbstractGenericItemService.add(..))")
    public void validateBeforeAdd(JoinPoint joinPoint) {
        if (properties.getValidation().isEnabled()) {
            String resourceTypeName = (String) joinPoint.getArgs()[0];
            Object item = joinPoint.getArgs()[1];
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                logger.debug("Validating resource: {}", objectMapper.writeValueAsString(item));
            } catch (JsonProcessingException ignore) {
            }
            validate(item, resourceTypeName);
        }
    }

    @Before("execution(* gr.athenarc.catalogue.service.AbstractGenericItemService.update(..))")
    public void validateBeforeUpdate(JoinPoint joinPoint) {
        if (properties.getValidation().isEnabled()) {
            String resourceTypeName = (String) joinPoint.getArgs()[0];
            Object item = joinPoint.getArgs()[2];
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                logger.debug("Validating resource: {}", objectMapper.writeValueAsString(item));
            } catch (JsonProcessingException ignore) {
            }
            validate(item, resourceTypeName);
        }
    }

    /**
     * Validates a {@link T resource} based on its {@link Model}.
     * @param resource The resource to validate.
     * @param resourceTypeName The resourceType name to search for the resource's {@link Model}.
     * @return {@link T}
     */
    private <T> T validate(T resource, String resourceTypeName) {
        FacetFilter ff = new FacetFilter();
        ff.addFilter("resourceType", resourceTypeName);
        List<Model> models = modelService.browse(ff).getResults();
        if (models == null || models.isEmpty()) {
            logger.warn("Could not find model to validate resource : [resourceType={}]", resourceTypeName);
            return resource;
        } else if (models.size() != 1) {
            throw new ResourceException(String.format("Found more than one models : [resourceType=%s]", resourceTypeName), HttpStatus.CONFLICT);
        }
        Model model = models.get(0);
        return validateSections(resource, model.getSections());
    }

    /**
     * Validates the {@link Object obj} against a list of sections.
     * @param obj
     * @param sections {@link List}<{@link Section}>
     * @return {@link T}
     * @param <T>
     */
    private <T> T validateSections(T obj, List<Section> sections) {
        return validateSections(obj, sections, null);
    }

    /**
     * Validates the {@link Object obj} against a list of sections, keeping track of the current path followed.
     * @param obj
     * @param sections {@link List}<{@link Section}>
     * @param path keeps track of the path followed in the {@link Object obj} (for displaying more detailed errors).
     * @return {@link T}
     * @param <T>
     */
    private <T> T validateSections(T obj, List<Section> sections, Deque<String> path) {
        if (path == null) {
            path = new ArrayDeque<>();
        }
        for (Section section : sections) {
            path.push(section.getName());
            if (section.getSubSections() != null) {
                validateSections(obj, section.getSubSections(), path);
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
     * @param object The {@link Object object} to validate
     * @param fields {@link List}<{@link UiField}>
     * @param mandatory Initial mandatory value.
     * @param path keeps track of the path of every field for displaying errors.
     * @return
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
                if (containsValue(object)) {
                    empty = false;
                }
                path.pop();
            }
        }
        return empty;
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
            if (((List) obj).isEmpty()) {
                throw new ValidationException(String.format("Mandatory field '%s' is empty.", prettyPrintPath(path)));
            } else if (((List) obj).stream().allMatch(item -> item == null || "".equals(item))) {
                throw new ValidationException(String.format("Mandatory field '%s' has only null entries.", prettyPrintPath(path)));
            } else {
                for (int i = 0; i < ((List<?>) obj).size(); i++) {
                    // check each list entry for the required field
                    if (((List) obj).get(i) instanceof LinkedHashMap) {
                        LinkedHashMap entry = (LinkedHashMap) ((List<?>) obj).get(i);
                        if (entry.get(field.getName()) == null || "".equals(entry.get(field.getName()))) {
                            throw new ValidationException(String.format("Mandatory field '%s' is missing.", prettyPrintPath(path)));
                        }
                    }
                }
            }
        } else if (obj instanceof LinkedHashMap && (((LinkedHashMap) obj).get(field.getName()) == null || "".equals(((LinkedHashMap) obj).get(field.getName())))) {
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
            for (Object key : ((LinkedHashMap) obj).keySet()) {
                if (((LinkedHashMap) obj).get(key) instanceof LinkedHashMap || ((LinkedHashMap) obj).get(key) instanceof List) {
                    contains = contains || containsValue(((LinkedHashMap) obj).get(key));
                } else if (((LinkedHashMap) obj).get(key) != null && !((LinkedHashMap) obj).get(key).equals("")) {
                    return true;
                }
            }
        } else if (obj instanceof List) {
            for (Object item : (List) obj) {
                contains = contains || containsValue(item);
                if (contains) {
                    break;
                }
            }
        }
        return contains;
    }
}
