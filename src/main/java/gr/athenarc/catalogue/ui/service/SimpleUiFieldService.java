package gr.athenarc.catalogue.ui.service;

import eu.openminted.registry.core.domain.Browsing;
import eu.openminted.registry.core.domain.FacetFilter;
import eu.openminted.registry.core.domain.Resource;
import eu.openminted.registry.core.service.AbstractGenericService;
import eu.openminted.registry.core.service.ParserService;
import eu.openminted.registry.core.service.SearchService;
import gr.athenarc.catalogue.exception.ResourceException;
import gr.athenarc.catalogue.exception.ResourceNotFoundException;
import gr.athenarc.catalogue.service.GenericItemService;
import gr.athenarc.catalogue.service.id.IdCreator;
import gr.athenarc.catalogue.ui.domain.FieldGroup;
import gr.athenarc.catalogue.ui.domain.Group;
import gr.athenarc.catalogue.ui.domain.UiField;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;

import java.net.UnknownHostException;
import java.util.List;

public class SimpleUiFieldService extends AbstractGenericService<UiField> implements UiFieldsService {

    private static final Logger logger = LogManager.getLogger(SimpleUiFieldService.class);
    private static final String RESOURCE_TYPE_NAME = "field";
    private final GenericItemService genericItemService;
    private final IdCreator<String> idCreator;

    public SimpleUiFieldService(GenericItemService genericItemService, IdCreator<String> idCreator) {
        super(UiField.class);
        this.genericItemService = genericItemService;
        this.idCreator = idCreator;
    }

    @Override
    public String getResourceType() {
        return RESOURCE_TYPE_NAME;
    }

    @Override
    public UiField addField(UiField field) {
        logger.trace(String.format("adding field: %s", field));
        if (field.getId() == null) {
            field.setId(idCreator.createId("f-"));
        }
        Resource resource = new Resource();
        resource.setResourceTypeName(RESOURCE_TYPE_NAME);
        resource.setResourceType(resourceTypeService.getResourceType(RESOURCE_TYPE_NAME));
        resource.setPayload(parserPool.serialize(field, ParserService.ParserServiceTypes.JSON));
        resource = resourceService.addResource(resource);
        return parserPool.deserialize(resource, UiField.class);
    }

    @Override
    public UiField updateField(String id, UiField field) throws ResourceNotFoundException {
        logger.trace(String.format("updating field with id [%s] and body: %s", id, field));
        if (field.getId().equals(id)) {
            throw new ResourceException("You are not allowed to modify the id of a resource.", HttpStatus.CONFLICT);
        }
        Resource existing = null;
        try {
            existing = searchService.searchId(getResourceType(), new SearchService.KeyValue("field_id", id));
        } catch (UnknownHostException e) {
            logger.error(e);
            throw new ResourceNotFoundException(id, RESOURCE_TYPE_NAME);
        }
        existing.setPayload(parserPool.serialize(field, ParserService.ParserServiceTypes.JSON));
        Resource resource = resourceService.addResource(existing);
        return parserPool.deserialize(resource, UiField.class);
    }

    @Override
    public void deleteField(String fieldId) throws ResourceNotFoundException {
        Resource resource = null;
        try {
            resource = searchService.searchId(RESOURCE_TYPE_NAME, new SearchService.KeyValue("id", fieldId));
        } catch (UnknownHostException e) {
            logger.error(e);
        }
        if (resource == null) {
            throw new ResourceNotFoundException();
        } else {
            resourceService.deleteResource(resource.getId());
        }
    }

    @Override
    public UiField getField(String id) {
        return genericItemService.get(RESOURCE_TYPE_NAME, id);
    }

    @Override
    public Browsing<UiField> browseFields(FacetFilter filter) {
        return genericItemService.getResults(filter);
    }

    @Override
    public List<UiField> getFields() {
        FacetFilter ff = new FacetFilter();
        ff.setQuantity(10000);
        ff.setResourceType(RESOURCE_TYPE_NAME);
        return browseFields(ff).getResults();
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
