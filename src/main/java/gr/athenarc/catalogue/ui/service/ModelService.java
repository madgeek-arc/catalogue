package gr.athenarc.catalogue.ui.service;

import eu.openminted.registry.core.domain.Browsing;
import eu.openminted.registry.core.domain.FacetFilter;
import gr.athenarc.catalogue.exception.ResourceNotFoundException;
import gr.athenarc.catalogue.ui.domain.Model;
import gr.athenarc.catalogue.ui.domain.Section;
import gr.athenarc.catalogue.ui.domain.UiField;

import java.util.ArrayList;
import java.util.List;

public interface ModelService {

    String MODEL_RESOURCE_TYPE_NAME = "model";

    Model add(Model model);

    Model update(String id, Model model);

    void delete(String id) throws ResourceNotFoundException;

    Model get(String id);

    Browsing<Model> browse(FacetFilter filter);

    List<UiField> getAllFields(Model model);

    List<UiField> getSectionFields(Section section);

    List<UiField> getFieldsRecursive(List<UiField> fields);
}
