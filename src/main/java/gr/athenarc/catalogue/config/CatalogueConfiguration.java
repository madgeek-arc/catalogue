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
     * Creates the {@link JAXBContext} (Similar to {@link CatalogueConfiguration#jaxbContext(CatalogueLibProperties)})
     * This bean is created only when a {@link CatalogueLibConfiguration} bean exists.
     * This bean exists to maintain backward compatibility and will be removed in the future.
     */
    @Deprecated
    @Bean
    @ConditionalOnBean(CatalogueLibConfiguration.class)
    JAXBContext catalogueJAXBContext(CatalogueLibConfiguration libConf, CatalogueLibProperties properties) throws JAXBException {
        // Move the 'generatedClassesPackageName' property to the 'includedPackages' property.
        properties.getJaxb().getIncludePackages().add(properties.getJaxb().getGeneratedClassesPackageName());
        // Overwrite the 'generatedClassesPackageName' with the configured
        properties.getJaxb().setGeneratedClassesPackageName(libConf.generatedClassesPackageName());

        logger.info("Creating JAXBContext for classes in the following packages: \n{}",
                String.join("\n", properties.getJaxb().getAllPackages())
        );
        Class<?>[] classes = ClasspathUtils.classesToArray(getClassesWithoutInterfaces(properties.getJaxb().getAllPackages()));
        return JAXBContext.newInstance(classes);
    }

    /**
     * Creates the {@link JAXBContext} bean using the classes found in all user-defined packages.
     * See {@link CatalogueLibProperties.JaxbProperties#getAllPackages()}
     */
    @Bean
    @ConditionalOnMissingBean(CatalogueLibConfiguration.class)
    JAXBContext jaxbContext(CatalogueLibProperties properties) throws JAXBException {
        logger.info("Creating JAXBContext for classes in the following packages: \n{}",
                String.join("\n", properties.getJaxb().getAllPackages())
        );
        Class<?>[] classes = ClasspathUtils.classesToArray(getClassesWithoutInterfaces(properties.getJaxb().getAllPackages()));
        return JAXBContext.newInstance(classes);
    }
}
