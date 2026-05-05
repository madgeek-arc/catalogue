/*
 * Copyright 2021-2026 OpenAIRE AMKE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gr.uoa.di.madgik.catalogue.service;

import gr.uoa.di.madgik.catalogue.domain.*;
import gr.uoa.di.madgik.catalogue.domain.types.DateProperties;
import gr.uoa.di.madgik.catalogue.domain.types.VocabularyProperties;
import gr.uoa.di.madgik.registry.domain.ResourceType;
import gr.uoa.di.madgik.registry.domain.index.IndexField;
import gr.uoa.di.madgik.registry.domain.index.SearchCapability;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.*;

/**
 * Maps a catalogue {@link Model} definition to a Registry {@link ResourceType}.
 *
 * <p>The mapper derives {@link IndexField} definitions from the model's sections and fields,
 * preserving nested access paths, multiplicity, search capabilities, and vocabulary relations.
 * It also ensures the generated resource type has the Registry defaults required for JSON payload
 * handling.</p>
 */
public class ModelResourceTypeMapper {

    static final String PRIMARY_KEY_FIELD_NAME = "resource_internal_id";
    static final String PRIMARY_KEY_FIELD_PATH = "$.id";
    static final String JSON_PAYLOAD_TYPE = "json";
    static final String DEFAULT_SCHEMA = "{}";
    static final String DEFAULT_INDEX_MAPPER_CLASS = "gr.uoa.di.madgik.registry.index.DefaultIndexMapper";
    static final String DEFAULT_VOCABULARY_RESOURCE_TYPE = "vocabulary";

    /**
     * Applies model-derived Registry metadata onto the provided resource type.
     *
     * <p>If the model declares a resource type name, that name becomes the target
     * {@link ResourceType#getName()}. Existing payload/index-mapper defaults are preserved unless
     * blank, and index fields are regenerated from the model structure on every invocation.</p>
     *
     * @param model        the source model definition
     * @param resourceType the target resource type to populate
     * @return the populated resource type
     */
    public final ResourceType map(Model model, ResourceType resourceType) {
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        if (resourceType == null) {
            throw new IllegalArgumentException("ResourceType cannot be null");
        }

        if (StringUtils.hasText(model.getResourceType())) {
            resourceType.setName(model.getResourceType());
        }
        if (!StringUtils.hasText(resourceType.getName())) {
            throw new IllegalArgumentException("ResourceType name cannot be blank");
        }

        if (!StringUtils.hasText(resourceType.getPayloadType())) {
            resourceType.setPayloadType(JSON_PAYLOAD_TYPE);
        }
        if (!StringUtils.hasText(resourceType.getIndexMapperClass())) {
            resourceType.setIndexMapperClass(DEFAULT_INDEX_MAPPER_CLASS);
        }
        if (!StringUtils.hasText(resourceType.getSchema()) && !StringUtils.hasText(resourceType.getSchemaUrl())) {
            resourceType.setSchema(DEFAULT_SCHEMA);
        }

        List<IndexField> indexFields = new ArrayList<>();
        indexFields.add(createPrimaryKeyField(resourceType));

        Set<String> usedNames = new LinkedHashSet<>();
        usedNames.add(PRIMARY_KEY_FIELD_NAME);
        Set<String> usedPaths = new LinkedHashSet<>();
        usedPaths.add(PRIMARY_KEY_FIELD_PATH);

        if (model.getSections() != null) {
            for (Section section : model.getSections()) {
                collectSectionFields(section, resourceType, usedNames, usedPaths, indexFields, rootSectionPath(section), false);
            }
        }
        mergeAdditionalIndexFields(model, resourceType, usedNames, usedPaths, indexFields);

        resourceType.setIndexFields(indexFields);
        return resourceType;
    }

    /**
     * Hook for downstream projects to contribute additional index fields alongside the fields
     * derived from the UI model.
     *
     * <p>The default implementation contributes no extra fields. Returned fields are merged after
     * model-derived fields. Paths already present are ignored, while name collisions are resolved
     * with the same uniqueness rules used for model-derived fields.</p>
     */
    protected List<IndexField> additionalIndexFields(Model model, ResourceType resourceType) {
        return Collections.emptyList();
    }

