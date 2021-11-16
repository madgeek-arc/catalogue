package gr.athenarc.catalogue.ui;

import eu.openminted.registry.core.service.ParserService;
import eu.openminted.registry.core.service.ResourceService;
import eu.openminted.registry.core.service.ResourceTypeService;
import eu.openminted.registry.core.service.SearchService;
import gr.athenarc.catalogue.CatalogueApplication;
import gr.athenarc.catalogue.service.GenericItemService;
import gr.athenarc.catalogue.service.id.IdCreator;
import gr.athenarc.catalogue.ui.domain.*;
import gr.athenarc.catalogue.ui.service.UiFieldsService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes =
        CatalogueApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(
        locations = "classpath:registry.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UiElementsTest {

    @Autowired
    GenericItemService genericItemService;
    @Autowired
    ResourceTypeService resourceTypeService;
    @Autowired
    ParserService parserService;
    @Autowired
    SearchService searchService;
    @Autowired
    ResourceService resourceService;

    @Autowired
    IdCreator<String> stringIdCreator;

    @Autowired
    UiFieldsService uiFieldsService;

    final String fieldId = "f-test";
    final String groupId = "g-test";
    final String surveyId = "s-test";

    @Test
    @Order(1)
    void addField() {
        UiField field = createField();
        uiFieldsService.addField(field);
    }

    @Test
    @Order(2)
    void updateField() {
        UiField field = uiFieldsService.getField(fieldId);
        uiFieldsService.updateField(fieldId, field);
        assert true;
    }

    @Test
    @Order(3)
    void addGroup() {
        Group group = createGroup();
        uiFieldsService.addGroup(group);
    }

    @Test
    @Order(4)
    void updateGroup() {
        Group group = uiFieldsService.getGroup(groupId);
        uiFieldsService.updateGroup(groupId, group);
        assert true;
    }

    @Test
    @Order(5)
    void addSurvey() {
        Survey survey = createSurvey();
        uiFieldsService.addSurvey(survey);
    }

    @Test
    @Order(6)
    void updateSurvey() {
        Survey survey = uiFieldsService.getSurvey(surveyId);
        uiFieldsService.updateSurvey(surveyId, survey);
    }

    @Test
    @Order(7)
    void deleteSurvey() {
        uiFieldsService.deleteSurvey(surveyId);
    }

    @Test
    @Order(8)
    void deleteGroup() {
        uiFieldsService.deleteGroup(groupId);
    }

    @Test
    @Order(9)
    void deleteField() {
        uiFieldsService.deleteField(fieldId);
    }

    UiField createField() {
        UiField field = new UiField();
        field.setParentId(null);
        field.setId(fieldId);
        field.setName("Test Field");
        field.setParent(null);
        field.setIncludedInSnippet(true);
        field.setLabel("Test Label");

        TypeInfo typeInfo = new TypeInfo();
        typeInfo.setType("composite");
        typeInfo.setMultiplicity(true);
        typeInfo.setVocabulary(null);
        List<String> values = new ArrayList<>();
        values.add("item 1");
        values.add("item 2");
        typeInfo.setValues(null);
        field.setTypeInfo(typeInfo);

        Form form = new Form();
        form.setAffects(new ArrayList<>());
        form.setMandatory(true);
        form.setDependsOn(null);
        form.setDescription("Field Description");
        form.setGroup(groupId);
        form.setImmutable(true);
        form.setVisible(true);
        form.setOrder(1);
        form.setPlaceholder("Test placeholder");
        form.setSuggestion("Test Suggestion");
        field.setForm(form);

        Display display = new Display();
        display.setOrder("1");
        display.setPlacement("2");
        field.setDisplay(display);

        return field;
    }

    Group createGroup() {
        Group group = new Group();
        group.setId(groupId);
        group.setName("Test Group");
        group.setDescription("This is a group");
        group.setOrder(1);
        return group;
    }

    Survey createSurvey() {
        Survey survey = new Survey();
        survey.setId(surveyId);
        survey.setName("Test Survey");
        survey.setDescription("This is a survey");
        survey.setCreatedBy(null);
        survey.setCreationDate(new Date());
        survey.setModifiedBy(null);
        survey.setModificationDate(new Date());
        survey.setChapters(null);
        return survey;
    }

}
