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

import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import gr.uoa.di.madgik.catalogue.domain.*;
import gr.uoa.di.madgik.catalogue.domain.types.DateProperties;
import gr.uoa.di.madgik.catalogue.domain.types.VocabularyProperties;
import gr.uoa.di.madgik.registry.domain.ResourceType;
import gr.uoa.di.madgik.registry.domain.index.IndexField;
import gr.uoa.di.madgik.registry.domain.index.SearchCapability;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;

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
    private static final Pattern SAFE_JSONPATH_BARE_SEGMENT = Pattern.compile("[A-Za-z0-9_-]+");

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
        mergeCustomResourceTypeProperties(resourceType);

        IndexField primaryKeyField = primaryKeyIndexField(model, resourceType);
        validatePrimaryKeyField(primaryKeyField, resourceType);

        Set<String> usedNames = new LinkedHashSet<>();
        usedNames.add(primaryKeyField.getName());
        Set<String> usedPaths = new LinkedHashSet<>();
        usedPaths.add(primaryKeyField.getPath());

        List<IndexField> indexFields = new ArrayList<>();
        indexFields.add(primaryKeyField);

        List<FieldCandidate> candidates = new ArrayList<>();
        if (model.getSections() != null) {
            for (Section section : model.getSections()) {
                List<PathSegment> rootPath = rootSectionPath(section);
                collectSectionFields(section, usedPaths, candidates, rootPath, rootPath.size(), false, true);
            }
        }
        Map<FieldCandidate, String> fieldNames = resolveModelFieldNames(candidates);
        for (FieldCandidate candidate : candidates) {
            indexFields.add(createIndexField(candidate, fieldNames.get(candidate), resourceType, usedNames));
        }
        mergeAdditionalIndexFields(model, resourceType, usedNames, usedPaths, indexFields);

        validateIndexFieldPaths(indexFields, resourceType);
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
     * Hook for downstream projects to add or override Registry resource type properties before
     * index fields are generated.
     *
     * <p>The default implementation contributes no custom properties. Returned values are merged
     * into {@link ResourceType#getProperties()}, replacing existing values with the same key.</p>
     */
    protected Map<String, String> customResourceTypeProperties(ResourceType resourceType) {
        return Collections.emptyMap();
    }

    /**
     * Hook for downstream projects to customize the primary key index field.
     *
     * <p>The returned field is always marked as primary key and associated with the mapped
     * {@link ResourceType}. Its name and path are reserved before model-derived and additional
     * fields are merged, so later fields with the same path are ignored and later name collisions
     * are deduplicated.</p>
     */
    protected IndexField primaryKeyIndexField(Model model, ResourceType resourceType) {
        return primaryKeyIndexField(resourceType, null, PRIMARY_KEY_FIELD_PATH, String.class.getName());
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
     * Convenience factory for downstream projects overriding the primary key index field through
     * {@link #primaryKeyIndexField(Model, ResourceType)}.
     */
    protected IndexField primaryKeyIndexField(ResourceType resourceType, String label, String path, String type) {
        IndexField indexField = additionalIndexField(resourceType, PRIMARY_KEY_FIELD_NAME, label, path, type, false);
        indexField.setPrimaryKey(true);
        return indexField;
    }

    /**
     * Recursively traverses a section tree and collects every UI field that should become an
     * index field.
     *
     * @param section             the current section to inspect
     * @param usedPaths           the set of already reserved index field paths
     * @param candidates          the mutable output list of collected fields
     * @param pathSegments        the current logical nesting path
     * @param rootDepth           the number of leading path segments contributed by the root section
     * @param ancestorMultivalued whether any ancestor path segment is multivalued
     */
    private void collectSectionFields(Section section, Set<String> usedPaths, List<FieldCandidate> candidates,
                                      List<PathSegment> pathSegments, int rootDepth, boolean ancestorMultivalued,
                                      boolean ancestorFacet) {
        if (section == null) {
            return;
        }
        if (section.getFields() != null) {
            for (UiField field : section.getFields()) {
                collectUiField(field, usedPaths, candidates, pathSegments, rootDepth, ancestorMultivalued,
                        ancestorFacet);
            }
        }
        if (section.getSubSections() != null) {
            for (Section subSection : section.getSubSections()) {
                collectSectionFields(subSection, usedPaths, candidates, pathSegments, rootDepth, ancestorMultivalued,
                        ancestorFacet);
            }
        }
    }

    /**
     * Collects the given UI field when its type is indexable and its path is not yet reserved,
     * then continues recursively through any sub-fields. A field not marked for indexing is
     * skipped together with its sub-fields, and a field marked as no facet makes its sub-fields
     * no facets either.
     */
    private void collectUiField(UiField field, Set<String> usedPaths, List<FieldCandidate> candidates,
                                List<PathSegment> parentPathSegments, int rootDepth, boolean ancestorMultivalued,
                                boolean ancestorFacet) {
        if (field == null || !field.isIndex()) {
            return;
        }

        boolean currentMultivalued = isMultivalued(field.getTypeInfo());
        boolean effectiveMultivalued = ancestorMultivalued || currentMultivalued;
        boolean facet = ancestorFacet && !Boolean.FALSE.equals(field.getFacet());
        List<PathSegment> currentPathSegments = appendPathSegment(parentPathSegments, field.getName(), currentMultivalued);
        if (shouldCreateIndexField(field)) {
            String path = resolvePath(field, currentPathSegments);
            if (usedPaths.add(path)) {
                List<String> parentNames = parentPathSegments.subList(Math.min(rootDepth, parentPathSegments.size()),
                                parentPathSegments.size()).stream()
                        .map(PathSegment::name)
                        .toList();
                candidates.add(new FieldCandidate(field, path, currentPathSegments, parentNames, effectiveMultivalued,
                        facet));
            }
        }

        if (field.getSubFields() != null) {
            for (UiField subField : field.getSubFields()) {
                collectUiField(subField, usedPaths, candidates, currentPathSegments, rootDepth, effectiveMultivalued,
                        facet);
            }
        }
    }

    /**
     * Assigns a base index field name to every collected model field.
     *
     * <p>Fields whose base name is unique keep it. Fields sharing a base name are prefixed, in
     * camelCase, with the fewest trailing parent field names (root section excluded) that tell
     * them apart, e.g. {@code mainContact.email} and {@code publicContacts.email} become
     * {@code mainContactEmail} and {@code publicContactsEmail}, while a top-level
     * {@code accessPolicy} keeps its name next to {@code classTierAccessPolicy}. The result
     * depends only on the field paths, not on their order in the model. Fields that cannot be
     * told apart this way keep the longest prefixed name and are later deduplicated with a
     * numeric suffix.</p>
     */
    private Map<FieldCandidate, String> resolveModelFieldNames(List<FieldCandidate> candidates) {
        Map<String, List<FieldCandidate>> byBaseName = new LinkedHashMap<>();
        Map<FieldCandidate, String> baseNames = new IdentityHashMap<>();
        for (FieldCandidate candidate : candidates) {
            String baseName = baseFieldName(firstNonBlank(candidate.field().getName(), candidate.field().getAccessPath()),
                    candidate.pathSegments());
            baseNames.put(candidate, baseName);
            byBaseName.computeIfAbsent(baseName, key -> new ArrayList<>()).add(candidate);
        }

        Map<FieldCandidate, String> resolved = new IdentityHashMap<>();
        byBaseName.forEach((baseName, group) -> {
            if (group.size() == 1) {
                resolved.put(group.getFirst(), baseName);
                return;
            }
            int maxDepth = group.stream().mapToInt(candidate -> candidate.parentNames().size()).max().orElse(0);
            for (FieldCandidate candidate : group) {
                int depth = 0;
                while (depth <= maxDepth && !isDistinctAtDepth(candidate, group, depth)) {
                    depth++;
                }
                resolved.put(candidate, prefixedFieldName(trailingParents(candidate, Math.min(depth, maxDepth)), baseName));
            }
        });
        return resolved;
    }

    private boolean isDistinctAtDepth(FieldCandidate candidate, List<FieldCandidate> group, int depth) {
        List<String> parents = trailingParents(candidate, depth);
        for (FieldCandidate other : group) {
            if (other != candidate && trailingParents(other, depth).equals(parents)) {
                return false;
            }
        }
        return true;
    }

    private List<String> trailingParents(FieldCandidate candidate, int depth) {
        List<String> parents = candidate.parentNames();
        return parents.subList(Math.max(0, parents.size() - depth), parents.size());
    }

    private String prefixedFieldName(List<String> parents, String baseName) {
        if (parents.isEmpty()) {
            return baseName;
        }
        StringBuilder builder = new StringBuilder(sanitizeFieldName(parents.getFirst()));
        for (String parent : parents.subList(1, parents.size())) {
            builder.append(capitalize(sanitizeFieldName(parent)));
        }
        builder.append(capitalize(baseName));
        return sanitizeFieldName(builder.toString());
    }

    private String capitalize(String value) {
        return value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1);
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

    private void mergeCustomResourceTypeProperties(ResourceType resourceType) {
        Map<String, String> customProperties = customResourceTypeProperties(resourceType);
        if (customProperties == null || customProperties.isEmpty()) {
            return;
        }

        Map<String, String> properties = resourceType.getProperties();
        if (properties == null) {
            properties = new LinkedHashMap<>();
            resourceType.setProperties(properties);
        }
        properties.putAll(customProperties);
    }

    private void validatePrimaryKeyField(IndexField indexField, ResourceType resourceType) {
        if (indexField == null) {
            throw new IllegalArgumentException("Primary key index field cannot be null");
        }
        if (!StringUtils.hasText(indexField.getName())) {
            throw new IllegalArgumentException("Primary key index field name cannot be blank");
        }
        if (!StringUtils.hasText(indexField.getPath())) {
            throw new IllegalArgumentException("Primary key index field path cannot be blank");
        }
        indexField.setName(indexField.getName().trim());
        indexField.setPath(indexField.getPath().trim());
        if (!StringUtils.hasText(indexField.getType())) {
            indexField.setType(String.class.getName());
        }
        indexField.setResourceType(resourceType);
        indexField.setPrimaryKey(true);
    }

    /**
     * Validates that every generated {@link IndexField#getPath()} is a syntactically valid
     * JSONPath expression, failing fast at Model create/update time instead of surfacing later as
     * an unrelated indexing failure when a resource is submitted.
     */
    private void validateIndexFieldPaths(List<IndexField> indexFields, ResourceType resourceType) {
        for (IndexField indexField : indexFields) {
            try {
                JsonPath.compile(indexField.getPath());
            } catch (InvalidPathException e) {
                throw new IllegalArgumentException("Invalid JSONPath '" + indexField.getPath()
                        + "' generated for index field '" + indexField.getName()
                        + "' on resource type '" + resourceType.getName() + "': " + e.getMessage(), e);
            }
        }
    }

    /**
     * Builds a Registry {@link IndexField} from a model field, including type, path, search
     * behavior, vocabulary relation metadata, and embedding defaults for strings.
     */
    private IndexField createIndexField(FieldCandidate candidate, String baseName, ResourceType resourceType,
                                        Set<String> usedNames) {
        UiField field = candidate.field();
        String javaType = resolveJavaType(field.getTypeInfo());

        IndexField indexField = new IndexField();
        indexField.setResourceType(resourceType);
        indexField.setName(reserveFieldName(baseName, usedNames));
        indexField.setPath(candidate.path());
        indexField.setType(javaType);
        indexField.setMultivalued(candidate.multivalued());

        applySearchCapabilities(field.getTypeInfo(), indexField);
        // Registry treats a labeled keyword field as a facet, so a field that is no facet gets no label.
        indexField.setLabel(candidate.facet() && indexField.getSearchCapabilities().contains(SearchCapability.KEYWORD)
                ? resolveLabel(field) : null);

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
     * Produces a unique Registry field name from a raw name, deduplicated with numeric suffixes.
     */
    private String uniqueFieldName(String rawName, List<PathSegment> pathSegments, Set<String> usedNames) {
        return reserveFieldName(baseFieldName(rawName, pathSegments), usedNames);
    }

    /**
     * Sanitizes a raw name to a Registry-safe identifier. Nested fields named {@code id} are
     * prefixed with their parent path, so they do not collide with top-level identifiers.
     */
    private String baseFieldName(String rawName, List<PathSegment> pathSegments) {
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
        return baseName;
    }

    /**
     * Reserves {@code baseName}, appending {@code _2}, {@code _3}, ... when it is already taken.
     */
    private String reserveFieldName(String baseName, Set<String> usedNames) {
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
            appendJsonPathSegment(builder, segment.name());
            if (segment.multivalued()) {
                builder.append("[*]");
            }
        }
        return builder.toString();
    }

    /**
     * Appends a single JSONPath segment to {@code builder}, using dot notation when {@code name}
     * is a safe bare identifier and escaped bracket notation otherwise, so segment names
     * containing spaces or other special characters always yield a syntactically valid JSONPath
     * expression.
     */
    private void appendJsonPathSegment(StringBuilder builder, String name) {
        if (SAFE_JSONPATH_BARE_SEGMENT.matcher(name).matches()) {
            builder.append('.').append(name);
        } else {
            builder.append("['").append(escapeBracketSegment(name)).append("']");
        }
    }

    /**
     * Escapes a segment name for inclusion inside a single-quoted JSONPath bracket-notation
     * property, per Jayway JsonPath's unescaping rules (backslash and single-quote are the only
     * characters that require escaping).
     */
    private String escapeBracketSegment(String name) {
        return name.replace("\\", "\\\\").replace("'", "\\'");
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
     * A model field selected for indexing, with its nesting path and the names of its parent
     * fields below the root section.
     */
    private record FieldCandidate(UiField field, String path, List<PathSegment> pathSegments,
                                  List<String> parentNames, boolean multivalued, boolean facet) {
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
