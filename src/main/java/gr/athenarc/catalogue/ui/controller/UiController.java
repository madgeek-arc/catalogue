package gr.athenarc.catalogue.ui.controller;

import gr.athenarc.catalogue.ClasspathUtils;
import gr.athenarc.catalogue.ui.domain.FieldGroup;
import gr.athenarc.catalogue.ui.domain.GroupedFields;
import gr.athenarc.catalogue.ui.domain.UiField;
import gr.athenarc.catalogue.ui.service.FormsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("ui")
public class UiController {

    private final FormsService formsService;

    @Autowired
    public UiController(FormsService formsService) {
        this.formsService = formsService;
    }

    @GetMapping(value = "{className}/fields/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UiField> createFields(@PathVariable("className") String name) throws ClassNotFoundException {
        return formsService.createFields(name, null);
    }

    @GetMapping(value = "{className}/fields", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UiField> getFields(@PathVariable("className") String name) {
        return formsService.getFields();
    }

    @Deprecated
    @GetMapping(value = "form/model/flat", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GroupedFields<UiField>> getFlatModel() {
        return formsService.getFlatModel();
    }

    @Deprecated
    @GetMapping(value = "form/model", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GroupedFields<FieldGroup>> getModel() {
        return formsService.getModel();
    }

    @GetMapping(value = "form/model/{surveyId}/flat", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GroupedFields<UiField>> getSurveyModelFlat(@PathVariable("surveyId") String surveyId) {
        return formsService.getSurveyModelFlat(surveyId);
    }

    @GetMapping(value = "form/model/{surveyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<GroupedFields<FieldGroup>>> getSurveyModel(@PathVariable("surveyId") String surveyId) {
        return formsService.getSurveyModel(surveyId);
    }


    @GetMapping(value = "vocabularies/map", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<String>> getVocabularies() {
        return getEnumsMap("gr.athenarc.xsd2java");
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
