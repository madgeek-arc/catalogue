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
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ModelResourceTypeMapperTest {

    private final ModelResourceTypeMapper mapper = new ModelResourceTypeMapper();

    @Test
    void mapBuildsPrimaryAndDynamicIndexFields() {
        Model model = new Model();
        model.setResourceType("sample_entity");
        model.setSections(List.of(section(field("Title", "title", null, FieldType.string, false))));

        ResourceType resourceType = new ResourceType();
        resourceType.setName("sample_entity");

        ResourceType mapped = mapper.map(model, resourceType);

        assertEquals("sample_entity", mapped.getName());
        assertEquals("json", mapped.getPayloadType());
        assertEquals("{}", mapped.getSchema());
        assertNotNull(mapped.getIndexFields());
        assertEquals(2, mapped.getIndexFields().size());

        IndexField primaryKey = mapped.getIndexFields().getFirst();
        assertEquals("resource_internal_id", primaryKey.getName());
        assertEquals("$.id", primaryKey.getPath());
        assertEquals("java.lang.String", primaryKey.getType());

        IndexField title = mapped.getIndexFields().get(1);
        assertEquals("title", title.getName());
        assertEquals("$.main.title", title.getPath());
        assertEquals("java.lang.String", title.getType());
        assertEquals("Title", title.getLabel());
    }

    @Test
    void mapHandlesNestedFieldsTypeInferenceAndSanitizedNames() {
        UiField composite = field("Person", "person", null, FieldType.composite, false);
        composite.setSubFields(List.of(
                field("Start Date", "start-date", null, FieldType.date, false),
                field("Score", "1score", null, FieldType.number, false),
                field("Tags", "tags", null, FieldType.select, true)
        ));

        Model model = new Model();
        model.setResourceType("dataset");
        model.setSections(List.of(section(composite)));

        ResourceType mapped = mapper.map(model, new ResourceType());

        assertEquals(4, mapped.getIndexFields().size());
        assertEquals("start_date", mapped.getIndexFields().get(1).getName());
        assertEquals("$.main.person.start-date", mapped.getIndexFields().get(1).getPath());
        assertEquals("java.time.Instant", mapped.getIndexFields().get(1).getType());

        assertEquals("field_1score", mapped.getIndexFields().get(2).getName());
        assertEquals("$.main.person.1score", mapped.getIndexFields().get(2).getPath());
        assertEquals("java.lang.Float", mapped.getIndexFields().get(2).getType());

        assertEquals("tags", mapped.getIndexFields().get(3).getName());
        assertEquals("$.main.person.tags[*]", mapped.getIndexFields().get(3).getPath());
        assertEquals("java.lang.String", mapped.getIndexFields().get(3).getType());
        assertEquals(true, mapped.getIndexFields().get(3).isMultivalued());
    }

    @Test
    void mapAddsJsonPathWildcardForMultivaluedCompositeSegments() {
        UiField childField = field("Child Field", "childField", null, FieldType.string, false);

        UiField groupedItems = field("Grouped Items", "groupedItems", null, FieldType.composite, true);
        groupedItems.setSubFields(List.of(childField));

        Model model = new Model();
        model.setResourceType("sample_entity");
        model.setSections(List.of(section(groupedItems)));

        ResourceType mapped = mapper.map(model, new ResourceType());

        IndexField indexField = mapped.getIndexFields().get(1);
        assertEquals("$.main.groupedItems[*].childField", indexField.getPath());
        assertEquals(true, indexField.isMultivalued());
    }

    @Test
    void mapMarksNestedLeafAsMultivaluedWhenAnyAncestorHasMultiplicity() {
        UiField grandChildField = field("Grand Child Field", "grandChildField", null, FieldType.string, false);

        UiField childComposite = field("Child Composite", "childComposite", null, FieldType.composite, false);
        childComposite.setSubFields(List.of(grandChildField));

        UiField groupedItems = field("Grouped Items", "groupedItems", null, FieldType.composite, true);
        groupedItems.setSubFields(List.of(childComposite));

        Model model = new Model();
        model.setResourceType("sample_entity");
        model.setSections(List.of(section(groupedItems)));

        ResourceType mapped = mapper.map(model, new ResourceType());

        IndexField indexField = mapped.getIndexFields().get(1);
        assertEquals("$.main.groupedItems[*].childComposite.grandChildField", indexField.getPath());
        assertEquals(true, indexField.isMultivalued());
    }

    @Test
    void mapCreatesIndexFieldForNonComposite() {
        Model model = new Model();
        model.setResourceType("dataset");
        model.setSections(List.of(section(
                field("Composite", "composite", null, FieldType.composite, false),
                field("Identifier", "identifier", null, FieldType.string, false)
        )));

        ResourceType mapped = mapper.map(model, new ResourceType());

        assertEquals(2, mapped.getIndexFields().size());
        IndexField identifier = mapped.getIndexFields().get(1);
        assertEquals("identifier", identifier.getName());
        assertEquals("$.main.identifier", identifier.getPath());
        assertEquals("java.lang.String", identifier.getType());
    }

    @Test
    void mapUsesRootSectionAndIgnoresSubSectionNames() {
        Section nestedSection = new Section();
        nestedSection.setName("nestedGroup");
        nestedSection.setFields(List.of(field("Id", "id", null, FieldType.string, false)));

        Section rootSection = new Section();
        rootSection.setName("sampleEntity");
        rootSection.setSubSections(List.of(nestedSection));

        Model model = new Model();
        model.setResourceType("sample_entity");
        model.setSections(List.of(rootSection));

        ResourceType mapped = mapper.map(model, new ResourceType());

        assertEquals(2, mapped.getIndexFields().size());
        assertEquals("sampleEntity_id", mapped.getIndexFields().get(1).getName());
        assertEquals("$.sampleEntity.id", mapped.getIndexFields().get(1).getPath());
    }

    @Test
    void mapSetsVocabularyRelationsFromVocabularyProperties() {
        UiField field = field("Category", "category", null, FieldType.vocabulary, false);
        VocabularyProperties vocabularyProperties = new VocabularyProperties();
        vocabularyProperties.setLabelField("name");
        vocabularyProperties.setUrl(URI.create("/api/vocabulary/types/Category"));
        field.getTypeInfo().setProperties(vocabularyProperties);

        Model model = new Model();
        model.setResourceType("sample_entity");
        model.setSections(List.of(section(field)));

        ResourceType mapped = mapper.map(model, new ResourceType());

        IndexField indexField = mapped.getIndexFields().get(1);
        assertEquals("vocabulary", indexField.getRelatedResourceType());
        assertEquals("name", indexField.getRelatedResourceTypeField());
    }

    @Test
    void mapInfersRelatedResourceTypeFromListUrl() {
        UiField field = field("Owner", "owner", null, FieldType.vocabulary, false);
        VocabularyProperties vocabularyProperties = new VocabularyProperties();
        vocabularyProperties.setLabelField("name");
        vocabularyProperties.setUrl(URI.create("/api/organization/list"));
        field.getTypeInfo().setProperties(vocabularyProperties);

        Model model = new Model();
        model.setResourceType("sample_entity");
        model.setSections(List.of(section(field)));

        ResourceType mapped = mapper.map(model, new ResourceType());

        IndexField indexField = mapped.getIndexFields().get(1);
        assertEquals("organization", indexField.getRelatedResourceType());
        assertEquals("name", indexField.getRelatedResourceTypeField());
    }

    @Test
    void mapMergesAdditionalIndexFieldsProvidedBySubclass() {
        Model model = new Model();
        model.setResourceType("sample_entity");
        model.setSections(List.of(section(field("Title", "title", null, FieldType.string, false))));

        ModelResourceTypeMapper mapper = new AdditionalFieldsMapper();

        ResourceType mapped = mapper.map(model, new ResourceType());

        assertHasField(mapped, "catalogueId", "$.catalogueId", "java.lang.String");
        assertHasField(mapped, "active", "$.active", "java.lang.Boolean");
        assertHasField(mapped, "registeredBy", "$.metadata.registeredBy", "java.lang.String");
    }

    @Test
    void mapDoesNotDuplicateModelFieldsAlreadyMappedByPath() {
        Model model = new Model();
        model.setResourceType("sample_entity");
        UiField catalogueId = field("Catalogue Id", "catalogueId", "$.catalogueId", FieldType.string, false);
        model.setSections(List.of(section(catalogueId)));

        ModelResourceTypeMapper mapper = new AdditionalFieldsMapper();

        ResourceType mapped = mapper.map(model, new ResourceType());

        assertEquals(1, mapped.getIndexFields().stream().filter(f -> "$.catalogueId".equals(f.getPath())).count());
    }

    @Test
    void mapSkipsAdditionalIndexFieldsWithoutPath() {
        Model model = new Model();
        model.setResourceType("sample_entity");
        model.setSections(List.of(section(field("Title", "title", null, FieldType.string, false))));

        ResourceType mapped = new ModelResourceTypeMapper() {
            @Override
            protected List<IndexField> additionalIndexFields(Model inputModel, ResourceType resourceType) {
                IndexField invalid = new IndexField();
                invalid.setName("invalid");
                return List.of(invalid);
            }
        }.map(model, new ResourceType());

        assertEquals(2, mapped.getIndexFields().size());
    }

    @Test
    void mapAllowsSubclassToOverridePrimaryKeyIndexField() {
        Model model = new Model();
        model.setResourceType("sample_entity");
        model.setSections(List.of(section(field("Title", "title", null, FieldType.string, false))));

        ResourceType mapped = new CustomPrimaryKeyMapper().map(model, new ResourceType());

        IndexField primaryKey = mapped.getIndexFields().getFirst();
        assertEquals("resource_internal_id", primaryKey.getName());
        assertEquals("$.identifiers.pid", primaryKey.getPath());
        assertEquals("java.lang.String", primaryKey.getType());
        assertEquals(true, primaryKey.isPrimaryKey());
        assertEquals(mapped, primaryKey.getResourceType());
    }

    @Test
    void mapDoesNotDuplicateModelFieldAlreadyMappedByCustomPrimaryKeyPath() {
        Model model = new Model();
        model.setResourceType("sample_entity");
        UiField pid = field("PID", "pid", "$.identifiers.pid", FieldType.string, false);
        model.setSections(List.of(section(pid)));

        ResourceType mapped = new CustomPrimaryKeyMapper().map(model, new ResourceType());

        assertEquals(1, mapped.getIndexFields().stream().filter(f -> "$.identifiers.pid".equals(f.getPath())).count());
    }

    private void assertHasField(ResourceType resourceType, String name, String path, String type) {
        IndexField indexField = resourceType.getIndexFields().stream()
                .filter(field -> name.equals(field.getName()) && path.equals(field.getPath()))
                .findFirst()
                .orElseThrow();
        assertEquals(type, indexField.getType());
    }

    private Section section(UiField... fields) {
        Section section = new Section();
        section.setName("main");
        section.setFields(List.of(fields));
        return section;
    }

    private UiField field(String label, String id, String accessPath, FieldType fieldType, boolean multiplicity) {
        UiField field = new UiField();
        field.setId(id);
        field.setName(id);
        field.setAccessPath(accessPath);
        field.setLabel(StyledString.of(label));
        TypeInfo typeInfo = new TypeInfo();
        typeInfo.setType(fieldType);
        typeInfo.setMultiplicity(multiplicity);
        field.setTypeInfo(typeInfo);
        return field;
    }

    private static class AdditionalFieldsMapper extends ModelResourceTypeMapper {
        @Override
        protected List<IndexField> additionalIndexFields(Model model, ResourceType resourceType) {
            return List.of(
                    additionalIndexField(resourceType, "catalogueId", "Catalogue ID", "$.catalogueId", String.class.getName(), false),
                    additionalIndexField(resourceType, "active", "Active", "$.active", Boolean.class.getName(), false),
                    additionalIndexField(resourceType, "registeredBy", "Registered By", "$.metadata.registeredBy", String.class.getName(), false)
            );
        }
    }

    private static class CustomPrimaryKeyMapper extends ModelResourceTypeMapper {
        @Override
        protected IndexField primaryKeyIndexField(Model model, ResourceType resourceType) {
            return primaryKeyIndexField(resourceType, "PID", "$.identifiers.pid", String.class.getName());
        }
    }
}
