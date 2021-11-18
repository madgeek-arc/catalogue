package gr.athenarc.catalogue.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(value = {"eu.openminted.registry.core"})
public class RegistryCoreConfiguration {
}
