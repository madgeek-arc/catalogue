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

import gr.uoa.di.madgik.catalogue.controller.DataRequestController;
import gr.uoa.di.madgik.catalogue.controller.FormsController;
import gr.uoa.di.madgik.catalogue.controller.GenericExceptionController;
import gr.uoa.di.madgik.catalogue.domain.Model;
import gr.uoa.di.madgik.catalogue.service.DataRequestService;
import gr.uoa.di.madgik.catalogue.service.DefaultDataRequestService;
import gr.uoa.di.madgik.catalogue.service.DefaultModelService;
import gr.uoa.di.madgik.catalogue.service.ModelResourceTypeMapper;
import gr.uoa.di.madgik.catalogue.service.ModelResponseValidator;
import gr.uoa.di.madgik.catalogue.service.ModelService;
import gr.uoa.di.madgik.catalogue.service.SnippetTemplateCache;
import gr.uoa.di.madgik.catalogue.service.id.IdGenerator;
import gr.uoa.di.madgik.catalogue.service.id.StringIdGenerator;
import gr.uoa.di.madgik.registry.service.GenericResourceService;
import gr.uoa.di.madgik.registry.service.ParserService;
import gr.uoa.di.madgik.registry.service.ResourceService;
import gr.uoa.di.madgik.registry.service.ResourceTypeService;
import gr.uoa.di.madgik.registry.service.ResourceValidator;
import gr.uoa.di.madgik.registry.service.SearchService;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Auto-configuration of the catalogue library: {@link Model} management, model-based resource
 * validation, model to resource type mapping and, in servlet web applications, the library's
 * REST endpoints and exception advice.
 *
 * <p>Every bean except the resource type sync executor backs off when the host application defines
 * a bean of the same type. The web beans can also be switched off individually with
 * {@code catalogue-lib.web.forms.enabled}, {@code catalogue-lib.web.data-request.enabled} and
 * {@code catalogue-lib.web.error-handler.enabled}.
 */
// Runs after the task execution auto-configuration so that the host still gets its default
// application task executor, which is only created while no other Executor bean exists.
@AutoConfiguration(afterName = {
        "gr.uoa.di.madgik.registry.autoconfigure.RegistryServiceAutoConfiguration",
        "org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration"
})
@EnableConfigurationProperties(CatalogueLibProperties.class)
public class CatalogueConfiguration {

    /**
     * Name of the executor that propagates model updates to their mapped resource types.
     */
    public static final String RESOURCE_TYPE_SYNC_EXECUTOR = "catalogueResourceTypeSyncExecutor";

    @Bean
    @ConditionalOnMissingBean(ModelService.class)
    DefaultModelService modelService(GenericResourceService genericResourceService,
                                     SearchService searchService, ResourceService resourceService,
                                     ResourceTypeService resourceTypeService, ParserService parserService,
                                     IdGenerator<String> idGenerator,
                                     ModelResourceTypeMapper modelResourceTypeMapper,
                                     @Qualifier(RESOURCE_TYPE_SYNC_EXECUTOR) Executor resourceTypeSyncExecutor,
                                     CatalogueLibProperties catalogueLibProperties) {
        return new DefaultModelService(genericResourceService, idGenerator, searchService, resourceService,
                resourceTypeService, parserService, modelResourceTypeMapper, resourceTypeSyncExecutor,
                catalogueLibProperties);
    }

    /**
     * Executor that refreshes the mapped resource type of an updated model (and with it the
     * resource type's index fields) without keeping the user's request waiting.
     *
     * <ul>
     *     <li>A single thread by default, so the refreshes of successive updates of a model are
     *     applied in submission order.</li>
     *     <li>When the queue is full the refresh runs on the caller's thread: an update is
     *     delayed rather than dropped.</li>
     *     <li>Not a default autowire candidate, so it is never injected by type into code that
     *     asks for an {@link Executor}.</li>
     * </ul>
     */
    @Bean(name = RESOURCE_TYPE_SYNC_EXECUTOR, defaultCandidate = false)
    ThreadPoolTaskExecutor catalogueResourceTypeSyncExecutor(CatalogueLibProperties catalogueLibProperties) {
        CatalogueLibProperties.ExecutorProperties settings =
                catalogueLibProperties.getModel().getResourceTypeSync().getExecutor();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("catalogue-rt-sync-");
        executor.setCorePoolSize(settings.getPoolSize());
        executor.setMaxPoolSize(settings.getPoolSize());
        executor.setQueueCapacity(settings.getQueueCapacity());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // Resource type updates run with the security context of the user who updated the model.
        executor.setTaskDecorator(DelegatingSecurityContextRunnable::new);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        return executor;
    }

    @Bean
    @ConditionalOnMissingBean(IdGenerator.class)
    StringIdGenerator idGenerator() {
        return new StringIdGenerator();
    }

    @Bean
    @ConditionalOnMissingBean(ModelResourceTypeMapper.class)
    ModelResourceTypeMapper modelResourceTypeMapper() {
        return new ModelResourceTypeMapper();
    }

    /**
     * Validates resources against their {@link Model}. The model service is resolved lazily
     * because it is itself built on services that use this validator.
     */
    @Bean
    @ConditionalOnMissingBean(ResourceValidator.class)
    ModelResponseValidator modelResponseValidator(@Lazy ModelService modelService,
                                                  CatalogueLibProperties catalogueLibProperties,
                                                  ObjectMapper objectMapper) {
        return new ModelResponseValidator(modelService, catalogueLibProperties, objectMapper);
    }

    /**
     * Cache of snippet templates; also listens for model writes to evict stale templates.
     */
    @Bean
    @ConditionalOnMissingBean(SnippetTemplateCache.class)
    SnippetTemplateCache snippetTemplateCache(ModelService modelService, ParserService parserService,
                                              CatalogueLibProperties catalogueLibProperties) {
        return new SnippetTemplateCache(modelService, parserService, catalogueLibProperties);
    }

    @Bean
    @ConditionalOnMissingBean(DataRequestService.class)
    DefaultDataRequestService dataRequestService(CatalogueLibProperties catalogueLibProperties) {
        return new DefaultDataRequestService(catalogueLibProperties);
    }

    /**
     * REST endpoints and exception advice, registered only in servlet web applications.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class CatalogueWebConfiguration {

        @Bean
        @ConditionalOnMissingBean(FormsController.class)
        @ConditionalOnProperty(prefix = "catalogue-lib.web.forms", name = "enabled", matchIfMissing = true)
        FormsController formsController(ModelService modelService) {
            return new FormsController(modelService);
        }

        @Bean
        @ConditionalOnMissingBean(DataRequestController.class)
        @ConditionalOnProperty(prefix = "catalogue-lib.web.data-request", name = "enabled", matchIfMissing = true)
        DataRequestController dataRequestController(DataRequestService dataRequestService) {
            return new DataRequestController(dataRequestService);
        }

        /**
         * Backs off when the host defines its own advice, including a subclass of
         * {@link GenericExceptionController}, so that only one of them handles exceptions.
         */
        @Bean
        @ConditionalOnMissingBean(GenericExceptionController.class)
        @ConditionalOnProperty(prefix = "catalogue-lib.web.error-handler", name = "enabled", matchIfMissing = true)
        GenericExceptionController genericExceptionController(ObjectProvider<Tracer> tracer) {
            return new GenericExceptionController(tracer);
        }
    }
}
