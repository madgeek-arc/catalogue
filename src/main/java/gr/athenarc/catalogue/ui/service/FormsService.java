package gr.athenarc.catalogue.ui.service;

import eu.openminted.registry.core.domain.Browsing;
import eu.openminted.registry.core.domain.FacetFilter;
import gr.athenarc.catalogue.exception.ResourceNotFoundException;
import gr.athenarc.catalogue.ui.domain.*;

import javax.xml.bind.annotation.XmlElement;
import java.util.*;

public interface FormsService extends ModelService {

    /**
     * Fields Methods
     */
    UiField addField(UiField field);

    UiField updateField(String id, UiField field) throws ResourceNotFoundException;

    void deleteField(String fieldId) throws ResourceNotFoundException;

    Browsing<UiField> browseFields(FacetFilter filter);

    UiField getField(String id);

    List<UiField> getFields();

    List<UiField> importFields(List<UiField> fields);
    List<UiField> updateFields(List<UiField> fields);

    default void setFormDependsOnName(UiField field) {
        if (field.getForm().getDependsOn() != null && field.getForm().getDependsOn().getId() != null) {
            field.getForm().getDependsOn().setName(this.getField(field.getForm().getDependsOn().getId()).getName());
        }
    }

    /**
     * Sections Methods
     */
    // TODO: refactor crud for fields/Sections
    Section addSection(Section section);

    Section updateSection(String id, Section section);

    void deleteSection(String fieldId) throws ResourceNotFoundException;

    Section getSection(String id);

    List<Section> getSections();

    List<Section> importSections(List<Section> sections);

    List<Section> updateSections(List<Section> sections);


    /**
     * Models Methods
     */

    List<UiField> getFieldsBySection(String sectionId);

    default List<UiField> createFields(String className, String parent) throws ClassNotFoundException {
        List<UiField> fields = new LinkedList<>();
        Class<?> clazz = Class.forName(className);


        if (clazz.getSuperclass().getName().startsWith("gr.athenarc")) {
//        if (clazz.getSuperclass().getTypeName().length() > 1) {
            String name = clazz.getGenericSuperclass().getTypeName();
            if (name.contains("<")) {
                name = name.substring(name.indexOf("<") + 1, name.indexOf(">"));
            }
            List<UiField> subfields = createFields(name, name);
            fields.addAll(subfields);
        }

        if (clazz.getSuperclass().getName().startsWith("gr.athenarc")) {
            String name = clazz.getSuperclass().getName()/*.replaceFirst(".*\\.", "")*/;
            List<UiField> subfields = createFields(name, name);
            fields.addAll(subfields);
        }

        java.lang.reflect.Field[] classFields = clazz.getDeclaredFields();
        for (java.lang.reflect.Field field : classFields) {
            UiField uiField = new UiField();

//            field.setAccessible(true);
            uiField.setName(field.getName());
            uiField.setParent(parent);

            XmlElement annotation = field.getAnnotation(XmlElement.class);

            if (annotation != null) {
                uiField.getForm().setMandatory(!annotation.nillable());

                String type = field.getType().getName();

                if (Collection.class.isAssignableFrom(field.getType())) {
                    uiField.getTypeInfo().setMultiplicity(true);
                    type = field.getGenericType().getTypeName();
                    type = type.substring(type.indexOf("<") + 1, type.indexOf(">"));
                }
                String typeName = type.replaceFirst(".*\\.", "").replaceAll("[<>]", "");
                uiField.getTypeInfo().setType(typeName);

                if (type.startsWith("gr.athenarc")) {
                    List<UiField> subfields = createFields(type, field.getName());
                    fields.addAll(subfields);
                }
            }
            fields.add(uiField);
        }
        return fields;
    }
}
