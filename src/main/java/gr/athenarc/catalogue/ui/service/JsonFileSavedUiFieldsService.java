package gr.athenarc.catalogue.ui.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openminted.registry.core.domain.Browsing;
import eu.openminted.registry.core.domain.FacetFilter;
import gr.athenarc.catalogue.ui.domain.FieldGroup;
import gr.athenarc.catalogue.ui.domain.FieldIdName;
import gr.athenarc.catalogue.ui.domain.Group;
import gr.athenarc.catalogue.ui.domain.UiField;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JsonFileSavedUiFieldsService implements UiFieldsService {

    private static final Logger logger = LogManager.getLogger(JsonFileSavedUiFieldsService.class);

    private static final String FILENAME_GROUPS = "groups.json";
    private static final String FILENAME_FIELDS = "fields.json";

    private final String directory;
    private String jsonObject;

    public JsonFileSavedUiFieldsService(String directory) {
        if ("".equals(directory)) {
            directory = "catalogue/uiElements";
            logger.warn("'ui.elements.json.dir' was not set. Using default: " + directory);
        }
        this.directory = directory;
        File dir = new File(directory);
        if (dir.mkdirs()) {
            logger.error(String.format("Directory for UI elements has been created: [%s]. Please place the necessary files inside...", dir.getAbsolutePath()));
        }
    }

    protected String readFile(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            logger.error(String.format("File [%s] does not exist", file.getAbsolutePath()));
        } else if (!file.canRead()) {
            logger.error(String.format("File [%s] is not readable", file.getAbsolutePath()));
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            return sb.toString();
        }
    }

    protected List<Group> readGroups(String filepath) {
        List<Group> groups = null;
        try {
            jsonObject = readFile(filepath);
            ObjectMapper objectMapper = new ObjectMapper();
            Group[] groupsArray = objectMapper.readValue(jsonObject, Group[].class);
            groups = new ArrayList<>(Arrays.asList(groupsArray));
        } catch (IOException e) {
            logger.error(e);
        }

        return groups;
    }

    protected List<UiField> readFields(String filepath) {
        List<UiField> fields = null;
        try {
            jsonObject = readFile(filepath);
            ObjectMapper objectMapper = new ObjectMapper();
            UiField[] fieldsArray = objectMapper.readValue(jsonObject, UiField[].class);
            fields = new ArrayList<>(Arrays.asList(fieldsArray));
        } catch (IOException e) {
            logger.error(e);
        }

        return fields;
    }

    @Override
    public Group addGroup(Group group) {
        throw new UnsupportedOperationException("To add a field group contact the administrator.");
    }

    @Override
    public UiField addField(UiField field) {
        throw new UnsupportedOperationException("To add a field contact the administrator.");
    }

    @Override
    public UiField updateField(String id, UiField field) {
        throw new UnsupportedOperationException("To update a field contact the administrator.");
    }

    @Override
    public void deleteField(String fieldId) {
        throw new UnsupportedOperationException("To delete a field contact the administrator.");
    }

    @Override
    public Browsing<UiField> browseFields(FacetFilter filter) {
        throw new UnsupportedOperationException("Browsing is not supported. Please use getFields() method instead.");
    }

    @Override
    public UiField getField(String id) {
        List<UiField> allFields = readFields(directory + "/" + FILENAME_FIELDS);
        for (UiField field : allFields) {
            if (field.getId() == id) {
                return field;
            }
        }
        return null;
    }

    @Override
    public List<UiField> getFields() { // TODO: refactor
        List<UiField> allFields = readFields(directory + "/" + FILENAME_FIELDS);

        Map<String, UiField> fieldMap = new HashMap<>();
        for (UiField field : allFields) {
            fieldMap.put(field.getId(), field);
        }
        for (UiField f : allFields) {
            if (f.getForm().getDependsOn() != null) {
                // f -> dependsOn
                FieldIdName dependsOn = f.getForm().getDependsOn();

                // affectingField is the field that 'f' dependsOn
                // meaning the field that affects 'f'
                UiField affectingField = fieldMap.get(dependsOn.getId());
                dependsOn.setName(affectingField.getName());

                FieldIdName affects = new FieldIdName(f.getId(), f.getName());
                if (affectingField.getForm().getAffects() == null) {
                    affectingField.getForm().setAffects(new ArrayList<>());
                }
                affectingField.getForm().getAffects().add(affects);

            }
        }

        for (UiField field : allFields) {
            String accessPath = field.getName();
            UiField parentField = field;
            while (parentField != null && parentField.getParent() != null) {
                accessPath = String.join(".", parentField.getParent(), accessPath);
                if (parentField.getParentId() == null) {
                    break;
                }
                parentField = getField(parentField.getParentId());
            }

            field.setAccessPath(accessPath);
        }
        return allFields;
    }

    @Override
    public List<Group> getGroups() {
        return readGroups(directory + "/" + FILENAME_GROUPS);
    }

    @Override
    public List<FieldGroup> createFieldGroups(List<UiField> fields) {
        Map<String, FieldGroup> topLevelFieldGroupMap;
        Set<String> fieldIds = fields.stream().map(UiField::getId).collect(Collectors.toSet());
        topLevelFieldGroupMap = fields
                .stream()
                .filter(Objects::nonNull)
                .filter(field -> field.getParentId() == null)
                .filter(field -> "composite".equals(field.getTypeInfo().getType()))
                .map(FieldGroup::new)
                .collect(Collectors.toMap(f -> (f.getField().getId()), Function.identity()));

        List<UiField> leftOvers = sortFieldsByParentId(fields);

        Map<String, FieldGroup> tempFieldGroups = new HashMap<>();
        tempFieldGroups.putAll(topLevelFieldGroupMap);
        int retries = 0;
        do {
            retries++;
            fields = leftOvers;
            leftOvers = new ArrayList<>();
            for (UiField field : fields) {
                FieldGroup fieldGroup = new FieldGroup(field, new ArrayList<>());

                // Fix problem when Parent ID is defined but Field with that ID is not contained to this group of fields.
                if (!fieldIds.contains(field.getParentId())) {
                    field = new UiField(field);
                    field.setParentId(null);
                }

                if (field.getParentId() == null) {
                    topLevelFieldGroupMap.putIfAbsent(field.getId(), fieldGroup);
                } else if (topLevelFieldGroupMap.containsKey(field.getParentId())) {
                    topLevelFieldGroupMap.get(field.getParentId()).getSubFieldGroups().add(fieldGroup);
                    tempFieldGroups.putIfAbsent(field.getId(), fieldGroup);
                } else if (tempFieldGroups.containsKey(field.getParentId())) {
                    tempFieldGroups.get(field.getParentId()).getSubFieldGroups().add(fieldGroup);
                    tempFieldGroups.putIfAbsent(field.getId(), fieldGroup);
                } else {
                    leftOvers.add(field);
                }
            }

        } while (!leftOvers.isEmpty() && retries < 10);
        return new ArrayList<>(topLevelFieldGroupMap.values());
    }

    private List<UiField> sortFieldsByParentId(List<UiField> fields) {
        List<UiField> sorted = fields.stream().filter(f -> f.getParentId() != null).sorted(Comparator.comparing(UiField::getParentId)).collect(Collectors.toList());
        sorted.addAll(fields.stream().filter(f -> f.getParentId() == null).collect(Collectors.toList()));
        return sorted;
    }
}
