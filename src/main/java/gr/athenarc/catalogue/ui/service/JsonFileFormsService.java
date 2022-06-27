package gr.athenarc.catalogue.ui.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openminted.registry.core.domain.Browsing;
import eu.openminted.registry.core.domain.FacetFilter;
import gr.athenarc.catalogue.exception.ResourceNotFoundException;
import gr.athenarc.catalogue.ui.domain.Model;
import gr.athenarc.catalogue.ui.domain.Section;
import gr.athenarc.catalogue.ui.domain.UiField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class JsonFileFormsService implements ModelService {

    private static final Logger logger = LoggerFactory.getLogger(JsonFileFormsService.class);

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
            logger.error("Directory for UI elements has been created: [{}]. Please place the necessary files inside...", dir.getAbsolutePath());
        }
    }

    protected String readFile(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            logger.error("File [{}] does not exist", file.getAbsolutePath());
        } else if (!file.canRead()) {
            logger.error("File [{}] is not readable", file.getAbsolutePath());
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
            logger.error(e.getMessage(), e);
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
            logger.error(e.getMessage(), e);
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
            logger.error(e.getMessage(), e);
        }

        return models;
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
