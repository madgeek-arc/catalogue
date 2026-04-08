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

import gr.uoa.di.madgik.catalogue.domain.FieldType;
import gr.uoa.di.madgik.catalogue.domain.Model;
import gr.uoa.di.madgik.catalogue.domain.Section;
import gr.uoa.di.madgik.catalogue.domain.StyledString;
import gr.uoa.di.madgik.catalogue.domain.TypeInfo;
import gr.uoa.di.madgik.catalogue.domain.UiField;
import gr.uoa.di.madgik.catalogue.domain.types.VocabularyProperties;
import gr.uoa.di.madgik.registry.domain.ResourceType;
import gr.uoa.di.madgik.registry.domain.index.IndexField;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ModelResourceTypeMapper {

    static final String PRIMARY_KEY_FIELD_NAME = "resource_internal_id";
    static final String PRIMARY_KEY_FIELD_PATH = "$.id";
    static final String JSON_PAYLOAD_TYPE = "json";
    static final String DEFAULT_SCHEMA = "{}";
    static final String DEFAULT_INDEX_MAPPER_CLASS = "gr.uoa.di.madgik.registry.index.DefaultIndexMapper";
    static final String DEFAULT_VOCABULARY_RESOURCE_TYPE = "vocabulary";

    public ResourceType map(Model model, ResourceType resourceType) {
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

        if (model.getSections() != null) {
            for (Section section : model.getSections()) {
                collectSectionFields(section, resourceType, usedNames, indexFields, rootSectionPath(section));
            }
        }

        resourceType.setIndexFields(indexFields);
        return resourceType;
    }

    private void collectSectionFields(Section section, ResourceType resourceType, Set<String> usedNames,
                                      List<IndexField> indexFields, List<PathSegment> pathSegments) {
        if (section == null) {
            return;
        }
        if (section.getFields() != null) {
            for (UiField field : section.getFields()) {
                collectUiField(field, resourceType, usedNames, indexFields, pathSegments);
            }
        }
        if (section.getSubSections() != null) {
            for (Section subSection : section.getSubSections()) {
                collectSectionFields(subSection, resourceType, usedNames, indexFields, pathSegments);
            }
        }
    }

    private void collectUiField(UiField field, ResourceType resourceType, Set<String> usedNames,
                                List<IndexField> indexFields, List<PathSegment> parentPathSegments) {
        if (field == null) {
            return;
        }

        List<PathSegment> currentPathSegments = appendPathSegment(parentPathSegments, field.getName(), isMultivalued(field.getTypeInfo()));
        if (shouldCreateIndexField(field)) {
            String path = resolvePath(field, currentPathSegments);
            indexFields.add(createIndexField(field, resourceType, usedNames, path));
        }

        if (field.getSubFields() != null) {
            for (UiField subField : field.getSubFields()) {
                collectUiField(subField, resourceType, usedNames, indexFields, currentPathSegments);
            }
        }
    }

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

    private IndexField createIndexField(UiField field, ResourceType resourceType, Set<String> usedNames, String path) {
        IndexField indexField = new IndexField();
        indexField.setResourceType(resourceType);
        indexField.setName(uniqueFieldName(field, usedNames));
        indexField.setPath(path);
        indexField.setType(resolveJavaType(field.getTypeInfo()));
        indexField.setMultivalued(isMultivalued(field.getTypeInfo()));
        indexField.setLabel(resolveLabel(field));
        applyVocabularyRelation(field.getTypeInfo(), indexField);
        return indexField;
    }

    private boolean shouldCreateIndexField(UiField field) {
        TypeInfo typeInfo = field.getTypeInfo();
        FieldType fieldType = typeInfo == null ? null : typeInfo.getType();
        return fieldType != FieldType.composite;
    }

    private String uniqueFieldName(UiField field, Set<String> usedNames) {
        String baseName = sanitizeFieldName(firstNonBlank(field.getId(), field.getName(), field.getAccessPath()));
        String candidate = baseName;
        int suffix = 2;
        while (!usedNames.add(candidate)) {
            candidate = baseName + "_" + suffix++;
        }
        return candidate;
    }

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
        return normalized.toLowerCase(Locale.ROOT);
    }

    private String resolveLabel(UiField field) {
        StyledString styledString = field.getLabel();
        return styledString == null ? null : styledString.getText();
    }

    private boolean isMultivalued(TypeInfo typeInfo) {
        return typeInfo != null && typeInfo.isMultiplicity();
    }

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

    private String resolveJavaType(TypeInfo typeInfo) {
        FieldType fieldType = typeInfo == null ? null : typeInfo.getType();
        if (fieldType == null) {
            return String.class.getName();
        }

        return switch (fieldType) {
            case number, scale -> Float.class.getName();
            case bool, checkbox -> Boolean.class.getName();
            case date -> java.time.Instant.class.getName();
            default -> String.class.getName();
        };
    }

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

    private List<PathSegment> rootSectionPath(Section section) {
        if (section == null || !StringUtils.hasText(section.getName())) {
            return Collections.emptyList();
        }
        return List.of(new PathSegment(section.getName().trim(), false));
    }

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

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }
}
