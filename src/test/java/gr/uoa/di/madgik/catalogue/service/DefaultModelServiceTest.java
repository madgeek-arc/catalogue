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
import gr.uoa.di.madgik.catalogue.domain.UiField;
import gr.uoa.di.madgik.catalogue.service.id.StringIdGenerator;
import gr.uoa.di.madgik.registry.domain.Resource;
import gr.uoa.di.madgik.registry.domain.ResourceType;
import gr.uoa.di.madgik.registry.service.GenericResourceService;
import gr.uoa.di.madgik.registry.service.ParserService;
import gr.uoa.di.madgik.registry.service.ResourceService;
import gr.uoa.di.madgik.registry.service.ResourceTypeService;
import gr.uoa.di.madgik.registry.service.SearchService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultModelServiceTest {

    @Test
    void reportsFieldsMarkedAsFacetsButNotIndexed() {
        UiField contradictory = field("a", false, true);
        UiField notIndexed = field("b", false, null);
        UiField noFacet = field("c", true, false);
        UiField plain = field("d", true, null);

        assertEquals(List.of("a"), DefaultModelService.facetsNotIndexed(List.of(contradictory, notIndexed, noFacet, plain)));
    }

    @Test
    void updateReturnsBeforeTheMappedResourceTypeIsUpdated() {
        GenericResourceService genericResourceService = mock(GenericResourceService.class);
        ResourceTypeService resourceTypeService = mock(ResourceTypeService.class);
        ParserService parserService = mock(ParserService.class);
        ModelResourceTypeMapper mapper = mock(ModelResourceTypeMapper.class);
        CatalogueLibProperties properties = new CatalogueLibProperties();
        properties.getModel().getResourceTypeSync().setOnUpdate(true);
        List<Runnable> submitted = new ArrayList<>();
        Executor capturingExecutor = submitted::add;

        ResourceType mapped = new ResourceType();
        when(genericResourceService.searchResource(eq(ModelService.MODEL_RESOURCE_TYPE_NAME), eq("m-1"), eq(true)))
                .thenReturn(new Resource());
        when(resourceTypeService.getResourceType("service")).thenReturn(new ResourceType());
        when(mapper.map(any(), any())).thenReturn(mapped);

        DefaultModelService service = new DefaultModelService(genericResourceService, new StringIdGenerator(),
                mock(SearchService.class), mock(ResourceService.class), resourceTypeService, parserService,
                mapper, capturingExecutor, properties);

        Model model = new Model();
        model.setId("m-1");
        model.setResourceType("service");
        model.setSections(new ArrayList<>());

        service.update("m-1", model);

        assertEquals(1, submitted.size());
        verify(resourceTypeService, never()).updateResourceType(any());

        submitted.getFirst().run();

        verify(resourceTypeService).updateResourceType(mapped);
    }

    private static UiField field(String id, boolean index, Boolean facet) {
        UiField field = new UiField();
        field.setId(id);
        field.setIndex(index);
        field.setFacet(facet);
        return field;
    }
}
