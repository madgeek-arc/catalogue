package gr.athenarc.catalogue.config;

import org.springframework.context.annotation.*;

@Configuration
@ComponentScan(value = {
        "eu.openminted.registry.core",
//        "eu.openminted.registry.core.controllers"
})
@PropertySource(value = {"classpath:application.properties", "classpath:registry.properties"})
public class ServiceConf {
}
