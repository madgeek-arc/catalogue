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

import gr.uoa.di.madgik.catalogue.config.CatalogueLibProperties;
import gr.uoa.di.madgik.catalogue.domain.Model;
import gr.uoa.di.madgik.catalogue.domain.Section;
import gr.uoa.di.madgik.catalogue.domain.UiField;
import gr.uoa.di.madgik.registry.domain.FacetFilter;
import gr.uoa.di.madgik.registry.domain.Paging;
import gr.uoa.di.madgik.registry.domain.Resource;
import gr.uoa.di.madgik.registry.domain.ResourceType;
import gr.uoa.di.madgik.registry.service.ParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Each model includes one field in snippets, named after the model, so the field a template keeps identifies the
 * model it was built from.
 */
class SnippetTemplateCacheTest {

    private static final Instant EARLIER = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant LATER = Instant.parse("2026-06-01T00:00:00Z");

    private final ModelService models = mock(ModelService.class);
    private final ParserService parser = mock(ParserService.class);
    private final CatalogueLibProperties properties = new CatalogueLibProperties();
    private final SnippetTemplateCache templates = new SnippetTemplateCache(models, parser, properties);
    /**
     * The models a search finds; a search may not reflect a write yet.
     */
    private final List<Model> indexed = new ArrayList<>();
    private Runnable duringSearch = () -> {
    };

    @BeforeEach
    void setUp() {
        when(models.browse(any())).thenAnswer(call -> {
            FacetFilter filter = call.getArgument(0);
            duringSearch.run();
            List<Model> found = indexed.stream()
                    .filter(model -> model.getResourceType().equals(filter.getFilter().get("resourceType")))
                    .toList();
            return new Paging<>(found.size(), 0, found.size(), found, List.of());
        });
    }

    @Test
    void templatesAreBuiltFromTheModelDefiningTheResourceTypeAndCached() {
        indexed.add(model("m-service", "service", EARLIER));
        indexed.add(model("m-provider", "provider", EARLIER));

        assertEquals(Optional.of(Set.of("m-service")), fieldsKept("service"));
        assertEquals(Optional.of(Set.of("m-service")), fieldsKept("service"));
        assertEquals(Optional.of(Set.of("m-provider")), fieldsKept("provider"));

        verify(models, times(2)).browse(any());
    }

    @Test
    void theMostRecentlyModifiedModelDefinesTheTemplateThenTheSmallestId() {
        indexed.add(model("m-b", "service", EARLIER));
        indexed.add(model("m-c", "service", LATER));
        indexed.add(model("m-a", "service", EARLIER));
        indexed.add(model("m-d", "provider", EARLIER));
        indexed.add(model("m-e", "provider", null));
        indexed.add(model("m-f", "provider", EARLIER));

        assertEquals(Optional.of(Set.of("m-c")), fieldsKept("service"));
        assertEquals(Optional.of(Set.of("m-d")), fieldsKept("provider"));
    }

    @Test
    void aResourceTypeWithoutAModelHasNoTemplateUntilOneIsAdded() {
        assertEquals(Optional.empty(), templates.find("service"));
        assertEquals(Optional.empty(), templates.find("service"));
        verify(models, times(1)).browse(any());

        Model added = model("m-service", "service", EARLIER);
        templates.resourceAdded(resource(added));

        assertEquals(Optional.of(Set.of("m-service")), fieldsKept("service"));
    }

    @Test
    void templatesAreRebuiltOnceTheyExpire() {
        properties.getModel().setSnippetTemplateTtl(Duration.ZERO);
        indexed.add(model("m-service", "service", EARLIER));

        templates.find("service");
        templates.find("service");

        verify(models, times(2)).browse(any());
    }

    @Test
    void anUpdatedModelReplacesTheTemplateBeforeSearchesReflectIt() {
        indexed.add(model("m-service", "service", EARLIER));
        templates.find("service");

        Model updated = model("m-service", "service", LATER, "m-service-v2");
        templates.resourceUpdated(resource(indexed.getFirst()), resource(updated));

        assertEquals(Optional.of(Set.of("m-service-v2")), fieldsKept("service"));
    }

    @Test
    void aModelMovedToAnotherResourceTypeLeavesThePreviousOneWithoutATemplate() {
        Model before = model("m-service", "service", EARLIER);
        indexed.add(before);
        templates.find("service");

        Model after = model("m-service", "datasource", LATER);
        templates.resourceUpdated(resource(before), resource(after));

        assertEquals(Optional.empty(), templates.find("service"));
        assertEquals(Optional.of(Set.of("m-service")), fieldsKept("datasource"));
    }

    @Test
    void aDeletedModelLeavesItsResourceTypeWithoutATemplateBeforeSearchesReflectIt() {
        Model deleted = model("m-service", "service", EARLIER);
        indexed.add(deleted);
        templates.find("service");

        templates.resourceDeleted(resource(deleted));

        assertEquals(Optional.empty(), templates.find("service"));
    }

    @Test
    void aTemplateBuiltWhileAModelIsWrittenIsRebuiltOnItsNextUse() {
        indexed.add(model("m-service", "service", EARLIER));
        ResourceType modelType = new ResourceType();
        modelType.setName(ModelService.MODEL_RESOURCE_TYPE_NAME);
        duringSearch = () -> templates.resourceChangedType(new Resource(), new Resource(), null, modelType);

        templates.find("service");
        duringSearch = () -> {
        };
        templates.find("service");
        templates.find("service");

        verify(models, times(2)).browse(any());
    }

    @Test
    void writesOfOtherResourcesLeaveTemplatesCached() {
        indexed.add(model("m-service", "service", EARLIER));
        templates.find("service");

        Resource service = new Resource();
        service.setResourceTypeName("service");
        templates.resourceAdded(service);
        templates.resourceUpdated(service, service);
        templates.resourceDeleted(service);
        templates.find("service");

        verify(models, times(1)).browse(any());
        verify(parser, never()).deserialize(any(Resource.class), eq(Model.class));
    }

    private Optional<Set<String>> fieldsKept(String resourceType) {
        Map<String, Object> payload = Map.of("m-a", 1, "m-b", 1, "m-c", 1, "m-d", 1, "m-e", 1, "m-f", 1,
                "m-service", 1, "m-service-v2", 1, "m-provider", 1);
        return templates.find(resourceType).map(template -> template.apply(payload).keySet());
    }

    private Resource resource(Model model) {
        Resource resource = new Resource();
        resource.setId(UUID.randomUUID().toString());
        resource.setResourceTypeName(ModelService.MODEL_RESOURCE_TYPE_NAME);
        when(parser.deserialize(resource, Model.class)).thenReturn(model);
        return resource;
    }

    private static Model model(String id, String resourceType, Instant modified) {
        return model(id, resourceType, modified, id);
    }

    private static Model model(String id, String resourceType, Instant modified, String snippetField) {
        UiField field = new UiField();
        field.setName(snippetField);
        field.setIncludedInSnippet(true);
        Section section = new Section();
        section.setFields(List.of(field));
        Model model = new Model();
        model.setId(id);
        model.setResourceType(resourceType);
        model.setModificationDate(modified);
        model.setSections(List.of(section));
        return model;
    }
}
