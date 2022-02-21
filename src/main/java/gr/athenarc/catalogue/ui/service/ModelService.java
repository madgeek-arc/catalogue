package gr.athenarc.catalogue.ui.service;

import eu.openminted.registry.core.domain.Browsing;
import eu.openminted.registry.core.domain.FacetFilter;
import gr.athenarc.catalogue.exception.ResourceNotFoundException;
import gr.athenarc.catalogue.ui.domain.Model;

public interface ModelService {

    Model add(Model model);

    Model update(String id, Model model);

    void delete(String id) throws ResourceNotFoundException;

    Model get(String id);

    Browsing<Model> browse(FacetFilter filter);
}
