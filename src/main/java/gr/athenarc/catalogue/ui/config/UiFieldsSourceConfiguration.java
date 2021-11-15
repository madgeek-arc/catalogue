package gr.athenarc.catalogue.ui.config;

import gr.athenarc.catalogue.service.GenericItemService;
import gr.athenarc.catalogue.service.id.IdCreator;
import gr.athenarc.catalogue.service.id.StringIdCreator;
import gr.athenarc.catalogue.ui.service.JsonFileSavedUiFieldsService;
import gr.athenarc.catalogue.ui.service.SimpleUiFieldService;
import gr.athenarc.catalogue.ui.service.UiFieldsService;
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
    UiFieldsService jsonFileSavedUiFieldsService(@Value("${ui.elements.json.dir}") String jsonDir) {
        return new JsonFileSavedUiFieldsService(jsonDir);
    }

    @Bean
    @ConditionalOnProperty(
            name = "ui.elements.json.enabled",
            havingValue = "false",
            matchIfMissing = true)
    UiFieldsService simpleUiFieldService(GenericItemService genericItemService) {
        return new SimpleUiFieldService(genericItemService, stringIdCreator());
    }

    @Bean
    IdCreator<String> stringIdCreator() {
        return new StringIdCreator();
    }

}
