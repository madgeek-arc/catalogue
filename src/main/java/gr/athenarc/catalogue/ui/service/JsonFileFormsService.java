package gr.athenarc.catalogue.ui.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openminted.registry.core.domain.Browsing;
import eu.openminted.registry.core.domain.FacetFilter;
import gr.athenarc.catalogue.exception.ResourceNotFoundException;
import gr.athenarc.catalogue.ui.domain.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JsonFileFormsService implements FormsService, ModelService {

    private static final Logger logger = LogManager.getLogger(JsonFileFormsService.class);

    private static final String FILENAME_SECTIONS = "sections.json";
    private static final String FILENAME_FIELDS = "fields.json";
    private static final String FILENAME_MODELS = "models.json";

    private final String directory;
    private String jsonObject;

    public JsonFileFormsService(String directory) {
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

    protected List<Section> readSections(String filepath) {
        List<Section> sections = null;
        try {
            jsonObject = readFile(filepath);
            ObjectMapper objectMapper = new ObjectMapper();
            Section[] sectionsArray = objectMapper.readValue(jsonObject, Section[].class);
            sections = new ArrayList<>(Arrays.asList(sectionsArray));
        } catch (IOException e) {
            logger.error(e);
        }

        return sections;
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

    protected List<Model> readModels(String filepath) {
        List<Model> models = null;
        try {
            jsonObject = readFile(filepath);
            ObjectMapper objectMapper = new ObjectMapper();
            Model[] fieldsArray = objectMapper.readValue(jsonObject, Model[].class);
            models = new ArrayList<>(Arrays.asList(fieldsArray));
        } catch (IOException e) {
            logger.error(e);
        }

        return models;
    }

    @Override
    public Section addSection(Section section) {
        throw new UnsupportedOperationException("To add a section contact the administrator.");
    }

    @Override
    public Section updateSection(String id, Section section) {
        throw new UnsupportedOperationException("To update a section contact the administrator.");
    }

    @Override
    public Section getSection(String sectionId) {
        List<Section> allSections = readSections(directory + "/" + FILENAME_SECTIONS);
        for (Section section : allSections) {
            if (section.getId().equals(sectionId)) {
                return section;
            }
        }
        return null;
    }

    @Override
    public void deleteSection(String fieldId) throws ResourceNotFoundException {
        throw new UnsupportedOperationException("To delete a section contact the administrator.");
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
            if (Objects.equals(field.getId(), id)) {
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
    public List<UiField> importFields(List<UiField> fields) {
        throw new UnsupportedOperationException("Please contact the administrator.");
    }

    @Override
    public List<UiField> updateFields(List<UiField> fields) {
        throw new UnsupportedOperationException("Please contact the administrator.");
    }

    @Override
    public List<Section> getSections() {
        return readSections(directory + "/" + FILENAME_SECTIONS);
    }

    @Override
    public List<Section> importSections(List<Section> sections) {
        throw new UnsupportedOperationException("Please contact the administrator.");
    }

    @Override
    public List<Section> updateSections(List<Section> sections) {
        throw new UnsupportedOperationException("Please contact the administrator.");
    }

    @Override
    public List<UiField> getFieldsBySection(String sectionId) {
        List<UiField> allFields = getFields();

        return allFields
                .stream()
                .filter(field -> field.getForm() != null)
                .filter(field -> field.getForm().getGroup() != null)
                .filter(field -> field.getForm().getGroup().equals(sectionId))
                .collect(Collectors.toList());
    }

    private List<UiField> sortFieldsByParentId(List<UiField> fields) {
        List<UiField> sorted = fields.stream().filter(f -> f.getParentId() != null).sorted(Comparator.comparing(UiField::getParentId)).collect(Collectors.toList());
        sorted.addAll(fields.stream().filter(f -> f.getParentId() == null).collect(Collectors.toList()));
        return sorted;
    }

    @Override
    public Model add(Model model) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Model update(String id, Model model) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(String id) throws ResourceNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Model get(String id) {
        List<Model> allModels = readModels(directory + "/" + FILENAME_MODELS);
        for (Model model : allModels) {
            if (Objects.equals(model.getId(), id)) {
                return model;
            }
        }
        return null;
    }

    @Override
    public Browsing<Model> browse(FacetFilter filter) {
        List<Model> allModels = readModels(directory + "/" + FILENAME_MODELS);

        Browsing<Model> models = new Browsing<>();
        models.setFacets(null);
        int to = Math.min(allModels.size(), filter.getFrom() + filter.getQuantity());
        models.setResults(allModels.subList(filter.getFrom(), to));
        models.setTo(to);
        models.setFrom(filter.getFrom());
        return models;
    }
}
