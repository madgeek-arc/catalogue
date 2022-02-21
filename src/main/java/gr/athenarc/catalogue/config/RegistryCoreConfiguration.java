package gr.athenarc.catalogue.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan(value = {"eu.openminted.registry.core"})
@PropertySource(value = {"classpath:application.properties", "classpath:registry.properties"})
public class RegistryCoreConfiguration {
}
