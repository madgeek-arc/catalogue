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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SimpleUiFieldService extends AbstractGenericService<UiField> implements UiFieldsService {

    private static final Logger logger = LogManager.getLogger(SimpleUiFieldService.class);
    private static final String FIELD_RESOURCE_TYPE_NAME = "field";
    private static final String GROUP_RESOURCE_TYPE_NAME = "group";
    private final GenericItemService genericItemService;
    private final IdCreator<String> idCreator;

    public SimpleUiFieldService(GenericItemService genericItemService, IdCreator<String> idCreator) {
        super(UiField.class);
        this.genericItemService = genericItemService;
        this.idCreator = idCreator;
    }

    @Override
    public String getResourceType() {
        return FIELD_RESOURCE_TYPE_NAME;
    }

    @Override
    public UiField addField(UiField field) {
        logger.trace(String.format("adding field: %s", field));
        if (field.getId() == null) {
            field.setId(idCreator.createId("f-"));
        }
        Resource resource = new Resource();
        resource.setResourceTypeName(FIELD_RESOURCE_TYPE_NAME);
        resource.setResourceType(resourceTypeService.getResourceType(FIELD_RESOURCE_TYPE_NAME));
        resource.setPayload(parserPool.serialize(field, ParserService.ParserServiceTypes.JSON));
        resource = resourceService.addResource(resource);
        return parserPool.deserialize(resource, UiField.class);
    }

    @Override
    public UiField updateField(String id, UiField field) throws ResourceNotFoundException {
        logger.trace(String.format("updating field with id [%s] and body: %s", id, field));
        if (!field.getId().equals(id)) {
            throw new ResourceException("You are not allowed to modify the id of a resource.", HttpStatus.CONFLICT);
        }
        Resource existing = null;
        try {
            existing = searchService.searchId(getResourceType(), new SearchService.KeyValue("field_id", id));
        } catch (UnknownHostException e) {
            logger.error(e);
            throw new ResourceNotFoundException(id, FIELD_RESOURCE_TYPE_NAME);
        }
        existing.setPayload(parserPool.serialize(field, ParserService.ParserServiceTypes.JSON));
        Resource resource = resourceService.updateResource(existing);
        return parserPool.deserialize(resource, UiField.class);
    }

    @Override
    public void deleteField(String fieldId) throws ResourceNotFoundException {
        Resource resource = null;
        try {
            resource = searchService.searchId(FIELD_RESOURCE_TYPE_NAME, new SearchService.KeyValue("id", fieldId));
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
        return genericItemService.get(FIELD_RESOURCE_TYPE_NAME, id);
    }

    @Override
    public Browsing<UiField> browseFields(FacetFilter filter) {
        return genericItemService.getResults(filter);
    }

    @Override
    public List<UiField> getFields() {
        FacetFilter ff = new FacetFilter();
        ff.setQuantity(10000);
        ff.setResourceType(FIELD_RESOURCE_TYPE_NAME);
        return browseFields(ff).getResults();
    }

    @Override
    public List<Group> getGroups() {
        FacetFilter ff = new FacetFilter();
        ff.setQuantity(10000);
        ff.setResourceType(GROUP_RESOURCE_TYPE_NAME);
        return (List) genericItemService.getResults(ff).getResults();
    }

    @Override
    public Group addGroup(Group group) {
        logger.trace(String.format("adding field: %s", group));
        if (group.getId() == null) {
            group.setId(idCreator.createId("g-"));
        }
        Resource resource = new Resource();
        resource.setResourceTypeName(GROUP_RESOURCE_TYPE_NAME);
        resource.setResourceType(resourceTypeService.getResourceType(GROUP_RESOURCE_TYPE_NAME));
        resource.setPayload(parserPool.serialize(group, ParserService.ParserServiceTypes.JSON));
        resource = resourceService.addResource(resource);
        return parserPool.deserialize(resource, Group.class);
    }

    // TODO: REWRITE THIS METHOD
    @Override
    public List<FieldGroup> createFieldGroups(List<UiField> fields) {
        Map<String, FieldGroup> topLevelFieldGroupMap;
        Set<String> fieldIds = fields.stream().map(UiField::getId).collect(Collectors.toSet());
        topLevelFieldGroupMap = fields
                .stream()
                .filter(Objects::nonNull)
                .filter(field -> field.getParentId() == null)
                .filter(field -> "composite".equals(field.getTypeInfo().getType()))
                .map(FieldGroup::new)
                .collect(Collectors.toMap(f -> (f.getField().getId()), Function.identity()));

        List<UiField> leftOvers = sortFieldsByParentId(fields);

        Map<String, FieldGroup> tempFieldGroups = new HashMap<>();
        tempFieldGroups.putAll(topLevelFieldGroupMap);
        int retries = 0;
        do {
            retries++;
            fields = leftOvers;
            leftOvers = new ArrayList<>();
            for (UiField field : fields) {
                FieldGroup fieldGroup = new FieldGroup(field, new ArrayList<>());

                // Fix problem when Parent ID is defined but Field with that ID is not contained to this group of fields.
                if (!fieldIds.contains(field.getParentId())) {
                    field = new UiField(field);
                    field.setParentId(null);
                }

                if (field.getParentId() == null) {
                    topLevelFieldGroupMap.putIfAbsent(field.getId(), fieldGroup);
                } else if (topLevelFieldGroupMap.containsKey(field.getParentId())) {
                    topLevelFieldGroupMap.get(field.getParentId()).getSubFieldGroups().add(fieldGroup);
                    tempFieldGroups.putIfAbsent(field.getId(), fieldGroup);
                } else if (tempFieldGroups.containsKey(field.getParentId())) {
                    tempFieldGroups.get(field.getParentId()).getSubFieldGroups().add(fieldGroup);
                    tempFieldGroups.putIfAbsent(field.getId(), fieldGroup);
                } else {
                    leftOvers.add(field);
                }
            }

        } while (!leftOvers.isEmpty() && retries < 10);
        return new ArrayList<>(topLevelFieldGroupMap.values());
    }

    private List<UiField> sortFieldsByParentId(List<UiField> fields) {
        List<UiField> sorted = fields.stream().filter(f -> f.getParentId() != null).sorted(Comparator.comparing(UiField::getParentId)).collect(Collectors.toList());
        sorted.addAll(fields.stream().filter(f -> f.getParentId() == null).collect(Collectors.toList()));
        return sorted;
    }

}
