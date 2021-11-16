package gr.athenarc.catalogue.ui.service;

import eu.openminted.registry.core.domain.Browsing;
import eu.openminted.registry.core.domain.FacetFilter;
import eu.openminted.registry.core.domain.Resource;
import eu.openminted.registry.core.domain.ResourceType;
import eu.openminted.registry.core.service.AbstractGenericService;
import eu.openminted.registry.core.service.ParserService;
import eu.openminted.registry.core.service.SearchService;
import gr.athenarc.catalogue.ReflectUtils;
import gr.athenarc.catalogue.exception.ResourceException;
import gr.athenarc.catalogue.exception.ResourceNotFoundException;
import gr.athenarc.catalogue.service.GenericItemService;
import gr.athenarc.catalogue.service.id.IdCreator;
import gr.athenarc.catalogue.ui.domain.FieldGroup;
import gr.athenarc.catalogue.ui.domain.Group;
import gr.athenarc.catalogue.ui.domain.Survey;
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
    private static final String SURVEY_RESOURCE_TYPE_NAME = "survey";
    private final GenericItemService genericItemService;
    private final IdCreator<String> idCreator;

    public SimpleUiFieldService(GenericItemService genericItemService, IdCreator<String> idCreator) {
        super(UiField.class);
        this.genericItemService = genericItemService;
        this.idCreator = idCreator;
    }

    @Override
    public String getResourceType() {
        return "";
    }

    @Override
    public UiField addField(UiField field) {
        field = add(field, FIELD_RESOURCE_TYPE_NAME);
        return field;
    }

    @Override
    public UiField updateField(String id, UiField field) throws ResourceNotFoundException {
        field = update(id, field, FIELD_RESOURCE_TYPE_NAME);
        return field;
    }

    @Override
    public void deleteField(String fieldId) throws ResourceNotFoundException {
        delete(fieldId, FIELD_RESOURCE_TYPE_NAME);
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
    public Group addGroup(Group group) {
        group = add(group, GROUP_RESOURCE_TYPE_NAME);
        return group;
    }

    @Override
    public Group updateGroup(String id, Group group) {
        group = update(id, group, GROUP_RESOURCE_TYPE_NAME);
        return group;
    }

    @Override
    public void deleteGroup(String groupId) throws ResourceNotFoundException {
        delete(groupId, GROUP_RESOURCE_TYPE_NAME);
    }

    @Override
    public Group getGroup(String id) {
        return genericItemService.get(GROUP_RESOURCE_TYPE_NAME, id);
    }

    @Override
    public List<Group> getGroups() {
        FacetFilter ff = new FacetFilter();
        ff.setQuantity(10000);
        ff.setResourceType(GROUP_RESOURCE_TYPE_NAME);
        return (List) genericItemService.getResults(ff).getResults();
    }

    @Override
    public Survey addSurvey(Survey survey) {
        survey = add(survey, SURVEY_RESOURCE_TYPE_NAME);
        return survey;
    }

    @Override
    public Survey updateSurvey(String id, Survey survey) {
        survey = update(id, survey, SURVEY_RESOURCE_TYPE_NAME);
        return survey;
    }

    @Override
    public void deleteSurvey(String surveyId) throws ResourceNotFoundException {
        delete(surveyId, SURVEY_RESOURCE_TYPE_NAME);
    }

    @Override
    public Survey getSurvey(String id) {
        return genericItemService.get(SURVEY_RESOURCE_TYPE_NAME, id);
    }

    @Override
    public List<Survey> getSurveys() {
        FacetFilter ff = new FacetFilter();
        ff.setQuantity(10000);
        ff.setResourceType(SURVEY_RESOURCE_TYPE_NAME);
        return (List) genericItemService.getResults(ff).getResults();
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

    public <T> T add(T obj, String resourceTypeName) {
        logger.trace(String.format("adding [%s]: %s", resourceTypeName, obj));

        ResourceType resourceType = resourceTypeService.getResourceType(resourceTypeName);

        try {
            if (ReflectUtils.getId(obj.getClass(), obj) == null) {
                ReflectUtils.setId(obj.getClass(), obj, idCreator.createId(resourceTypeName.charAt(0) + "-"));
            }

        } catch (NoSuchFieldException e) {
            logger.error(e);
        }

        Resource resource = new Resource();
        resource.setResourceTypeName(resourceTypeName);
        resource.setResourceType(resourceType);
        resource.setPayload(parserPool.serialize(obj, ParserService.ParserServiceTypes.fromString(resourceType.getPayloadType())));
        resource = resourceService.addResource(resource);
        return (T) parserPool.deserialize(resource, obj.getClass());
    }

    public <T> T update(String id, T obj, String resourceTypeName) {
        logger.trace(String.format("updating [%s] with id [%s] and body: %s", resourceTypeName, id, obj));
        Resource existing = null;
        try {
            if (!id.equals(ReflectUtils.getId(obj.getClass(), obj))) {
                throw new ResourceException("You are not allowed to modify the id of a resource.", HttpStatus.CONFLICT);
            }
            existing = searchService.searchId(resourceTypeName, new SearchService.KeyValue(resourceTypeName + "_id", id));
            existing.setPayload(parserPool.serialize(obj, ParserService.ParserServiceTypes.JSON));
        } catch (NoSuchFieldException e) {
            logger.error(e);
        } catch (UnknownHostException e) {
            logger.error(e);
            throw new ResourceNotFoundException(id, SURVEY_RESOURCE_TYPE_NAME);
        }

        Resource resource = resourceService.updateResource(existing);
        return (T) parserPool.deserialize(resource, obj.getClass());
    }

    public void delete(String id, String resourceTypeName) throws ResourceNotFoundException {
        logger.trace(String.format("deleting [%s] with id [%s]", resourceTypeName, id));
        Resource resource = null;
        try {
            resource = searchService.searchId(resourceTypeName, new SearchService.KeyValue(resourceTypeName + "_id", id));
        } catch (UnknownHostException e) {
            logger.error(e);
        }
        if (resource == null) {
            throw new ResourceNotFoundException();
        } else {
            resourceService.deleteResource(resource.getId());
        }
    }

}
