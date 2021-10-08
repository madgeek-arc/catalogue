package gr.athenarc.catalogue.ui.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.athenarc.catalogue.ui.domain.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Component
public class JsonFileSavedUiFieldsService implements UiFieldsService {

    private static final Logger logger = LogManager.getLogger(JsonFileSavedUiFieldsService.class);

    private static final String FILENAME_GROUPS = "groups.json";
    private static final String FILENAME_FIELDS = "fields.json";

    private final String directory;
    private String jsonObject;

    @Autowired
    public JsonFileSavedUiFieldsService(@Value("${ui.elements.json.dir}") String directory) {
        if ("".equals(directory)) {
            directory = "catalogue/uiElements";
            logger.warn("'ui.elements.json.dir' was not set. Using default: " + directory);
        }
        this.directory = directory;
        File dir = new File(directory);
        if (dir.mkdirs()) {
            logger.error("Directory for UI elements has been created. Please place the necessary files inside...");
        }
    }

    protected String readFile(String filename) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
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
    public UiField getField(int id) {
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

        Map<Integer, UiField> fieldMap = new HashMap<>();
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
            int counter = 0;
            while (parentField.getParent() != null && counter < allFields.size()) {
                counter++;
                accessPath = String.join(".", parentField.getParent(), accessPath);
                for (UiField temp : allFields) {
                    if (temp.getName().equals(parentField.getParent())) {
                        parentField = temp;
                        break;
                    }
                }
            }
            if (counter >= allFields.size()) {
                throw new RuntimeException("The json model located at '" + directory + "/" + FILENAME_FIELDS +
                        "' contains errors in the 'parent' fields...\nPlease fix it and try again.");
            }

            // FIXME: Check if this is needed
            accessPath = accessPath.replaceFirst("\\w+\\.", "");

            field.setAccessPath(accessPath);
        }
        return allFields;
    }

    @Override
    public List<Group> getGroups() {
        return readGroups(directory + "/" + FILENAME_GROUPS);
    }
}