    /**
     * Convenience factory for downstream projects contributing extra index fields through
     * {@link #additionalIndexFields(Model, ResourceType)}.
     */
    protected IndexField additionalIndexField(ResourceType resourceType, String name, String label, String path, String type,
                                              boolean multivalued) {
        IndexField indexField = new IndexField();
        indexField.setResourceType(resourceType);
        indexField.setName(name);
        indexField.setLabel(label);
        indexField.setPath(path);
        indexField.setType(type);
        indexField.setMultivalued(multivalued);
        return indexField;
    }

    /**
     * Convenience factory for downstream projects contributing extra index fields through
     * {@link #additionalIndexFields(Model, ResourceType)}.
     */
    protected IndexField additionalIndexField(ResourceType resourceType, String name, String label, String path, String type,
                                              boolean multivalued, Set<SearchCapability> capabilities,
                                              float embeddingWeight) {
        IndexField indexField = additionalIndexField(resourceType, name, label, path, type, multivalued);
        indexField.setSearchCapabilities(capabilities);
        indexField.setEmbeddingWeight(embeddingWeight);
        return indexField;
    }

    /**
     * Recursively traverses a section tree and emits index fields for all eligible UI fields.
     *
     * @param section             the current section to inspect
     * @param resourceType        the resource type receiving generated index fields
     * @param usedNames           the set of already reserved index field names
     * @param indexFields         the mutable output list of generated index fields
     * @param pathSegments        the current logical nesting path
     * @param ancestorMultivalued whether any ancestor path segment is multivalued
     */
    private void collectSectionFields(Section section, ResourceType resourceType, Set<String> usedNames,
                                      Set<String> usedPaths,
                                      List<IndexField> indexFields, List<PathSegment> pathSegments,
                                      boolean ancestorMultivalued) {
        if (section == null) {
            return;
        }
        if (section.getFields() != null) {
            for (UiField field : section.getFields()) {
                collectUiField(field, resourceType, usedNames, usedPaths, indexFields, pathSegments, ancestorMultivalued);
            }
        }
        if (section.getSubSections() != null) {
            for (Section subSection : section.getSubSections()) {
                collectSectionFields(subSection, resourceType, usedNames, usedPaths, indexFields, pathSegments, ancestorMultivalued);
            }
        }
    }

    /**
     * Generates an {@link IndexField} for the given UI field when its type is indexable, then
     * continues recursively through any sub-fields.
     */
    private void collectUiField(UiField field, ResourceType resourceType, Set<String> usedNames,
                                Set<String> usedPaths,
                                List<IndexField> indexFields, List<PathSegment> parentPathSegments,
                                boolean ancestorMultivalued) {
        if (field == null) {
            return;
        }

        boolean currentMultivalued = isMultivalued(field.getTypeInfo());
        boolean effectiveMultivalued = ancestorMultivalued || currentMultivalued;
        List<PathSegment> currentPathSegments = appendPathSegment(parentPathSegments, field.getName(), currentMultivalued);
        if (shouldCreateIndexField(field)) {
            String path = resolvePath(field, currentPathSegments);
            if (usedPaths.add(path)) {
                indexFields.add(createIndexField(field, resourceType, usedNames, path, currentPathSegments, effectiveMultivalued));
            }
        }

        if (field.getSubFields() != null) {
            for (UiField subField : field.getSubFields()) {
                collectUiField(subField, resourceType, usedNames, usedPaths, indexFields, currentPathSegments, effectiveMultivalued);
            }
        }
    }

