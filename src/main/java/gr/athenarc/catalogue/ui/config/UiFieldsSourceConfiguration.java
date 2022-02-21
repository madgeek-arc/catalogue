package gr.athenarc.catalogue.ui.config;

import eu.openminted.registry.core.service.ParserService;
import eu.openminted.registry.core.service.ResourceService;
import eu.openminted.registry.core.service.ResourceTypeService;
import eu.openminted.registry.core.service.SearchService;
import gr.athenarc.catalogue.service.GenericItemService;
import gr.athenarc.catalogue.service.id.IdGenerator;
import gr.athenarc.catalogue.ui.service.FormsService;
import gr.athenarc.catalogue.ui.service.JsonFileSavedFormsService;
import gr.athenarc.catalogue.ui.service.SimpleFormsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UiFieldsSourceConfiguration {

    @Bean
    @ConditionalOnProperty(
            name = "ui.elements.json.enabled",
            havingValue = "true",
            matchIfMissing = false)
    FormsService jsonFileSavedUiFieldsService(@Value("${ui.elements.json.dir}") String jsonDir) {
        return new JsonFileSavedFormsService(jsonDir);
    }

    @Bean
    @Autowired
    @ConditionalOnProperty(
            name = "ui.elements.json.enabled",
            havingValue = "false",
            matchIfMissing = true)
    FormsService simpleUiFieldService(@Qualifier("catalogueGenericItemService") GenericItemService genericItemService,
                                      SearchService searchService, ResourceService resourceService,
                                      ResourceTypeService resourceTypeService, ParserService parserService,
                                      IdGenerator<String> idGenerator) {
        return new SimpleFormsService(genericItemService, idGenerator, searchService, resourceService, resourceTypeService, parserService);
    }
}
