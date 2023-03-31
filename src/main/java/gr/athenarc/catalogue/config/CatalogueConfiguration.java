package gr.athenarc.catalogue.config;

import gr.athenarc.catalogue.utils.ClasspathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import static gr.athenarc.catalogue.utils.ClasspathUtils.getClassesWithoutInterfaces;

@Configuration
@EnableAspectJAutoProxy
public class CatalogueConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(CatalogueConfiguration.class);

    @Bean
    JAXBContext catalogueJAXBContext(CatalogueLibConfiguration libConf) throws JAXBException {
        logger.info("Creating JAXBContext for classes in package: {}", libConf.generatedClassesPackageName());
        Class<?>[] classes = ClasspathUtils.classesToArray(getClassesWithoutInterfaces(libConf.generatedClassesPackageName()));
        return JAXBContext.newInstance(classes);
    }
}
