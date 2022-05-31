package gr.athenarc.catalogue.ui.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.athenarc.catalogue.ui.domain.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JsonFileFormDisplayService implements FormDisplayService {

    private static final Logger logger = LogManager.getLogger(JsonFileFormDisplayService.class);

    @Value("${ui.elements.json.form:}")
    private String formsDirectory;
    @Value("${ui.elements.json.display:}")
    private String displayDirectory;

    public JsonFileFormDisplayService() {
        if ("".equals(formsDirectory)) {
            formsDirectory = "catalogue/uiElements";
            logger.warn("'ui.elements.json.form' was not set. Using default: " + formsDirectory);
        }
        if ("".equals(displayDirectory)) {
            displayDirectory = "catalogue/uiElements";
            logger.warn("'ui.elements.json.display' was not set. Using default: " + displayDirectory);
        }
//        File dir = new File(formsDirectory);
//        if (dir.mkdirs()) {
//            logger.error(String.format("Directory for UI elements has been created: [%s]. Please place the necessary files inside...", dir.getAbsolutePath()));
//        }
    }

    @Override
    public Form getForm(String fieldId) {
        return null;
    }

    @Override
    public Display getDisplay(String fieldId) {
        return null;
    }

    @Override
    public List<UiFieldForm> getForms() {
        return new ArrayList<>(Arrays.asList(readObjects(formsDirectory, UiFieldForm[].class)));
    }

    @Override
    public List<UiFieldDisplay> getDisplays() {
        return new ArrayList<>(Arrays.asList(readObjects(displayDirectory, UiFieldDisplay[].class)));
    }

    @Override
    public Map<String, Form> getUiFieldIdFormMap() {
        return getForms().stream().collect(Collectors.toMap(UiFieldForm::getId, UiFieldForm::getForm));
    }

    @Override
    public Map<String, Display> getUiFieldIdDisplayMap() {
        return getDisplays().stream().collect(Collectors.toMap(UiFieldDisplay::getId, UiFieldDisplay::getDisplay));
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

    protected <T> T[] readObjects(String filepath, Class<T[]> clazz) {
        T[] objects = null;
        try {
            String jsonObject = readFile(filepath);
            ObjectMapper objectMapper = new ObjectMapper();
            objects = objectMapper.readValue(jsonObject, clazz);
        } catch (IOException e) {
            logger.error(e);
        }

        return objects;
    }

    protected List<UiFieldForm> readForms(String filepath) {
        List<UiFieldForm> forms = null;
        try {
            String jsonObject = readFile(filepath);
            ObjectMapper objectMapper = new ObjectMapper();
            UiFieldForm[] sectionsArray = objectMapper.readValue(jsonObject, UiFieldForm[].class);
            forms = new ArrayList<>(Arrays.asList(sectionsArray));
        } catch (IOException e) {
            logger.error(e);
        }

        return forms;
    }

    protected List<UiFieldDisplay> readDisplays(String filepath) {
        List<UiFieldDisplay> displays = null;
        try {
            String jsonObject = readFile(filepath);
            ObjectMapper objectMapper = new ObjectMapper();
            UiFieldDisplay[] sectionsArray = objectMapper.readValue(jsonObject, UiFieldDisplay[].class);
            displays = new ArrayList<>(Arrays.asList(sectionsArray));
        } catch (IOException e) {
            logger.error(e);
        }

        return displays;
    }
}
