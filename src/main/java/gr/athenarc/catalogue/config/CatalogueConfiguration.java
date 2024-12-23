/**
 * Copyright 2021-2024 OpenAIRE AMKE
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

package gr.athenarc.catalogue.config;

import gr.athenarc.catalogue.utils.ClasspathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

import static gr.athenarc.catalogue.utils.ClasspathUtils.getClassesWithoutInterfaces;

@Configuration
@ComponentScan(basePackages = "gr.athenarc.catalogue")
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
}
