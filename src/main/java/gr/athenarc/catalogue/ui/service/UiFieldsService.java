package gr.athenarc.catalogue.ui.service;

import gr.athenarc.catalogue.ui.domain.*;

import java.util.*;
import java.util.stream.Collectors;

public interface UiFieldsService {

    default List<UiField> createFields(String className, String group) throws ClassNotFoundException {
        List<UiField> fields = new LinkedList<>();
        Class<?> clazz = Class.forName(className);


        if (clazz.getSuperclass().getName().startsWith("eu.einfracentral.domain.Bundle")) {
            String name = clazz.getGenericSuperclass().getTypeName();
            name = name.replaceFirst(".*\\.", "").replace(">", "");
            List<UiField> subfields = createFields(name, name);
            fields.addAll(subfields);
        }

        if (clazz.getSuperclass().getName().startsWith("eu.einfracentral.domain")) {
            String name = clazz.getSuperclass().getName().replaceFirst(".*\\.", "");
            List<UiField> subfields = createFields(name, name);
            fields.addAll(subfields);
        }

        java.lang.reflect.Field[] classFields = clazz.getDeclaredFields();
        for (java.lang.reflect.Field field : classFields) {
            UiField uiField = new UiField();

//            field.setAccessible(true);
            uiField.setName(field.getName());
//            uiField.setParent(parent);
//
//            XmlElement annotation = field.getAnnotation(XmlElement.class);
//
//            if (annotation != null) {
//                uiField.getForm().setMandatory(!annotation.nillable());
//
//                if (annotation.containsId() && Vocabulary.class.equals(annotation.idClass())) {
//                    VocabularyValidation vvAnnotation = field.getAnnotation(VocabularyValidation.class);
//                    if (vvAnnotation != null) {
//                        uiField.getForm().setVocabulary(vvAnnotation.type().getKey());
//                    }
//                    uiField.setType("VOCABULARY");
//                } else if (!field.getType().getName().contains("eu.einfracentral.domain.Identifiable")) {
//                    String type = field.getType().getName();
//
//                    if (Collection.class.isAssignableFrom(field.getType())) {
//                        uiField.setMultiplicity(true);
//                        type = field.getGenericType().getTypeName();
//                        type = type.replaceFirst(".*<", "");
//                        type = type.substring(0, type.length() - 1);
//                    }
//                    String typeName = type.replaceFirst(".*\\.", "").replaceAll("[<>]", "");
//                    uiField.setType(typeName);
//
//                    if (type.startsWith("gr.athenarc")) {
////                        uiField.getForm().setSubgroup(typeName);
//                        List<UiField> subfields = createFields(typeName, field.getName());
//                        fields.addAll(subfields);
//                    }
//                }
//
//            }
            fields.add(uiField);
        }
        return fields;
    }

    UiField getField(int id);

    List<UiField> getFields();

    List<Group> getGroups();

    default List<FieldGroup> createFieldGroups(List<UiField> fields) {
        Map<Integer, FieldGroup> fieldGroupMap = new HashMap<>();
        Map<Integer, List<FieldGroup>> groups = new HashMap<>();
        Set<Integer> ids = fields
                .stream()
                .map(UiField::getParentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (Integer id : ids) {
            groups.put(id, new ArrayList<>());
        }

        for (Iterator<UiField> it = fields.iterator(); it.hasNext(); ) {
            UiField field = it.next();
            FieldGroup fieldGroup = new FieldGroup(field);
            if (ids.contains(field.getParentId())) {
                groups.get(field.getParentId()).add(fieldGroup);
            } else {
                fieldGroupMap.put(field.getId(), fieldGroup);
            }
        }

        for (Map.Entry<Integer, List<FieldGroup>> entry : groups.entrySet()) {
            fieldGroupMap.get(entry.getKey()).setSubFieldGroups(entry.getValue());
        }


        return new ArrayList<>(fieldGroupMap.values());

    }

    default List<UiField> getFieldsByGroup(String groupId) {
        List<UiField> allFields = getFields();

        return allFields
                .stream()
                .filter(field -> field.getForm() != null)
                .filter(field -> field.getForm().getGroup() != null)
                .filter(field -> field.getForm().getGroup().equals(groupId))
                .collect(Collectors.toList());
    }

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
                        && f.getType() != null && !f.getType().equals("composite")) {
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

}
