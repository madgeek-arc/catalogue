/**
 * Copyright 2021-2025 OpenAIRE AMKE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gr.athenarc.catalogue.ui.service;

import gr.uoa.di.madgik.registry.domain.Browsing;
import gr.uoa.di.madgik.registry.domain.FacetFilter;
import gr.uoa.di.madgik.registry.domain.Resource;
import gr.uoa.di.madgik.registry.domain.ResourceType;
import gr.uoa.di.madgik.registry.service.ParserService;
import gr.uoa.di.madgik.registry.service.ResourceService;
import gr.uoa.di.madgik.registry.service.ResourceTypeService;
import gr.uoa.di.madgik.registry.service.SearchService;
import gr.athenarc.catalogue.utils.LoggingUtils;
import gr.athenarc.catalogue.utils.ReflectUtils;
import gr.uoa.di.madgik.registry.exception.ResourceAlreadyExistsException;
import gr.uoa.di.madgik.registry.exception.ResourceException;
import gr.uoa.di.madgik.registry.exception.ResourceNotFoundException;
import gr.athenarc.catalogue.service.GenericResourceService;
import gr.athenarc.catalogue.service.id.IdGenerator;
import gr.athenarc.catalogue.ui.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class SimpleFormsService implements ModelService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleFormsService.class);

    private final GenericResourceService genericResourceService;
    private final IdGenerator<String> idGenerator;
    public final SearchService searchService;
    public final ResourceService resourceService;
    public final ResourceTypeService resourceTypeService;
    public final ParserService parserPool;
    public final FormDisplayService formDisplayService;

    private Map<String, Form> formMap;
    private Map<String, Display> displayMap;

    public SimpleFormsService(GenericResourceService genericResourceService,
                              IdGenerator<String> idGenerator,
                              SearchService searchService,
                              ResourceService resourceService,
                              ResourceTypeService resourceTypeService,
                              ParserService parserPool,
                              FormDisplayService formDisplayService) {
        this.genericResourceService = genericResourceService;
        this.idGenerator = idGenerator;
        this.searchService = searchService;
        this.resourceService = resourceService;
        this.resourceTypeService = resourceTypeService;
        this.parserPool = parserPool;
        this.formDisplayService = formDisplayService;
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
            existing = genericResourceService.get(resourceTypeName, id);
        } catch (NoSuchFieldException e) {
            logger.error(e.getMessage(), e);
        } catch (ResourceNotFoundException e) {
            // skip
        } catch (InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
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
            existing = genericResourceService.searchResource(resourceTypeName, id, true);
            existing.setPayload(parserPool.serialize(obj, ParserService.ParserServiceTypes.JSON));
        } catch (NoSuchFieldException e) {
            logger.error(e.getMessage(), e);
        } catch (InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        logger.trace(LoggingUtils.updateResource(resourceTypeName, id, obj));
        resourceService.updateResource(existing);
        return obj;
    }

    public <T> void delete(String id, String resourceTypeName) throws ResourceNotFoundException {
        Resource resource = null;
        Class<?> clazz = genericResourceService.getClassFromResourceType(resourceTypeName);
        resource = genericResourceService.searchResource(resourceTypeName, id, true);
        T obj = (T) parserPool.deserialize(resource, clazz);
        logger.trace(LoggingUtils.deleteResource(resourceTypeName, id, obj));
        resourceService.deleteResource(resource.getId());
//        return obj;
    }

    @Override
    public Model add(Model model) {
        createSectionIds(model);

        Date date = new Date();
        model.setCreationDate(date);
        model.setModificationDate(date);

        validateIds(model);
        createParents(model);
        model = add(model, MODEL_RESOURCE_TYPE_NAME);
        return model;
    }

    @Override
    public Model update(String id, Model model) {
        createSectionIds(model);
        model.setModificationDate(new Date());
        validateIds(model);
        createParents(model);
        model = update(id, model, MODEL_RESOURCE_TYPE_NAME);
        return model;
    }

    @Override
    public void delete(String id) throws ResourceNotFoundException {
        delete(id, MODEL_RESOURCE_TYPE_NAME);
    }

    @Override
    public Model get(String id) {
        Model model = genericResourceService.get(MODEL_RESOURCE_TYPE_NAME, id);
        enrichModel(model);
        return model;
    }

    @Override
    public Browsing<Model> browse(FacetFilter filter) {
        filter.setResourceType(MODEL_RESOURCE_TYPE_NAME);
        Browsing<Model> models = genericResourceService.getResults(filter);
        models.getResults().forEach(this::enrichModel);
        return models;
    }

    @Override
    public List<UiField> getAllFields(Model model) {
        List<UiField> allFields = new ArrayList<>();
        model.getSections().forEach(section -> allFields.addAll(getSectionFields(section)));
        return allFields;
    }

    @Override
    public List<UiField> getSectionFields(Section section) {
        List<UiField> fields = new ArrayList<>();
        if (section.getSubSections() != null) {
            for (Section s : section.getSubSections()) {
                fields.addAll(getSectionFields(s));
            }
        }
        if (section.getFields() != null) {
            fields.addAll(getFieldsRecursive(section.getFields()));
        }
        return fields;
    }

    @Override
    public List<UiField> getFieldsRecursive(List<UiField> fields) {
        List<UiField> allFields = new ArrayList<>();
        for (UiField field : fields) {
            allFields.add(field);
            if (field.getSubFields() != null) {
                allFields.addAll(getFieldsRecursive(field.getSubFields()));
            }
        }
        return allFields;
    }


    void enrichModel(Model model) { // TODO: refactor
        this.formMap = formDisplayService.getUiFieldIdFormMap(model.getId());
        this.displayMap = formDisplayService.getUiFieldIdDisplayMap(model.getId());
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
        return formMap != null ? formMap.get(fieldId) : null;
    }

    private Display getFieldDisplay(String fieldId) {
        return displayMap != null ? displayMap.get(fieldId) : null;
    }

    private void validateIds(Model model) {
        List<UiField> allFields = getAllFields(model);
        Set<String> ids = new HashSet<>();
        Set<String> uniqueIds = allFields
                .stream()
                .map(UiField::getId)
                .filter(f -> !ids.add(f))
                .collect(Collectors.toSet());
        if (!uniqueIds.isEmpty()) {
            throw new RuntimeException(String.format("Duplicate IDs found: [%s]", String.join(", ", uniqueIds)));
        }
    }

    private void createSectionIds(Model model) {
        if (model.getSections() != null) {
            for (Section section : model.getSections()) {
                if (section.getId() == null || "".equals(section.getId())) {
                    section.setId(idGenerator.createId("section-"));
                }
            }
        }
    }

    private void createParents(Model model) {
        for (Section section : model.getSections()) {
            getSectionFields(section).forEach(this::createFieldParents);
        }
    }

    private void createFieldParents(UiField parent) {
        if (parent != null && parent.getSubFields() != null) {
            for (UiField field : parent.getSubFields()) {
                field.setParentId(parent.getId());
                field.setParent(parent.getName());
                createFieldParents(field);
            }
        }
    }
}
