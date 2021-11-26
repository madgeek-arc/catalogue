package gr.athenarc.catalogue.ui.service;

import eu.openminted.registry.core.domain.Browsing;
import eu.openminted.registry.core.domain.FacetFilter;
import gr.athenarc.catalogue.exception.ResourceNotFoundException;
import gr.athenarc.catalogue.ui.domain.*;

import javax.xml.bind.annotation.XmlElement;
import java.util.*;
import java.util.stream.Collectors;

public interface FormsService {

    /**
     * Fields Methods
     */
    UiField addField(UiField field);

    UiField updateField(String id, UiField field) throws ResourceNotFoundException;

    void deleteField(String fieldId) throws ResourceNotFoundException;

    Browsing<UiField> browseFields(FacetFilter filter);

    UiField getField(String id);

    List<UiField> getFields();

    /**
     * Groups Methods
     */
    // TODO: refactor crud for fields/groups
    Group addGroup(Group group);

    Group updateGroup(String id, Group group);

    void deleteGroup(String fieldId) throws ResourceNotFoundException;

    Group getGroup(String id);

    List<Group> getGroups();

    /**
     * Survey Methods
     */
    Survey addSurvey(Survey survey);

    Survey updateSurvey(String id, Survey survey);

    void deleteSurvey(String surveyId) throws ResourceNotFoundException;

    Survey getSurvey(String id);

    List<Survey> getSurveys();


    /**
     * Models Methods
     */
    List<FieldGroup> createFieldGroups(List<UiField> fields);

    default List<UiField> getFieldsByGroup(String groupId) {
        List<UiField> allFields = getFields();

        return allFields
                .stream()
                .filter(field -> field.getForm() != null)
                .filter(field -> field.getForm().getGroup() != null)
                .filter(field -> field.getForm().getGroup().equals(groupId))
                .collect(Collectors.toList());
    }

    @Deprecated
    default List<GroupedFields<FieldGroup>> getModel() {
        List<GroupedFields<FieldGroup>> groupedFieldGroups = new ArrayList<>();
        List<GroupedFields<UiField>> groupedFieldsList = getFlatModel();

        for (GroupedFields<UiField> groupedFields : groupedFieldsList) {
            GroupedFields<FieldGroup> groupedFieldGroup = new GroupedFields<>();

            groupedFieldGroup.setGroup(groupedFields.getGroup());
            List<FieldGroup> fieldGroups = createFieldGroups(groupedFields.getFields());
            groupedFieldGroup.setFields(fieldGroups);

            int total = 0;
            for (UiField f : groupedFields.getFields()) {
                if (f.getForm().getMandatory() != null && f.getForm().getMandatory()
                        && f.getTypeInfo().getType() != null && !f.getTypeInfo().getType().equals("composite")) {
                    total += 1;
                }
            }

            int topLevel = 0;
            for (FieldGroup fg : fieldGroups) {
                if (fg.getField().getForm().getMandatory() != null && fg.getField().getForm().getMandatory()) {
                    topLevel += 1;
                }
            }
            RequiredFields requiredFields = new RequiredFields(topLevel, total);
            groupedFieldGroup.setRequired(requiredFields);

            groupedFieldGroups.add(groupedFieldGroup);
        }

        return groupedFieldGroups;
    }

    @Deprecated
    default List<GroupedFields<UiField>> getFlatModel() {
        List<Group> groups = getGroups();
        List<GroupedFields<UiField>> groupedFieldsList = new ArrayList<>();

        if (groups != null) {
            for (Group group : groups) {
                GroupedFields<UiField> groupedFields = new GroupedFields<>();

                groupedFields.setGroup(group);
                groupedFields.setFields(getFieldsByGroup(group.getId()));

                groupedFieldsList.add(groupedFields);
            }
        }

        return groupedFieldsList;
    }

    Map<String, List<GroupedFields<FieldGroup>>> getSurveyModel(String surveyId);

    List<GroupedFields<UiField>> getSurveyModelFlat(String surveyId);

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
