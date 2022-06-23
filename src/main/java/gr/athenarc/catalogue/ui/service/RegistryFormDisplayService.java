package gr.athenarc.catalogue.ui.service;

import gr.athenarc.catalogue.service.GenericItemService;
import gr.athenarc.catalogue.ui.domain.Display;
import gr.athenarc.catalogue.ui.domain.Form;
import gr.athenarc.catalogue.ui.domain.UiFieldDisplay;
import gr.athenarc.catalogue.ui.domain.UiFieldForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class RegistryFormDisplayService implements FormDisplayService {

    private static final Logger logger = LoggerFactory.getLogger(RegistryFormDisplayService.class);

    private final GenericItemService genericItemService;

    public RegistryFormDisplayService(GenericItemService genericItemService) {
        this.genericItemService = genericItemService;
    }

    @Override
    public Form getForm(String fieldId) {
        return genericItemService.get(camelCaseToSnakeCase(Form.class.getName()), fieldId);
    }

    @Override
    public UiFieldForm saveForm(String fieldId, Form form) {
        return null;
    }

    @Override
    public void deleteForm(String fieldId) {

    }

    @Override
    public Display getDisplay(String fieldId) {
        return null;
    }

    @Override
    public UiFieldDisplay saveDisplay(String fieldId, Display display) {
        return null;
    }

    @Override
    public void deleteDisplay(String fieldId) {

    }

    @Override
    public List<UiFieldForm> getForms() {
        return null;
    }

    @Override
    public List<UiFieldDisplay> getDisplays() {
        return null;
    }

    @Override
    public Map<String, Form> getUiFieldIdFormMap() {
        return null;
    }

    @Override
    public Map<String, Display> getUiFieldIdDisplayMap() {
        return null;
    }

    public static String camelCaseToSnakeCase(String str) {
        return str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }
}
