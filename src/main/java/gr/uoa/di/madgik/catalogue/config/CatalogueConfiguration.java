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
package gr.uoa.di.madgik.catalogue.config;

import gr.uoa.di.madgik.catalogue.service.GenericResourceService;
import gr.uoa.di.madgik.catalogue.service.id.IdGenerator;
import gr.uoa.di.madgik.catalogue.service.ModelService;
import gr.uoa.di.madgik.catalogue.service.DefaultModelService;
import gr.uoa.di.madgik.catalogue.utils.ClasspathUtils;
import gr.uoa.di.madgik.registry.service.ParserService;
import gr.uoa.di.madgik.registry.service.ResourceService;
import gr.uoa.di.madgik.registry.service.ResourceTypeService;
import gr.uoa.di.madgik.registry.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

import static gr.uoa.di.madgik.catalogue.utils.ClasspathUtils.getClassesWithoutInterfaces;

@Configuration
@ComponentScan(basePackages = "gr.uoa.di.madgik.catalogue")
@EnableConfigurationProperties(CatalogueLibProperties.class)
@EnableAspectJAutoProxy
public class CatalogueConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(CatalogueConfiguration.class);

    /**
     * Creates the {@link JAXBContext} bean using the classes found in all user-defined packages.
     * See {@link CatalogueLibProperties.JaxbProperties#getAllPackages()}
     */
    @Bean
    JAXBContext jaxbContext(CatalogueLibProperties properties) throws JAXBException {
        logger.info("Creating JAXBContext for classes in the following packages: \n{}",
                String.join("\n", properties.getJaxb().getAllPackages())
        );
        Class<?>[] classes = ClasspathUtils.classesToArray(getClassesWithoutInterfaces(properties.getJaxb().getAllPackages()));
        return JAXBContext.newInstance(classes);
    }

    /**
     *
     * @param genericResourceService
     * @param searchService
     * @param resourceService
     * @param resourceTypeService
     * @param parserService
     * @param idGenerator
     * @return
     */
    @Bean
    ModelService modelService(GenericResourceService genericResourceService,
                              SearchService searchService, ResourceService resourceService,
                              ResourceTypeService resourceTypeService, ParserService parserService,
                              IdGenerator<String> idGenerator) {
        return new DefaultModelService(genericResourceService, idGenerator, searchService, resourceService,
                resourceTypeService, parserService);
    }
}
