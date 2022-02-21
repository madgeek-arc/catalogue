package gr.athenarc.catalogue.ui.service;

import eu.openminted.registry.core.domain.Browsing;
import eu.openminted.registry.core.domain.FacetFilter;
import eu.openminted.registry.core.domain.Resource;
import eu.openminted.registry.core.domain.ResourceType;
import eu.openminted.registry.core.service.ParserService;
import eu.openminted.registry.core.service.ResourceService;
import eu.openminted.registry.core.service.ResourceTypeService;
import eu.openminted.registry.core.service.SearchService;
import gr.athenarc.catalogue.LoggingUtils;
import gr.athenarc.catalogue.ReflectUtils;
import gr.athenarc.catalogue.exception.ResourceException;
import gr.athenarc.catalogue.exception.ResourceNotFoundException;
import gr.athenarc.catalogue.service.GenericItemService;
import gr.athenarc.catalogue.service.id.IdGenerator;
import gr.athenarc.catalogue.ui.domain.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SimpleFormsService implements FormsService, ModelService {

    private static final Logger logger = LogManager.getLogger(SimpleFormsService.class);
    private static final String FIELD_RESOURCE_TYPE_NAME = "field";
    private static final String GROUP_RESOURCE_TYPE_NAME = "group";
    private static final String SURVEY_RESOURCE_TYPE_NAME = "survey";
    private static final String MODEL_RESOURCE_TYPE_NAME = "model";
    private final GenericItemService genericItemService;
    private final IdGenerator<String> idGenerator;
    public final SearchService searchService;
    public final ResourceService resourceService;
    public final ResourceTypeService resourceTypeService;
    public final ParserService parserPool;

    public SimpleFormsService(GenericItemService genericItemService,
                              IdGenerator<String> idGenerator,
                              SearchService searchService,
                              ResourceService resourceService,
                              ResourceTypeService resourceTypeService,
                              ParserService parserPool) {
        this.genericItemService = genericItemService;
        this.idGenerator = idGenerator;
        this.searchService = searchService;
        this.resourceService = resourceService;
        this.resourceTypeService = resourceTypeService;
        this.parserPool = parserPool;
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
        UiField field = genericItemService.get(FIELD_RESOURCE_TYPE_NAME, id);
        setFormDependsOnName(field);
        return field;
    }

    @Override
    public Browsing<UiField> browseFields(FacetFilter filter) {
        Browsing<UiField> results = genericItemService.getResults(filter);
        for (UiField field : results.getResults()) {
            setFormDependsOnName(field);
        }
        return results;
    }

    @Override
    public List<UiField> getFields() {
        FacetFilter ff = new FacetFilter();
        ff.setQuantity(10000);
        ff.setResourceType(FIELD_RESOURCE_TYPE_NAME);
        return browseFields(ff).getResults();
    }

    @Override
    public List<UiField> importFields(List<UiField> fields) {
        List<UiField> imported = new ArrayList<>();
        for (UiField field : fields) {
            try {
                getField(field.getId());
                logger.info(String.format("Could not import UiField: [id=%s] - Already exists", field.getId()));
            } catch (ResourceNotFoundException e) {
                logger.info(String.format("Importing UiField: [id=%s] [name=%s]", field.getId(), field.getName()));
                imported.add(addField(field));
            }
        }
        return imported;
    }

    @Override
    public List<UiField> updateFields(List<UiField> fields) {
        List<UiField> updated = new ArrayList<>();
        for (UiField field : fields) {
            try {
                getField(field.getId());
                logger.info(String.format("Updating UiField: [id=%s] [name=%s]", field.getId(), field.getName()));
                updated.add(updateField(field.getId(), field));
            } catch (ResourceNotFoundException e) {
                logger.info(String.format("Could not update UiField: [id=%s] - Not Found", field.getId()));
            }
        }
        return updated;
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
    public List<Group> importGroups(List<Group> groups) {
        List<Group> imported = new ArrayList<>();
        for (Group group : groups) {
            try {
                getField(group.getId());
                logger.info("Could not import Group: [id=%s] - Already exists");
            } catch (ResourceNotFoundException e) {
                logger.info(String.format("Importing Group: [id=%s] [name=%s]", group.getId(), group.getName()));
                imported.add(addGroup(group));
            }
        }
        return imported;
    }

    @Override
    public List<Group> updateGroups(List<Group> groups) {
        List<Group> updated = new ArrayList<>();
        for (Group group : groups) {
            try {
                getField(group.getId());
                logger.info(String.format("Updating Group: [id=%s] [name=%s]", group.getId(), group.getName()));
                updated.add(updateGroup(group.getId(), group));
            } catch (ResourceNotFoundException e) {
                logger.info("Could not update Group: [id=%s] - Not Found");
            }
        }
        return updated;
    }

    @Override
    public Survey addSurvey(Survey survey) {
        createChapterIds(survey);
//        Date date = new Date();
//        survey.setCreationDate(date);
//        survey.setModificationDate(date);
        survey = add(survey, SURVEY_RESOURCE_TYPE_NAME);
        return survey;
    }

    @Override
    public Survey updateSurvey(String id, Survey survey) {
        createChapterIds(survey);
//        survey.setModificationDate(new Date());
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

    @Override
    public List<UiField> getFieldsByGroup(String groupId) {
        FacetFilter filter = new FacetFilter();
        filter.setResourceType(FIELD_RESOURCE_TYPE_NAME);
        filter.setQuantity(10000);
        filter.addFilter("form_group", groupId);

        Browsing<UiField> allFields = browseFields(filter);

        return allFields.getResults();
    }

    @Override
    public SurveyModel getSurveyModel(String surveyId) {
        Survey survey = getSurvey(surveyId);

        SurveyModel model = new SurveyModel();
        model.setSurveyId(surveyId);

        for (Chapter chapter : survey.getChapters()) {
            ChapterModel chapterModel = new ChapterModel(chapter, getChapterModel(surveyId, chapter.getId()));
            model.getChapterModels().add(chapterModel);
        }
        return model;
    }

    public List<GroupedFields<FieldGroup>> getChapterModel(String surveyId, String chapterId) {
        List<GroupedFields<FieldGroup>> groupedFieldGroups = new ArrayList<>();
        List<GroupedFields<UiField>> groupedFieldsList = getChapterModelFlat(surveyId, chapterId);

        for (GroupedFields<UiField> groupedFields : groupedFieldsList) {
            GroupedFields<FieldGroup> groupedFieldGroup = new GroupedFields<>();

            groupedFieldGroup.setGroup(groupedFields.getGroup());
            List<FieldGroup> fieldGroups = createFieldGroups(groupedFields.getFields());
            groupedFieldGroup.setFields(fieldGroups);

            int total = 0;
            for (UiField f : groupedFields.getFields()) {
                if (f.getForm().getMandatory() != null && f.getForm().getMandatory()
                        && f.getTypeInfo().getType() != null && !f.getTypeInfo().getType().equals("composite")) {
                    total += 1;
                }
            }

            int topLevel = 0;
            for (FieldGroup fg : fieldGroups) {
                if (fg.getField().getForm().getMandatory() != null && fg.getField().getForm().getMandatory()) {
                    topLevel += 1;
                }
            }
            RequiredFields requiredFields = new RequiredFields(topLevel, total);
            groupedFieldGroup.setRequired(requiredFields);

            groupedFieldGroups.add(groupedFieldGroup);
        }

        return groupedFieldGroups;
    }

    public List<GroupedFields<UiField>> getChapterModelFlat(String surveyId, String chapterId) {
        Survey survey = getSurvey(surveyId);
        List<GroupedFields<UiField>> groupedFieldsList = new ArrayList<>();
        List<Group> groups = new ArrayList<>();
        for (Chapter chapter : survey.getChapters()) {
            if (chapter.getId().equals(chapterId)) {

                groups.addAll(chapter.getSections().stream().map((Group id) -> getGroup(id.toString())).collect(Collectors.toList()));
            }
        }

        for (Group group : groups) {
            GroupedFields<UiField> groupedFields = new GroupedFields<>();

            groupedFields.setGroup(group);
            groupedFields.setFields(getFieldsByGroup(group.getId()));

            groupedFieldsList.add(groupedFields);
        }

        return groupedFieldsList;
    }

    @Override
    public List<GroupedFields<UiField>> getSurveyModelFlat(String surveyId) {
        Survey survey = getSurvey(surveyId);
        List<GroupedFields<UiField>> groupedFieldsList = new ArrayList<>();
        List<Group> groups = new ArrayList<>();
        for (Chapter chapter : survey.getChapters()) {
            groups.addAll(chapter.getSections().stream().map((Group id) -> getGroup(id.toString())).collect(Collectors.toList()));
        }

        for (Group group : groups) {
            GroupedFields<UiField> groupedFields = new GroupedFields<>();

            groupedFields.setGroup(group);
            groupedFields.setFields(getFieldsByGroup(group.getId()));

            groupedFieldsList.add(groupedFields);
        }

        return groupedFieldsList;
    }

    public Map<String, List<UiField>> getChapterFieldsMap(String id) {
        Map<String, List<UiField>> chapterFieldsMap = new HashMap<>();
        Model model = get(id);

        for (Chapter chapter : model.getChapters()) {
            List<UiField> fields = new ArrayList<>();
            for (Group group : chapter.getSections()) {
                fields.addAll(group.getFields());
            }
            chapterFieldsMap.put(chapter.getId(), fields);
        }

        return chapterFieldsMap;
    }

    private List<UiField> sortFieldsByParentId(List<UiField> fields) {
        List<UiField> sorted = fields.stream().filter(f -> f.getParentId() != null).sorted(Comparator.comparing(UiField::getParentId)).collect(Collectors.toList());
        sorted.addAll(fields.stream().filter(f -> f.getParentId() == null).collect(Collectors.toList()));
        return sorted;
    }

    public <T> T add(T obj, String resourceTypeName) {
        ResourceType resourceType = resourceTypeService.getResourceType(resourceTypeName);
        String id = null;
        try {
            id = ReflectUtils.getId(obj.getClass(), obj);
            if (id == null) {
                id = idGenerator.createId(resourceTypeName.charAt(0) + "-");
                ReflectUtils.setId(obj.getClass(), obj, id);
            }

        } catch (NoSuchFieldException e) {
            logger.error(e);
        }

        Resource resource = new Resource();
        resource.setResourceTypeName(resourceTypeName);
        resource.setResourceType(resourceType);
        resource.setPayload(parserPool.serialize(obj, ParserService.ParserServiceTypes.fromString(resourceType.getPayloadType())));
        logger.trace(LoggingUtils.addResource(resourceTypeName, id, obj));
        resourceService.addResource(resource);
        return obj;
    }

    public <T> T update(String id, T obj, String resourceTypeName) {
        Resource existing = null;
        try {
            if (!id.equals(ReflectUtils.getId(obj.getClass(), obj))) {
                throw new ResourceException("You are not allowed to modify the id of a resource.", HttpStatus.CONFLICT);
            }
            existing = genericItemService.searchResource(resourceTypeName, id, true);
            existing.setPayload(parserPool.serialize(obj, ParserService.ParserServiceTypes.JSON));
        } catch (NoSuchFieldException e) {
            logger.error(e);
        }

        logger.trace(LoggingUtils.updateResource(resourceTypeName, id, obj));
        resourceService.updateResource(existing);
        return obj;
    }

    public <T> void delete(String id, String resourceTypeName) throws ResourceNotFoundException {
        Resource resource = null;
        Class<?> clazz = genericItemService.getClassFromResourceType(resourceTypeName);
        resource = genericItemService.searchResource(resourceTypeName, id, true);
        T obj = (T) parserPool.deserialize(resource, clazz);
        logger.trace(LoggingUtils.deleteResource(resourceTypeName, id, obj));
        resourceService.deleteResource(resource.getId());
//        return obj;
    }

    private void createChapterIds(Survey survey) {
        if (survey.getChapters() != null) {
            for (Chapter chapter : survey.getChapters()) {
                if (chapter.getId() == null || "".equals(chapter.getId())) {
                    chapter.setId(idGenerator.createId("c-"));
                }
            }
        }
    }

    private void createChapterIds(Model model) {
        if (model.getChapters() != null) {
            for (Chapter chapter : model.getChapters()) {
                if (chapter.getId() == null || "".equals(chapter.getId())) {
                    chapter.setId(idGenerator.createId("c-"));
                }
            }
        }
    }

    @Override
    public Model add(Model model) {
        createChapterIds(model);
//        Date date = new Date();
//        model.setCreationDate(date);
//        model.setModificationDate(date);
        model = add(model, MODEL_RESOURCE_TYPE_NAME);
        return model;
    }

    @Override
    public Model update(String id, Model model) {
        createChapterIds(model);
//        model.setModificationDate(new Date());
        model = update(id, model, MODEL_RESOURCE_TYPE_NAME);
        return model;
    }

    @Override
    public void delete(String surveyId) throws ResourceNotFoundException {
        delete(surveyId, MODEL_RESOURCE_TYPE_NAME);
    }

    @Override
    public Model get(String id) {
        return genericItemService.get(MODEL_RESOURCE_TYPE_NAME, id);
    }

    @Override
    public Browsing<Model> browse(FacetFilter filter) {
        FacetFilter ff = new FacetFilter();
        ff.setQuantity(10000);
        ff.setResourceType(MODEL_RESOURCE_TYPE_NAME);
        return genericItemService.getResults(ff);
    }
}
