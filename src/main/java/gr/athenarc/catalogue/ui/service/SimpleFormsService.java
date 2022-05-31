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
import gr.athenarc.catalogue.exception.ResourceAlreadyExistsException;
import gr.athenarc.catalogue.exception.ResourceException;
import gr.athenarc.catalogue.exception.ResourceNotFoundException;
import gr.athenarc.catalogue.service.GenericItemService;
import gr.athenarc.catalogue.service.id.IdGenerator;
import gr.athenarc.catalogue.ui.domain.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.stream.Collectors;

public class SimpleFormsService implements FormsService, ModelService {

    private static final Logger logger = LogManager.getLogger(SimpleFormsService.class);
    private static final String FIELD_RESOURCE_TYPE_NAME = "field";
    private static final String SECTION_RESOURCE_TYPE_NAME = "section";
    private static final String SURVEY_RESOURCE_TYPE_NAME = "survey";
    private static final String MODEL_RESOURCE_TYPE_NAME = "model";
    private final GenericItemService genericItemService;
    private final IdGenerator<String> idGenerator;
    public final SearchService searchService;
    public final ResourceService resourceService;
    public final ResourceTypeService resourceTypeService;
    public final ParserService parserPool;
    public final FormDisplayService formDisplayService;

    private Map<String, Form> formMap;
    private Map<String, Display> displayMap;

    public SimpleFormsService(GenericItemService genericItemService,
                              IdGenerator<String> idGenerator,
                              SearchService searchService,
                              ResourceService resourceService,
                              ResourceTypeService resourceTypeService,
                              ParserService parserPool,
                              FormDisplayService formDisplayService) {
        this.genericItemService = genericItemService;
        this.idGenerator = idGenerator;
        this.searchService = searchService;
        this.resourceService = resourceService;
        this.resourceTypeService = resourceTypeService;
        this.parserPool = parserPool;
        this.formDisplayService = formDisplayService;
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
    public Section addSection(Section section) {
        section = add(section, SECTION_RESOURCE_TYPE_NAME);
        return section;
    }

    @Override
    public Section updateSection(String id, Section section) {
        section = update(id, section, SECTION_RESOURCE_TYPE_NAME);
        return section;
    }

    @Override
    public void deleteSection(String sectionId) throws ResourceNotFoundException {
        delete(sectionId, SECTION_RESOURCE_TYPE_NAME);
    }

    @Override
    public Section getSection(String id) {
        return genericItemService.get(SECTION_RESOURCE_TYPE_NAME, id);
    }

    @Override
    public List<Section> getSections() {
        FacetFilter ff = new FacetFilter();
        ff.setQuantity(10000);
        ff.setResourceType(SECTION_RESOURCE_TYPE_NAME);
        return (List) genericItemService.getResults(ff).getResults();
    }

    @Override
    public List<Section> importSections(List<Section> sections) {
        List<Section> imported = new ArrayList<>();
        for (Section section : sections) {
            try {
                getField(section.getId());
                logger.info("Could not import Section: [id=%s] - Already exists");
            } catch (ResourceNotFoundException e) {
                logger.info(String.format("Importing Section: [id=%s] [name=%s]", section.getId(), section.getName()));
                imported.add(addSection(section));
            }
        }
        return imported;
    }

    @Override
    public List<Section> updateSections(List<Section> sections) {
        List<Section> updated = new ArrayList<>();
        for (Section section : sections) {
            try {
                getField(section.getId());
                logger.info(String.format("Updating Section: [id=%s] [name=%s]", section.getId(), section.getName()));
                updated.add(updateSection(section.getId(), section));
            } catch (ResourceNotFoundException e) {
                logger.info("Could not update Section: [id=%s] - Not Found");
            }
        }
        return updated;
    }

    @Override
    public List<UiField> getFieldsBySection(String sectionId) {
        FacetFilter filter = new FacetFilter();
        filter.setResourceType(FIELD_RESOURCE_TYPE_NAME);
        filter.setQuantity(10000);
        filter.addFilter("form_section", sectionId);

        Browsing<UiField> allFields = browseFields(filter);

        return allFields.getResults();
    }

    private List<UiField> sortFieldsByParentId(List<UiField> fields) {
        List<UiField> sorted = fields.stream().filter(f -> f.getParentId() != null).sorted(Comparator.comparing(UiField::getParentId)).collect(Collectors.toList());
        sorted.addAll(fields.stream().filter(f -> f.getParentId() == null).collect(Collectors.toList()));
        return sorted;
    }

    public <T> T add(T obj, String resourceTypeName) {
        ResourceType resourceType = resourceTypeService.getResourceType(resourceTypeName);
        String id = null;
        T existing = null;
        try {
            id = ReflectUtils.getId(obj.getClass(), obj);
            if (id == null) {
                id = idGenerator.createId(resourceTypeName.charAt(0) + "-");
                ReflectUtils.setId(obj.getClass(), obj, id);
            }
            existing = genericItemService.get(resourceTypeName, id);
        } catch (NoSuchFieldException e) {
            logger.error(e);
        } catch (ResourceNotFoundException e) {
            // skip
        }
        if (existing != null) {
            throw new ResourceAlreadyExistsException(id, resourceTypeName);
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

    private void createChapterIds(Model model) {
        if (model.getSections() != null) {
            for (Section section : model.getSections()) {
                if (section.getId() == null || "".equals(section.getId())) {
                    section.setId(idGenerator.createId("section-"));
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
        Model model = genericItemService.get(MODEL_RESOURCE_TYPE_NAME, id);
        enrichModel(model);
        return model;
    }

    @Override
    public Browsing<Model> browse(FacetFilter filter) {
        filter.setResourceType(MODEL_RESOURCE_TYPE_NAME);
        return genericItemService.getResults(filter);
    }

    void enrichModel(Model model) { // TODO: refactor
        this.formMap = formDisplayService.getUiFieldIdFormMap();
        this.displayMap = formDisplayService.getUiFieldIdDisplayMap();
        if (model != null && model.getSections() != null) {
            for (Section section : model.getSections()) {
                enrichFields(section.getFields());
            }
        }
    }

    private void enrichFields(List<UiField> fields) {
        if (fields != null) {
            for (UiField field : fields) {
                Form form = getFieldForm(field.getId());
                if (form != null) {
                    field.setForm(form);
                }
                Display display = getFieldDisplay(field.getId());
                if (display != null) {
                    field.setDisplay(getFieldDisplay(field.getId()));
                }
                enrichFields(field.getSubFields());
            }
        }
    }

    private Form getFieldForm(String fieldId) {
        return formMap.get(fieldId);
    }

    private Display getFieldDisplay(String fieldId) {
        return displayMap.get(fieldId);
    }
}
