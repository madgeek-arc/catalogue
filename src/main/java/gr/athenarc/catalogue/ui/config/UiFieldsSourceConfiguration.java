package gr.athenarc.catalogue.ui.config;

import eu.openminted.registry.core.service.ParserService;
import eu.openminted.registry.core.service.ResourceService;
import eu.openminted.registry.core.service.ResourceTypeService;
import eu.openminted.registry.core.service.SearchService;
import gr.athenarc.catalogue.service.GenericItemService;
import gr.athenarc.catalogue.service.id.IdCreator;
import gr.athenarc.catalogue.service.id.StringIdCreator;
import gr.athenarc.catalogue.ui.service.JsonFileSavedFormsService;
import gr.athenarc.catalogue.ui.service.SimpleFormsService;
import gr.athenarc.catalogue.ui.service.FormsService;
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
    @ConditionalOnProperty(
            name = "ui.elements.json.enabled",
            havingValue = "false",
            matchIfMissing = true)
    FormsService simpleUiFieldService(GenericItemService genericItemService, SearchService searchService, ResourceService resourceService, ResourceTypeService resourceTypeService, ParserService parserService) {
        return new SimpleFormsService(genericItemService, stringIdCreator(), searchService, resourceService, resourceTypeService, parserService);
    }

    @Bean
    IdCreator<String> stringIdCreator() {
        return new StringIdCreator();
    }

}
