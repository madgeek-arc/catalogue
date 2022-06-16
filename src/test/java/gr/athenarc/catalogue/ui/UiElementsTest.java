package gr.athenarc.catalogue.ui;

import gr.athenarc.catalogue.ui.domain.*;
import gr.athenarc.catalogue.ui.service.FormsService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UiElementsTest {

    @Autowired
    FormsService formsService;

    final String fieldId = "f-test";
    final String sectionId = "g-test";
    final String modelId = "s-test";

    @Test
    @Order(1)
    void addField() {
        UiField field = createField();
        UiField res = formsService.addField(field);
        assert res.equals(field);
    }

    @Test
    @Order(2)
    void updateField() {
        UiField field = formsService.getField(fieldId);
        UiField res = formsService.updateField(fieldId, field);
        assert res.equals(field);
    }

    @Test
    @Order(3)
    void addSection() {
        Section section = createSection();
        Section res = formsService.addSection(section);
        assert res.equals(section);
    }

    @Test
    @Order(4)
    void updateSection() {
        Section section = formsService.getSection(sectionId);
        Section res = formsService.updateSection(sectionId, section);
        assert res.equals(section);
    }

    @Test
    @Order(5)
    void addModel() {
        Model model = createModel();
        Model res = formsService.add(model);
        assert res.equals(model);
    }

    @Test
    @Order(6)
    void updateModel() {
        Model model = formsService.get(modelId);
        Model res = formsService.update(modelId, model);
        assert res.equals(model);
    }

    @Test
    @Order(7)
    void deleteModel() {
        formsService.delete(modelId);
    }

    @Test
    @Order(8)
    void deleteSection() {
        formsService.deleteSection(sectionId);
    }

    @Test
    @Order(9)
    void deleteField() {
        formsService.deleteField(fieldId);
    }

    UiField createField() {
        UiField field = new UiField();
        field.setParentId(null);
        field.setId(fieldId);
        field.setName("Test Field");
        field.setParent(null);
        field.setIncludedInSnippet(true);

        field.setLabel(StyledString.of("Test Label"));

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
        form.setDescription(StyledString.of("Field Description"));
        form.setGroup(sectionId);
        form.setImmutable(true);
        form.setDisplay(new Display(null, 1, true, false, null, null));
        form.setPlaceholder("Test placeholder");
        form.setSuggestion(StyledString.of("Test Suggestion"));
        field.setForm(form);

        Display display = new Display();
        display.setOrder(1);
        display.setPlacement("2");
        field.setDisplay(display);

        return field;
    }

    Section createSection() {
        Section section = new Section();
        section.setId(sectionId);
        section.setName("Test Section");
        section.setDescription("This is a section");
        section.setOrder(1);
        return section;
    }

    Model createModel() {
        Model model = new Model();
        model.setId(modelId);
        model.setName("Test Model");
        model.setDescription("This is a model");
        model.setCreatedBy(null);
        model.setCreationDate(new Date());
        model.setModifiedBy(null);
        model.setModificationDate(new Date());
        model.setSections(null);
        return model;
    }

}
