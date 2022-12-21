package gr.athenarc.catalogue.ui.config;

import eu.openminted.registry.core.service.ParserService;
import eu.openminted.registry.core.service.ResourceService;
import eu.openminted.registry.core.service.ResourceTypeService;
import eu.openminted.registry.core.service.SearchService;
import gr.athenarc.catalogue.service.GenericItemService;
import gr.athenarc.catalogue.service.id.IdGenerator;
import gr.athenarc.catalogue.ui.service.FormDisplayService;
import gr.athenarc.catalogue.ui.service.ModelService;
import gr.athenarc.catalogue.ui.service.RegistryFormDisplayService;
import gr.athenarc.catalogue.ui.service.SimpleFormsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UiFieldsSourceConfiguration {

    @Bean
    @Autowired
    ModelService simpleUiFieldService(@Qualifier("catalogueGenericItemService") GenericItemService genericItemService,
                                      SearchService searchService, ResourceService resourceService,
                                      ResourceTypeService resourceTypeService, ParserService parserService,
                                      IdGenerator<String> idGenerator, FormDisplayService formDisplayService) {
        return new SimpleFormsService(genericItemService, idGenerator, searchService, resourceService, resourceTypeService, parserService, formDisplayService);
    }

    @Bean
    @Autowired
    FormDisplayService registryFormDisplayService(@Qualifier("catalogueGenericItemService") GenericItemService genericItemService) {
        return new RegistryFormDisplayService(genericItemService);
    }
}
