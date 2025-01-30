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
package gr.uoa.di.madgik.catalogue.ui.config;

import gr.uoa.di.madgik.catalogue.service.GenericResourceService;
import gr.uoa.di.madgik.catalogue.service.id.IdGenerator;
import gr.uoa.di.madgik.catalogue.ui.service.FormDisplayService;
import gr.uoa.di.madgik.catalogue.ui.service.ModelService;
import gr.uoa.di.madgik.catalogue.ui.service.RegistryFormDisplayService;
import gr.uoa.di.madgik.catalogue.ui.service.SimpleFormsService;
import gr.uoa.di.madgik.registry.service.ParserService;
import gr.uoa.di.madgik.registry.service.ResourceService;
import gr.uoa.di.madgik.registry.service.ResourceTypeService;
import gr.uoa.di.madgik.registry.service.SearchService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class UiFieldsSourceConfiguration {

    @Bean
    ModelService simpleUiFieldService(@Qualifier("catalogueGenericResourceService")
                                      GenericResourceService genericResourceService,
                                      SearchService searchService, ResourceService resourceService,
                                      ResourceTypeService resourceTypeService, ParserService parserService,
                                      IdGenerator<String> idGenerator, FormDisplayService formDisplayService) {
        return new SimpleFormsService(genericResourceService, idGenerator, searchService, resourceService,
                resourceTypeService, parserService, formDisplayService);
    }

    @Bean
    FormDisplayService registryFormDisplayService(@Qualifier("catalogueGenericResourceService")
                                                  GenericResourceService genericResourceService) {
        return new RegistryFormDisplayService(genericResourceService);
    }
}