    private void mergeAdditionalIndexFields(Model model, ResourceType resourceType, Set<String> usedNames,
                                            Set<String> usedPaths, List<IndexField> indexFields) {
        List<IndexField> additionalFields = additionalIndexFields(model, resourceType);
        if (additionalFields == null) {
            return;
        }
        for (IndexField additionalField : additionalFields) {
            if (additionalField == null || !StringUtils.hasText(additionalField.getPath())) {
                continue;
            }
            String path = additionalField.getPath().trim();
            if (!usedPaths.add(path)) {
                continue;
            }

            IndexField mergedField = new IndexField();
            mergedField.setResourceType(resourceType);
            mergedField.setName(uniqueFieldName(
                    firstNonBlank(additionalField.getName(), path),
                    pathSegmentsFromPath(path),
                    usedNames
            ));
            mergedField.setPath(path);
            mergedField.setType(StringUtils.hasText(additionalField.getType())
                    ? additionalField.getType()
                    : String.class.getName());
            mergedField.setMultivalued(additionalField.isMultivalued());
            mergedField.setLabel(additionalField.getLabel());
            mergedField.setPrimaryKey(additionalField.isPrimaryKey());
            mergedField.setDefaultValue(additionalField.getDefaultValue());
            mergedField.setSearchCapabilities(additionalField.getSearchCapabilities());
            mergedField.setRelatedResourceType(additionalField.getRelatedResourceType());
            mergedField.setRelatedResourceTypeField(additionalField.getRelatedResourceTypeField());
            mergedField.setEmbeddingWeight(additionalField.getEmbeddingWeight());
            indexFields.add(mergedField);
        }
    }

    /**
     * Creates the mandatory Registry primary key field mapped to {@code $.id}.
     */
    private IndexField createPrimaryKeyField(ResourceType resourceType) {
        IndexField indexField = new IndexField();
        indexField.setResourceType(resourceType);
        indexField.setName(PRIMARY_KEY_FIELD_NAME);
        indexField.setPath(PRIMARY_KEY_FIELD_PATH);
        indexField.setType(String.class.getName());
        indexField.setPrimaryKey(true);
        indexField.setMultivalued(false);
        indexField.setLabel("ID");
        return indexField;
    }

    /**
     * Builds a Registry {@link IndexField} from a model field, including type, path, search
     * behavior, vocabulary relation metadata, and embedding defaults for strings.
     */
    private IndexField createIndexField(UiField field, ResourceType resourceType, Set<String> usedNames, String path,
                                        List<PathSegment> pathSegments, boolean multivalued) {
        String javaType = resolveJavaType(field.getTypeInfo());

        IndexField indexField = new IndexField();
        indexField.setResourceType(resourceType);
        indexField.setName(uniqueFieldName(field, pathSegments, usedNames));
        indexField.setPath(path);
        indexField.setType(javaType);
        indexField.setMultivalued(multivalued);

        applySearchCapabilities(field.getTypeInfo(), indexField);
        indexField.setLabel(indexField.getSearchCapabilities().contains(SearchCapability.KEYWORD) ?
                // if field is not mapped as a keyword, skip labeling to treat it as not a facet field
                resolveLabel(field) : null
        );

        applyVocabularyRelation(field.getTypeInfo(), indexField);

        if (String.class.getName().equals(javaType)) { // include all string fields in embedding vector
            indexField.setEmbeddingWeight(1.0f);
        }
        return indexField;
    }

    /**
     * Returns whether the UI field should materialize as a Registry index field.
     *
     * <p>Composite fields are structural containers only; their concrete sub-fields carry the
     * actual indexable values.</p>
     */
    private boolean shouldCreateIndexField(UiField field) {
        TypeInfo typeInfo = field.getTypeInfo();
        FieldType fieldType = typeInfo == null ? null : typeInfo.getType();
        return fieldType != FieldType.composite;
    }

    /**
     * Produces a unique Registry field name from model metadata.
     *
     * <p>The name is sanitized to a Registry-safe identifier, deduplicated with numeric suffixes,
     * and for nested fields named {@code id}, prefixed with the parent path to avoid collisions
     * with common identifier fields deeper in the model tree.</p>
     */
    private String uniqueFieldName(UiField field, List<PathSegment> pathSegments, Set<String> usedNames) {
        return uniqueFieldName(firstNonBlank(field.getName(), field.getAccessPath()), pathSegments, usedNames);
    }

