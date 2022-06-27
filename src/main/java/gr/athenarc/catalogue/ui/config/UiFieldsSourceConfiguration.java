package gr.athenarc.catalogue.ui.config;

import eu.openminted.registry.core.service.ParserService;
import eu.openminted.registry.core.service.ResourceService;
import eu.openminted.registry.core.service.ResourceTypeService;
import eu.openminted.registry.core.service.SearchService;
import gr.athenarc.catalogue.service.GenericItemService;
import gr.athenarc.catalogue.service.id.IdGenerator;
import gr.athenarc.catalogue.ui.service.*;
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
            name = "ui.elements.json.model.enabled",
            havingValue = "true",
            matchIfMissing = false)
    ModelService jsonFileSavedUiFieldsService(@Value("${ui.elements.json.model.dir}") String jsonDir) {
        return new JsonFileFormsService(jsonDir);
    }

    @Bean
    @Autowired
    @ConditionalOnProperty(
            name = "ui.elements.json.model.enabled",
            havingValue = "false",
            matchIfMissing = true)
    ModelService simpleUiFieldService(@Qualifier("catalogueGenericItemService") GenericItemService genericItemService,
                                      SearchService searchService, ResourceService resourceService,
                                      ResourceTypeService resourceTypeService, ParserService parserService,
                                      IdGenerator<String> idGenerator, FormDisplayService formDisplayService) {
        return new SimpleFormsService(genericItemService, idGenerator, searchService, resourceService, resourceTypeService, parserService, formDisplayService);
    }

    @Bean
    @ConditionalOnProperty(
            name = "ui.elements.json.form-display.enabled",
            havingValue = "true",
            matchIfMissing = false)
    FormDisplayService jsonFileFormDisplayService(@Value("${ui.elements.json.form-display.forms-dir}") String formsDir,
                                                  @Value("${ui.elements.json.form-display.forms-dir}") String displaysDir) {
        return new JsonFileFormDisplayService(formsDir, displaysDir);
    }

    @Bean
    @Autowired
    @ConditionalOnProperty(
            name = "ui.elements.json.form-display.enabled",
            havingValue = "false",
            matchIfMissing = true)
    FormDisplayService registryFormDisplayService(@Qualifier("catalogueGenericItemService") GenericItemService genericItemService) {
        return new RegistryFormDisplayService(genericItemService);
    }
}
