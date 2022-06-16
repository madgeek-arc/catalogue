package gr.athenarc.catalogue.ui.service;

import gr.athenarc.catalogue.ui.domain.Display;
import gr.athenarc.catalogue.ui.domain.Form;
import gr.athenarc.catalogue.ui.domain.UiFieldDisplay;
import gr.athenarc.catalogue.ui.domain.UiFieldForm;

import java.util.List;
import java.util.Map;

public interface FormDisplayService {

    Form getForm(String fieldId);
    Display getDisplay(String fieldId);

    List<UiFieldForm> getForms();
    List<UiFieldDisplay> getDisplays();

    Map<String, Form> getUiFieldIdFormMap();
    Map<String, Display> getUiFieldIdDisplayMap();

}
