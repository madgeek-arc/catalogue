package gr.athenarc.catalogue.config;

import gr.athenarc.catalogue.ClasspathUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import static gr.athenarc.catalogue.ClasspathUtils.getClassesWithoutInterfaces;

@Configuration
@EnableAspectJAutoProxy
public class CatalogueConfiguration {

    private static final Logger logger = LogManager.getLogger(CatalogueConfiguration.class);

    @Bean
    JAXBContext catalogueJAXBContext(CatalogueLibConfiguration libConf) throws JAXBException {
        logger.info("Creating JAXBContext for classes in package: " + libConf.generatedClassesPackageName());
        Class<?>[] classes = ClasspathUtils.classesToArray(getClassesWithoutInterfaces(libConf.generatedClassesPackageName()));
        return JAXBContext.newInstance(classes);
    }
}
