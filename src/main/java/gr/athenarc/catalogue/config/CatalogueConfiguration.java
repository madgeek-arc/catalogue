package gr.athenarc.catalogue.config;

import gr.athenarc.catalogue.utils.ClasspathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

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
