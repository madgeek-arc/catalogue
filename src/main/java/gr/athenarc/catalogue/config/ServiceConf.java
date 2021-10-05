package gr.athenarc.catalogue.config;

import eu.openminted.registry.core.controllers.ResourceSyncController;
import gr.athenarc.catalogue.ClasspathUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.context.annotation.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.HashSet;
import java.util.Set;

@Configuration
@ComponentScan(value = {
        "eu.openminted.registry.core",
}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ResourceSyncController.class)
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
        logger.info("Classes found in '" + packageName + "': " + allClasses.size());
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
