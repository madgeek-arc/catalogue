package gr.athenarc.catalogue.ui.controller;

import gr.athenarc.catalogue.ClasspathUtils;
import gr.athenarc.catalogue.config.CatalogueLibConfiguration;
import gr.athenarc.catalogue.ui.domain.Model;
import gr.athenarc.catalogue.ui.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class UiController {

    private final ModelService modelService;
    private final CatalogueLibConfiguration catalogueLibConfiguration;

    @Autowired
    public UiController(ModelService modelService,
                        CatalogueLibConfiguration catalogueLibConfiguration) {
        this.modelService = modelService;
        this.catalogueLibConfiguration = catalogueLibConfiguration;
    }

    @GetMapping(value = "ui/form/model/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Model getFormModel(@PathVariable("id") String id) {
        return modelService.get(id);
//        return formsService.getSurveyModel(surveyId);
    }


    @GetMapping(value = "ui/vocabularies/map", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<String>> getVocabularies() {
        return getEnumsMap(catalogueLibConfiguration.generatedClassesPackageName());
    }

    private Map<String, List<String>> getEnumsMap(String packageName) {
        Map<String, List<String>> enumsMap = new HashMap<>();
        Set<Class<?>> allClasses = ClasspathUtils.findAllEnums(packageName);
        for (Class<?> c : allClasses) {
            if (c.isEnum()) {
                enumsMap.put(c.getSimpleName(), new ArrayList<>(Arrays.stream(c.getEnumConstants()).map(Object::toString).collect(Collectors.toList())));
            }
        }
        return enumsMap;
    }
}
