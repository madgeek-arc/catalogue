/**
 * Copyright 2021-2025 OpenAIRE AMKE
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import java.util.*;

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
            validateResourceAgainstModel(resource, model.getSections());
            return validateSections(resource, model.getSections());
        }
        return resource;
    }

    /**
     * Validates the given {@link Object obj} against the model defined by the provided sections,
     * ensuring that no extra or unexpected fields are present.
     *
     * @param obj      The {@link Object object} to validate.
     * @param sections the {@link List}<{@link Section}> defining the allowed fields in the model
     * @param <T>      the type of the object being validated
     */
    private <T> void validateResourceAgainstModel(T obj, List<Section> sections) {
        objectMapper.convertValue(resourceToModelValidation(
                objectMapper.convertValue(obj, LinkedHashMap.class), sections), new TypeReference<T>() {});
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
        return objectMapper.convertValue(validateSections(
                objectMapper.convertValue(obj, LinkedHashMap.class), sections, null), new TypeReference<T>() {});
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

    /**
     * Converts the given resource object into a structure suitable for validation
     * against the model defined by the provided sections.
     *
     * @param obj      the resource object to validate
     * @param sections the list of {@link Section}s defining the allowed model structure
     * @param <T>      the type of the resource object
     * @return a {@link LinkedHashMap} representation of the resource for validation
     * @throws ValidationException if extra fields are found in the resource object
     */
    private <T> LinkedHashMap resourceToModelValidation(T obj, List<Section> sections) {
        Map<String, Object> modelFields = new HashMap<>();
        for (Section section : sections) {
            for (Section subSection : section.getSubSections()) {
                modelFields.putAll(buildModelFieldTree(subSection.getFields()));
            }
        }

        String resourceTypeName = sections.getFirst().getName(); //FIXME: works correctly only for forms with 1 section
        if (obj instanceof LinkedHashMap<?, ?> mapObj) {
            boolean found = false;
            for (Object key : mapObj.keySet()) {
                if (key.equals(resourceTypeName)) {
                    found = true;
                    Object innerFields = mapObj.get(key);
                    Map<String, Object> resourceTree = buildResourceFieldTree(innerFields);
                    validateTrees(modelFields, resourceTree, resourceTypeName + ".");
                }
            }
            if (!found) {
                logger.warn("Could not find model to validate resource : [resourceType={}]", resourceTypeName);
            }
        }
        return null;
    }

    /**
     * Recursively builds a tree representation of the model fields.
     *
     * @param fields the list of {@link UiField}s to convert into a tree structure
     * @return a {@link Map} representing the hierarchical structure of the model fields
     */
    private Map<String, Object> buildModelFieldTree(List<UiField> fields) {
        Map<String, Object> result = new HashMap<>();
        for (UiField field : fields) {
            if (field.getSubFields() == null || field.getSubFields().isEmpty()) {
                result.put(field.getName(), null);
            } else {
                result.put(field.getName(), buildModelFieldTree(field.getSubFields()));
            }
        }
        return result;
    }

    /**
     * Recursively builds a tree representation of the resource fields.
     *
     * @param resource the resource object to convert
     * @return a {@link Map} representing the hierarchical structure of the resource fields
     */
    private Map<String, Object> buildResourceFieldTree(Object resource) {
        Map<String, Object> result = new HashMap<>();

        if (resource instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                Object value = entry.getValue();

                if (value instanceof Map<?, ?>) {
                    result.put(key, buildResourceFieldTree(value));
                } else if (value instanceof List<?> list) {
                    result.put(key, extractListTree(list));
                } else {
                    result.put(key, null);
                }
            }
            return result;
        }
        return result;
    }

    /**
     * Converts a list of resource elements into a tree structure for validation.
     *
     * @param list the list of resource elements
     * @return a {@link Map} representing the merged tree structure of the list
     */
    private Map<String, Object> extractListTree(List<?> list) {
        Map<String, Object> merged = new HashMap<>();
        for (Object element : list) {
            if (element instanceof Map<?, ?>) {
                Map<String, Object> subtree = buildResourceFieldTree(element);
                subtree.forEach(merged::putIfAbsent);
            } else {
                merged.put("value", null);
            }
        }
        return merged;
    }

    /**
     * Recursively validates the resource tree against the model tree.
     * Throws a {@link ValidationException} if any extra fields are found
     * in the resource that are not defined in the model.
     *
     * @param model the model tree defining allowed fields
     * @param resource the resource tree to validate
     * @param path the path prefix used for building error messages for nested fields
     * @throws ValidationException if an extra field is detected
     */
    private void validateTrees(Map<String, Object> model, Map<String, Object> resource, String path) {
        for (String field : resource.keySet()) {
            if (!model.containsKey(field)) {
                throw new ValidationException("Extra field found: " + path + field);
            }
        }
        for (Map.Entry<String, Object> modelEntry : model.entrySet()) {
            String field = modelEntry.getKey();
            Object modelChild = modelEntry.getValue();
            Object resourceChild = resource.get(field);
            if (modelChild instanceof Map && resourceChild instanceof Map) {
                validateTrees(
                        (Map<String, Object>) modelChild,
                        (Map<String, Object>) resourceChild,
                        path + field + "."
                );
            }
        }
    }

}