    private String uniqueFieldName(String rawName, List<PathSegment> pathSegments, Set<String> usedNames) {
        String baseName = sanitizeFieldName(rawName);
        if ("id".equals(baseName) && pathSegments != null && pathSegments.size() > 1) {
            String nestingPrefix = sanitizeFieldName(pathSegments.subList(0, pathSegments.size() - 1).stream()
                    .map(PathSegment::name)
                    .filter(StringUtils::hasText)
                    .collect(java.util.stream.Collectors.joining("_")));
            if (StringUtils.hasText(nestingPrefix)) {
                baseName = nestingPrefix + "_" + baseName;
            }
        }
        String candidate = baseName;
        int suffix = 2;
        while (!usedNames.add(candidate)) {
            candidate = baseName + "_" + suffix++;
        }
        return candidate;
    }

    /**
     * Normalizes an arbitrary field identifier into an ASCII-ish Registry field name.
     */
    private String sanitizeFieldName(String value) {
        String normalized = value == null ? "" : value.trim().replaceAll("[^A-Za-z0-9_]", "_");
        normalized = normalized.replaceAll("_+", "_");
        normalized = normalized.replaceAll("^_+|_+$", "");
        if (normalized.isEmpty()) {
            normalized = "field";
        }
        if (Character.isDigit(normalized.charAt(0))) {
            normalized = "field_" + normalized;
        }
        return normalized;
    }

    /**
     * Resolves the human-readable label text to store on the Registry index field.
     */
    private String resolveLabel(UiField field) {
        StyledString styledString = field.getLabel();
        return styledString == null ? null : styledString.getText();
    }

    /**
     * Returns whether the field should be treated as multivalued in generated Registry metadata.
     */
    private boolean isMultivalued(TypeInfo typeInfo) {
        return typeInfo != null && typeInfo.isMultiplicity();
    }

    /**
     * Assigns Registry search capabilities based on the catalogue field type.
     */
    private void applySearchCapabilities(TypeInfo typeInfo, IndexField indexField) {
        FieldType fieldType = typeInfo == null ? null : typeInfo.getType();
        if (fieldType == null) {
            return;
        }

        switch (fieldType) {
            case largeText, richText -> indexField.setSearchCapabilities(EnumSet.of(SearchCapability.TEXT));
            case string -> indexField.setSearchCapabilities(EnumSet.of(SearchCapability.KEYWORD));
            default -> {
            }
        }
    }

    /**
     * Maps vocabulary field metadata to Registry related-resource references used by facet label
     * resolution.
     */
    private void applyVocabularyRelation(TypeInfo typeInfo, IndexField indexField) {
        if (typeInfo == null || typeInfo.getType() != FieldType.vocabulary) {
            return;
        }
        if (!(typeInfo.getProperties() instanceof VocabularyProperties vocabularyProperties)) {
            return;
        }

        indexField.setRelatedResourceTypeField(vocabularyProperties.getLabelField());
        indexField.setRelatedResourceType(resolveRelatedResourceType(vocabularyProperties));
    }

    /**
     * Infers the related Registry resource type for a vocabulary field from its configured URL.
     */
    private String resolveRelatedResourceType(VocabularyProperties vocabularyProperties) {
        URI url = vocabularyProperties.getUrl();
        if (url == null || !StringUtils.hasText(url.getPath())) {
            return DEFAULT_VOCABULARY_RESOURCE_TYPE;
        }

        String path = url.getPath().trim();
        String[] segments = path.split("/");
        List<String> nonBlankSegments = new ArrayList<>();
        for (String segment : segments) {
            if (StringUtils.hasText(segment)) {
                nonBlankSegments.add(segment.trim());
            }
        }

        if (nonBlankSegments.size() >= 3
                && "api".equals(nonBlankSegments.get(0))
                && "vocabulary".equals(nonBlankSegments.get(1))) {
            return DEFAULT_VOCABULARY_RESOURCE_TYPE;
        }

        if (nonBlankSegments.size() >= 3
                && "api".equals(nonBlankSegments.get(0))
                && "list".equals(nonBlankSegments.get(nonBlankSegments.size() - 1))) {
            return nonBlankSegments.get(1);
        }

        return DEFAULT_VOCABULARY_RESOURCE_TYPE;
    }

