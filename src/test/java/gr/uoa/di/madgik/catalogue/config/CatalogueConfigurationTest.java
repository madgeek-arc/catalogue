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
import gr.uoa.di.madgik.catalogue.service.DataRequestService;
import gr.uoa.di.madgik.catalogue.service.ModelResponseValidator;
import gr.uoa.di.madgik.catalogue.service.ModelService;
import gr.uoa.di.madgik.catalogue.service.id.IdGenerator;
import gr.uoa.di.madgik.registry.service.GenericResourceService;
import gr.uoa.di.madgik.registry.service.ParserService;
import gr.uoa.di.madgik.registry.service.ResourceService;
import gr.uoa.di.madgik.registry.service.ResourceTypeService;
import gr.uoa.di.madgik.registry.service.ResourceValidator;
import gr.uoa.di.madgik.registry.service.SearchService;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CatalogueConfigurationTest {

    private static final AutoConfigurations AUTO_CONFIGURATIONS =
            AutoConfigurations.of(TaskExecutionAutoConfiguration.class, CatalogueConfiguration.class);

    private static final String BASE_URL = "catalogue-lib.model.validation.base-url=https://vocabularies.example.org";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AUTO_CONFIGURATIONS)
            .withUserConfiguration(RegistryBeans.class)
            .withPropertyValues(BASE_URL);

    private final WebApplicationContextRunner webContextRunner = new WebApplicationContextRunner()
            .withConfiguration(AUTO_CONFIGURATIONS)
            .withUserConfiguration(RegistryBeans.class)
            .withPropertyValues(BASE_URL);

    @Test
    void registersLibraryBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(ModelService.class);
            assertThat(context).hasSingleBean(IdGenerator.class);
            assertThat(context).hasSingleBean(ModelResponseValidator.class);
            assertThat(context).hasSingleBean(DataRequestService.class);
            assertThat(context).hasBean(CatalogueConfiguration.RESOURCE_TYPE_SYNC_EXECUTOR);
        });
    }

    @Test
    void doesNotContributeAnObjectMapper() {
        contextRunner.run(context -> assertThat(context.getBeansOfType(ObjectMapper.class))
                .containsOnlyKeys("objectMapper"));
    }

    @Test
    void keepsTheApplicationTaskExecutor() {
        contextRunner.withUserConfiguration(ExecutorConsumer.class).run(context -> {
            assertThat(context).hasBean("applicationTaskExecutor");
            assertThat(context.getBean(ExecutorHolder.class).executor())
                    .isSameAs(context.getBean("applicationTaskExecutor"));
        });
    }

    @Test
    void resourceTypeSyncExecutorIsSingleThreadedByDefault() {
        contextRunner.run(context -> {
            ThreadPoolTaskExecutor executor =
                    context.getBean(CatalogueConfiguration.RESOURCE_TYPE_SYNC_EXECUTOR, ThreadPoolTaskExecutor.class);
            assertThat(executor.getCorePoolSize()).isEqualTo(1);
            assertThat(executor.getMaxPoolSize()).isEqualTo(1);
            assertThat(executor.getQueueCapacity()).isEqualTo(100);
        });
    }

    @Test
    void resourceTypeSyncExecutorIsConfigurable() {
        contextRunner.withPropertyValues(
                "catalogue-lib.model.resource-type-sync.executor.pool-size=2",
                "catalogue-lib.model.resource-type-sync.executor.queue-capacity=10"
        ).run(context -> {
            ThreadPoolTaskExecutor executor =
                    context.getBean(CatalogueConfiguration.RESOURCE_TYPE_SYNC_EXECUTOR, ThreadPoolTaskExecutor.class);
            assertThat(executor.getMaxPoolSize()).isEqualTo(2);
            assertThat(executor.getQueueCapacity()).isEqualTo(10);
        });
    }

    @Test
    void backsOffToHostBeans() {
        contextRunner.withUserConfiguration(HostOverrides.class).run(context -> {
            assertThat(context).hasSingleBean(ResourceValidator.class);
            assertThat(context).doesNotHaveBean(ModelResponseValidator.class);
            assertThat(context.getBean(ModelService.class)).isSameAs(context.getBean("hostModelService"));
            assertThat(context.getBean(IdGenerator.class)).isSameAs(context.getBean("hostIdGenerator"));
            assertThat(context.getBean(DataRequestService.class)).isSameAs(context.getBean("hostDataRequestService"));
        });
    }

    @Test
    void registersNoWebBeansOutsideAWebApplication() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(FormsController.class);
            assertThat(context).doesNotHaveBean(DataRequestController.class);
            assertThat(context).doesNotHaveBean(GenericExceptionController.class);
        });
    }

    @Test
    void registersWebBeansInAServletApplication() {
        webContextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(FormsController.class);
            assertThat(context).hasSingleBean(DataRequestController.class);
            assertThat(context).hasSingleBean(GenericExceptionController.class);
        });
    }

    @Test
    void webBeansCanBeDisabled() {
        webContextRunner.withPropertyValues(
                "catalogue-lib.web.forms.enabled=false",
                "catalogue-lib.web.data-request.enabled=false",
                "catalogue-lib.web.error-handler.enabled=false"
        ).run(context -> {
            assertThat(context).doesNotHaveBean(FormsController.class);
            assertThat(context).doesNotHaveBean(DataRequestController.class);
            assertThat(context).doesNotHaveBean(GenericExceptionController.class);
        });
    }

    @Test
    void hostExceptionAdviceReplacesTheLibraryOne() {
        webContextRunner.withUserConfiguration(HostExceptionAdvice.class).run(context -> {
            assertThat(context).hasSingleBean(GenericExceptionController.class);
            assertThat(context.getBean(GenericExceptionController.class))
                    .isInstanceOf(HostExceptionAdvice.CustomExceptionController.class);
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class RegistryBeans {

        @Bean
        GenericResourceService genericResourceService() {
            return mock(GenericResourceService.class);
        }

        @Bean
        SearchService searchService() {
            return mock(SearchService.class);
        }

        @Bean
        ResourceService resourceService() {
            return mock(ResourceService.class);
        }

        @Bean
        ResourceTypeService resourceTypeService() {
            return mock(ResourceTypeService.class);
        }

        @Bean
        ParserService parserService() {
            return mock(ParserService.class);
        }

        @Bean
        ObjectMapper objectMapper() {
            return JsonMapper.builder().build();
        }
    }

    record ExecutorHolder(Executor executor) {
    }

    @Configuration(proxyBeanMethods = false)
    static class ExecutorConsumer {

        @Bean
        ExecutorHolder executorHolder(Executor executor) {
            return new ExecutorHolder(executor);
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class HostOverrides {

        @Bean
        ResourceValidator hostValidator() {
            return mock(ResourceValidator.class);
        }

        @Bean
        ModelService hostModelService() {
            return mock(ModelService.class);
        }

        @Bean
        @SuppressWarnings("unchecked")
        IdGenerator<String> hostIdGenerator() {
            return mock(IdGenerator.class);
        }

        @Bean
        DataRequestService hostDataRequestService() {
            return mock(DataRequestService.class);
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class HostExceptionAdvice {

        static class CustomExceptionController extends GenericExceptionController {
            CustomExceptionController(ObjectProvider<Tracer> tracer) {
                super(tracer);
            }
        }

        @Bean
        CustomExceptionController customExceptionController(ObjectProvider<Tracer> tracer) {
            return new CustomExceptionController(tracer);
        }
    }
}
