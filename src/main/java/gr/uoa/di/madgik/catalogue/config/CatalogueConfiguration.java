/*
 * Copyright 2021-2026 OpenAIRE AMKE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gr.uoa.di.madgik.catalogue.config;

import gr.uoa.di.madgik.catalogue.domain.Model;
import gr.uoa.di.madgik.registry.service.GenericResourceService;
import gr.uoa.di.madgik.catalogue.service.id.IdGenerator;
import gr.uoa.di.madgik.catalogue.service.ModelService;
import gr.uoa.di.madgik.catalogue.service.DefaultModelService;
import gr.uoa.di.madgik.catalogue.service.ModelResourceTypeMapper;
import gr.uoa.di.madgik.registry.service.ParserService;
import gr.uoa.di.madgik.registry.service.ResourceService;
import gr.uoa.di.madgik.registry.service.ResourceTypeService;
import gr.uoa.di.madgik.registry.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

@Configuration
@ComponentScan(basePackages = "gr.uoa.di.madgik.catalogue")
@EnableConfigurationProperties(CatalogueLibProperties.class)
public class CatalogueConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(CatalogueConfiguration.class);

    /**
     * Creates the {@link ModelService} bean backed by {@link DefaultModelService}.
     *
     * <p>{@code ModelService} manages {@link Model} resources —
     * the UI field definitions that describe how catalogue resources are structured and displayed.
     *
     * @param genericResourceService type-safe CRUD over registry resources
     * @param searchService          search backend used for browsing models
     * @param resourceService        low-level resource persistence
     * @param resourceTypeService    resource type metadata and index field resolution
     * @param parserService          JSON/XML serialization of model payloads
     * @param idGenerator            generates unique string IDs for new model resources
     * @return the configured {@link ModelService} instance
     */
    @Bean
    ModelService modelService(GenericResourceService genericResourceService,
                              SearchService searchService, ResourceService resourceService,
                              ResourceTypeService resourceTypeService, ParserService parserService,
                              IdGenerator<String> idGenerator,
                              ModelResourceTypeMapper modelResourceTypeMapper,
                              AsyncTaskExecutor modelUpdateTaskExecutor,
                              CatalogueLibProperties catalogueLibProperties) {
        return new DefaultModelService(genericResourceService, idGenerator, searchService, resourceService,
                resourceTypeService, parserService, modelResourceTypeMapper, modelUpdateTaskExecutor,
                catalogueLibProperties);
    }

    @Bean
    @ConditionalOnMissingBean(ModelResourceTypeMapper.class)
    ModelResourceTypeMapper modelResourceTypeMapper() {
        return new ModelResourceTypeMapper();
    }

    @Bean
    AsyncTaskExecutor modelUpdateTaskExecutor() {
        SimpleAsyncTaskExecutor delegate = new SimpleAsyncTaskExecutor("catalogue-model-update-");
        return new DelegatingSecurityContextAsyncTaskExecutor(delegate);
    }
}
