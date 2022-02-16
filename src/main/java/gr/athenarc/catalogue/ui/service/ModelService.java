package gr.athenarc.catalogue.ui.service;

import eu.openminted.registry.core.domain.FacetFilter;
import gr.athenarc.catalogue.exception.ResourceNotFoundException;
import gr.athenarc.catalogue.ui.domain.Model;

import java.util.List;

public interface ModelService {

    Model add(Model model);

    Model update(String id, Model model);

    void delete(String id) throws ResourceNotFoundException;

    Model get(String id);

    List<Model> browse(FacetFilter filter);
}
