package gr.athenarc.catalogue.config;

import gr.athenarc.catalogue.ClasspathUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.HashSet;
import java.util.Set;

@Configuration
@ComponentScan(value = {
        "eu.openminted.registry.core",
})
@PropertySource(value = {"classpath:application.properties", "classpath:registry.properties"})
public class ServiceConf {

    private static final Logger logger = LogManager.getLogger(ServiceConf.class);

    @Bean
    JAXBContext catalogueJAXBContext() throws JAXBException {
        Class<?>[] classes = ClasspathUtils.classesToArray(getClassesWithoutInterfaces("gr.athenarc.xsd2java"));
        return JAXBContext.newInstance(classes);
    }

    private Set<Class<?>> getClassesWithoutInterfaces(String packageName) {
        Set<Class<?>> allClasses = ClasspathUtils.findAllClasses(packageName);
        Set<Class<?>> classes = new HashSet<>();
        for (Class<?> c : allClasses) {
            if (!c.isInterface()) {
                try {
                    JAXBContext.newInstance(c);
                    classes.add(c);
                } catch (Exception e) {
                    logger.warn("JAXBContext error with class {}" + c.toString(), e);
                }
            }
        }
        return classes;
    }
}
