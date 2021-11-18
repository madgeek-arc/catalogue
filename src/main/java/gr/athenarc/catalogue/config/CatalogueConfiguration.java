package gr.athenarc.catalogue.config;

import gr.athenarc.catalogue.ClasspathUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import static gr.athenarc.catalogue.ClasspathUtils.getClassesWithoutInterfaces;

@Configuration
@PropertySource(value = {"classpath:application.properties", "classpath:registry.properties"})
public class CatalogueConfiguration {

    private static final Logger logger = LogManager.getLogger(CatalogueConfiguration.class);

    @Bean
    JAXBContext catalogueJAXBContext() throws JAXBException {
        Class<?>[] classes = ClasspathUtils.classesToArray(getClassesWithoutInterfaces("gr.athenarc.xsd2java"));
        return JAXBContext.newInstance(classes);
    }
}
