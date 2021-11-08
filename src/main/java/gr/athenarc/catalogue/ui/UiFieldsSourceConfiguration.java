package gr.athenarc.catalogue.ui;

import gr.athenarc.catalogue.ui.service.JsonFileSavedUiFieldsService;
import gr.athenarc.catalogue.ui.service.SimpleUiFieldService;
import gr.athenarc.catalogue.ui.service.UiFieldsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UiFieldsSourceConfiguration {

//    @Autowired
//    SimpleUiFieldService simpleUiFieldService;
//
//    @Autowired
//    JsonFileSavedUiFieldsService jsonFileSavedUiFieldsService;
//
//    @Bean
//    @ConditionalOnProperty(
//            value="ui.elements.json.dir",
//            matchIfMissing = false)
//    UiFieldsService jsonFileSavedUiFieldsService() {
//        return jsonFileSavedUiFieldsService;
//    }
//
//    @Bean
//    @ConditionalOnProperty(
//            value="ui.elements.json.dir",
//            havingValue = "",
//            matchIfMissing = true)
//    UiFieldsService simpleUiFieldService() {
//        return simpleUiFieldService;
//    }

}
