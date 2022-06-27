package gr.athenarc.catalogue.ui.service;

import gr.athenarc.catalogue.ui.domain.UiField;

import javax.xml.bind.annotation.XmlElement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public interface FormsService extends ModelService {

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
