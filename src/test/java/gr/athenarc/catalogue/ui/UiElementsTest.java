package gr.athenarc.catalogue.ui;

import eu.openminted.registry.core.configuration.HibernateConfiguration;
import gr.athenarc.catalogue.CatalogueApplication;
import gr.athenarc.catalogue.config.CatalogueConfiguration;
import gr.athenarc.catalogue.config.RegistryCoreConfiguration;
import gr.athenarc.catalogue.ui.domain.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest(classes = CatalogueApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UiElementsTest {

    final String fieldId = "f-test";
    final String sectionId = "g-test";
    final String modelId = "s-test";

    @Test
    @Order(1)
    void addModel() {
        Model model = createModel();
        assert model != null;
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
        section.setFields(List.of(createField()));
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
        model.setSections(List.of(createSection()));
        return model;
    }

}
