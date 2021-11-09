package gr.athenarc.catalogue.ui.service;

import eu.openminted.registry.core.domain.Browsing;
import eu.openminted.registry.core.domain.FacetFilter;
import eu.openminted.registry.core.domain.Resource;
import eu.openminted.registry.core.exception.ResourceNotFoundException;
import eu.openminted.registry.core.service.AbstractGenericService;
import eu.openminted.registry.core.service.ParserService;
import eu.openminted.registry.core.service.ResourceCRUDService;
import eu.openminted.registry.core.service.SearchService;
import gr.athenarc.catalogue.service.GenericItemService;
import gr.athenarc.catalogue.ui.domain.FieldGroup;
import gr.athenarc.catalogue.ui.domain.Group;
import gr.athenarc.catalogue.ui.domain.UiField;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.security.core.Authentication;

import java.net.UnknownHostException;
import java.util.List;

public class SimpleUiFieldService extends AbstractGenericService<UiField> implements ResourceCRUDService<UiField, Authentication>, UiFieldsService {

    private static final Logger logger = LogManager.getLogger(SimpleUiFieldService.class);
    private static final String RESOURCE_TYPE_NAME = "field";
    private final GenericItemService genericItemService;

    public SimpleUiFieldService(GenericItemService genericItemService) {
        super(UiField.class);
        this.genericItemService = genericItemService;
    }

    @Override
    public String getResourceType() {
        return RESOURCE_TYPE_NAME;
    }

    @Override
    public UiField get(String id) {
        UiField field = genericItemService.get(RESOURCE_TYPE_NAME, id);
        return field;
    }

    @Override
    public Browsing<UiField> getAll(FacetFilter filter, Authentication authentication) {
        return genericItemService.getResults(filter);
    }

    @Override
    public Browsing<UiField> getMy(FacetFilter filter, Authentication authentication) {
        return getAll(filter, authentication);
    }

    @Override
    public UiField add(UiField field, Authentication authentication) {
        logger.trace(String.format("adding field: %s", field));
        try {
            field.setId(searchService.search(new FacetFilter()).getTotal());
        } catch (UnknownHostException e) {
            logger.error(e);
        }
        Resource resource = new Resource();
        resource.setResourceTypeName(RESOURCE_TYPE_NAME);
        resource.setResourceType(resourceTypeService.getResourceType(RESOURCE_TYPE_NAME));
        resource.setPayload(parserPool.serialize(resource, ParserService.ParserServiceTypes.JSON));
        resource = resourceService.addResource(resource);
        return parserPool.deserialize(resource, UiField.class);
    }

    @Override
    public UiField update(UiField field, Authentication authentication) throws ResourceNotFoundException {
        logger.trace(String.format("updating field: %s", field));
        Resource existing = null;
        try {
            existing = searchService.searchId(getResourceType(), new SearchService.KeyValue("field_id", Integer.toString(field.getId())));
        } catch (UnknownHostException e) {
            logger.error(e);
            throw new ResourceNotFoundException(Integer.toString(field.getId()), RESOURCE_TYPE_NAME);
        }
        existing.setPayload(parserPool.serialize(field, ParserService.ParserServiceTypes.JSON));
        Resource resource = resourceService.addResource(existing);
        return parserPool.deserialize(resource, UiField.class);
    }

    @Override
    public void delete(UiField field) throws ResourceNotFoundException {
        throw new ResourceNotFoundException(Integer.toString(field.getId()), RESOURCE_TYPE_NAME);
    }

    @Override
    public UiField getField(int id) {
        return null;
    }

    @Override
    public List<UiField> getFields() {
        return null;
    }

    @Override
    public List<Group> getGroups() {
        return null;
    }

    @Override
    public List<FieldGroup> createFieldGroups(List<UiField> fields) {
        return null;
    }
}
