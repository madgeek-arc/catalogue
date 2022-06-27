package gr.athenarc.catalogue.ui.service;

import gr.athenarc.catalogue.ui.domain.Display;
import gr.athenarc.catalogue.ui.domain.Form;
import gr.athenarc.catalogue.ui.domain.UiFieldDisplay;
import gr.athenarc.catalogue.ui.domain.UiFieldForm;

import java.util.List;
import java.util.Map;

public interface FormDisplayService {

    Form getForm(String modelId, String fieldId);
    UiFieldForm saveForm(String modelId, String fieldId, Form form);
    void deleteForm(String modelId, String fieldId);

    Display getDisplay(String modelId, String fieldId);
    UiFieldDisplay saveDisplay(String modelId, String fieldId, Display display);
    void deleteDisplay(String modelId, String fieldId);


    List<UiFieldForm> getForms();
    List<UiFieldDisplay> getDisplays();

    Map<String, Form> getUiFieldIdFormMap(String modelId);
    Map<String, Display> getUiFieldIdDisplayMap(String modelId);

}