    /**
     * Resolves the Java type stored in Registry metadata for the given catalogue field type.
     */
    private String resolveJavaType(TypeInfo typeInfo) {
        FieldType fieldType = typeInfo == null ? null : typeInfo.getType();
        if (fieldType == null) {
            return String.class.getName();
        }

        return switch (fieldType) {
            case number, scale -> Float.class.getName();
            case bool, checkbox -> Boolean.class.getName();
            case date -> {
                DateProperties properties = (DateProperties) typeInfo.getProperties();
                yield properties != null && properties.isFormatToString() ?
                        java.lang.String.class.getName() : java.time.Instant.class.getName();
            }
            default -> String.class.getName();
        };
    }

    private List<PathSegment> pathSegmentsFromPath(String path) {
        if (!StringUtils.hasText(path) || "$".equals(path.trim())) {
            return Collections.emptyList();
        }
        String normalized = path.trim();
        if (normalized.startsWith("$.")) {
            normalized = normalized.substring(2);
        } else if (normalized.startsWith("$")) {
            normalized = normalized.substring(1);
        }

        List<PathSegment> segments = new ArrayList<>();
        for (String rawSegment : normalized.split("\\.")) {
            if (!StringUtils.hasText(rawSegment)) {
                continue;
            }
            String segment = rawSegment.trim();
            boolean multivalued = segment.endsWith("[*]");
            if (multivalued) {
                segment = segment.substring(0, segment.length() - 3);
            } else if ("*".equals(segment)) {
                continue;
            }
            if (StringUtils.hasText(segment)) {
                segments.add(new PathSegment(segment, multivalued));
            }
        }
        return segments;
    }

    /**
     * Normalizes a model access path into Registry JSONPath notation.
     */
    private String normalizePath(String accessPath) {
        if (!StringUtils.hasText(accessPath)) {
            return null;
        }
        String path = accessPath.trim();
        if (path.startsWith("$")) {
            return path;
        }
        if (path.startsWith(".")) {
            return "$" + path;
        }
        return "$." + path;
    }

    /**
     * Resolves the Registry JSONPath for the field, preferring an explicit access path when one is
     * defined and otherwise deriving it from the section/field nesting path.
     */
    private String resolvePath(UiField field, List<PathSegment> pathSegments) {
        String explicitPath = normalizePath(field.getAccessPath());
        if (explicitPath != null) {
            return explicitPath;
        }
        if (pathSegments.isEmpty()) {
            return "$";
        }
        StringBuilder builder = new StringBuilder("$");
        for (PathSegment segment : pathSegments) {
            builder.append('.').append(segment.name());
            if (segment.multivalued()) {
                builder.append("[*]");
            }
        }
        return builder.toString();
    }

    /**
     * Starts a derived path from the root section name, when present.
     */
    private List<PathSegment> rootSectionPath(Section section) {
        if (section == null || !StringUtils.hasText(section.getName())) {
            return Collections.emptyList();
        }
        return List.of(new PathSegment(section.getName().trim(), false));
    }

    /**
     * Appends a logical path segment used for deriving JSONPath expressions and nested-name
     * disambiguation.
     */
    private List<PathSegment> appendPathSegment(List<PathSegment> pathSegments, String segment, boolean multivalued) {
        if (!StringUtils.hasText(segment)) {
            return pathSegments;
        }
        List<PathSegment> updatedPath = new ArrayList<>(pathSegments);
        updatedPath.add(new PathSegment(segment.trim(), multivalued));
        return updatedPath;
    }

    private record PathSegment(String name, boolean multivalued) {
    }

    /**
     * Returns the first non-blank value from the provided candidates.
     */
    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }
}
