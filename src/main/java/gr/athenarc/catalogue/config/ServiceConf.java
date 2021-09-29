package gr.athenarc.catalogue.config;

import eu.openminted.registry.core.domain.Resource;
import org.springframework.context.annotation.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

@Configuration
@ComponentScan(value = {
        "eu.openminted.registry.core",
//        "eu.openminted.registry.core.controllers"
})
@PropertySource(value = {"classpath:application.properties", "classpath:registry.properties"})
public class ServiceConf {

    @Bean
    JAXBContext catalogueJAXBContext() throws JAXBException {
        Class[] classes = {Resource.class, gr.athenarc.xsd2java.Resource.class};
        return JAXBContext.newInstance(classes);

    }
}
