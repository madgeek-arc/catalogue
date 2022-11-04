package gr.athenarc.catalogue.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openminted.registry.core.domain.*;
import eu.openminted.registry.core.domain.index.IndexField;
import eu.openminted.registry.core.service.*;
import gr.athenarc.catalogue.LoggingUtils;
import gr.athenarc.catalogue.ReflectUtils;
import gr.athenarc.catalogue.exception.ResourceException;
import gr.athenarc.catalogue.exception.ResourceNotFoundException;
import gr.athenarc.catalogue.ui.domain.Model;
import gr.athenarc.catalogue.ui.domain.Section;
import gr.athenarc.catalogue.ui.domain.UiField;
import gr.athenarc.catalogue.ui.service.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractGenericItemService implements GenericItemService {
    private static final Logger logger = LoggerFactory.getLogger(AbstractGenericItemService.class);

    public final SearchService searchService;
    public final ResourceService resourceService;
    public final ResourceTypeService resourceTypeService;
    public final ModelService modelService;
    public final ParserService parserPool;
    public final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${elastic.index.max_result_window:10000}")
    protected int maxQuantity;

    private Map<String, List<String>> browseByMap;
    private Map<String, Map<String, String>> labelsMap;

    protected AbstractGenericItemService(SearchService searchService,
                                         ResourceService resourceService,
                                         ResourceTypeService resourceTypeService,
                                         ModelService modelService,
                                         ParserService parserPool) {
        this.searchService = searchService;
        this.resourceService = resourceService;
        this.resourceTypeService = resourceTypeService;
        this.modelService = modelService;
        this.parserPool = parserPool;
    }

    @PostConstruct
    void initResourceTypesBrowseFields() { // TODO: move this to a bean to avoid running multiple times ??
        browseByMap = new HashMap<>();
        labelsMap = new HashMap<>();
        for (ResourceType rt : resourceTypeService.getAllResourceType()) {
            Set<String> browseSet = new HashSet<>();
            Map<String, Set<String>> sets = new HashMap<>();
            Map<String, String> labels = new HashMap<>();

            labels.put("resourceType", "Resource Type");
            for (IndexField f : rt.getIndexFields()) {
                sets.putIfAbsent(f.getResourceType().getName(), new HashSet<>());
                labels.put(f.getName(), f.getLabel());
                if (f.getLabel() != null) {
                    sets.get(f.getResourceType().getName()).add(f.getName());
                }
            }
            labelsMap.put(rt.getName(), labels);
            boolean flag = true;
            for (Map.Entry<String, Set<String>> entry : sets.entrySet()) {
                if (flag) {
                    browseSet.addAll(entry.getValue());
                    flag = false;
                } else {
                    browseSet.retainAll(entry.getValue());
                }
            }
            List<String> browseBy = new ArrayList<>(browseSet);
            java.util.Collections.sort(browseBy);
            browseByMap.put(rt.getName(), browseBy);
            logger.debug("Generating browse fields for [{}]", rt.getName());
        }
    }

    @Override
    public <T> T get(String resourceTypeName, String field, String value, boolean throwOnNull) {
        Resource res;
        T ret;
        try {
            res = searchService.searchId(resourceTypeName, new SearchService.KeyValue(field, value));
            if (throwOnNull && res == null) {
                throw new ResourceException(String.format("%s '%s' does not exist!", resourceTypeName, value), HttpStatus.NOT_FOUND);
            }
            ret = (T) parserPool.deserialize(res, getClassFromResourceType(resourceTypeName));
        } catch (UnknownHostException e) {
            logger.error(e.getMessage(), e);
            throw new ServiceException(e);
        }
        return ret;
    }

    @Override
    public <T> T add(String resourceTypeName, T resource) {
        resource = validate(resource, resourceTypeName);
        Class<?> clazz = getClassFromResourceType(resourceTypeName);
        if (!clazz.isInstance(resource)) {
            resource = (T) objectMapper.convertValue(resource, clazz);
        }

        ResourceType resourceType = resourceTypeService.getResourceType(resourceTypeName);
        Resource res = new Resource();
        res.setResourceTypeName(resourceTypeName);
        res.setResourceType(resourceType);

        String id = null;
        try {
            id = ReflectUtils.getId(clazz, resource);
        } catch (Exception e) {
            logger.warn("Could not find field 'id'.", e);
        }
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
            ReflectUtils.setId(clazz, resource, id);
        }
        String payload = parserPool.serialize(resource, ParserService.ParserServiceTypes.fromString(resourceType.getPayloadType()));
        res.setPayload(payload);
        logger.info(LoggingUtils.addResource(resourceTypeName, id, resource));
        resourceService.addResource(res);

        return resource;
    }

    @Override
    public <T> T update(String resourceTypeName, String id, T resource) throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
        validate(resource, resourceTypeName);
        Class<?> clazz = getClassFromResourceType(resourceTypeName);
        resource = (T) objectMapper.convertValue(resource, clazz);

        String existingId = ReflectUtils.getId(clazz, resource);
        if (!id.equals(existingId)) {
            throw new ResourceException("Resource body id different than path id", HttpStatus.CONFLICT);
        }

        ResourceType resourceType = resourceTypeService.getResourceType(resourceTypeName);
        Resource res = searchResource(resourceTypeName, id, true);
        String payload = parserPool.serialize(resource, ParserService.ParserServiceTypes.fromString(resourceType.getPayloadType()));
        res.setPayload(payload);
        logger.info(LoggingUtils.updateResource(resourceTypeName, id, resource));
        resourceService.updateResource(res);

        return resource;
    }

    @Override
    public <T> T delete(String resourceTypeName, String id) {
        Resource res = searchResource(resourceTypeName, id, true);
        logger.info(LoggingUtils.deleteResource(resourceTypeName, id, res));
        resourceService.deleteResource(res.getId());
        return (T) parserPool.deserialize(res, getClassFromResourceType(resourceTypeName));
    }

    @Override
    public <T> T get(String resourceTypeName, String id) {
        Resource res = searchResource(resourceTypeName, id, true);
        return (T) parserPool.deserialize(res, getClassFromResourceType(resourceTypeName));
    }

    @Override
    public <T> Browsing<T> cqlQuery(FacetFilter filter) {
        filter.setBrowseBy(browseByMap.get(filter.getResourceType()));
        return convertToBrowsing(searchService.cqlQuery(filter), filter.getResourceType());
    }

    @Override
    public <T> Browsing<T> getResults(FacetFilter filter) {
        filter.setBrowseBy(browseByMap.get(filter.getResourceType()));
        Browsing<T> browsing;
        try {
            browsing = convertToBrowsing(searchService.search(filter), filter.getResourceType());
        } catch (UnknownHostException e) {
            throw new ServiceException(e);
        }
        return browsing;
    }

    @Override
    public <T> Browsing<T> convertToBrowsing(@NotNull Paging<Resource> paging, String resourceTypeName) {
        Class<?> clazz = getClassFromResourceType(resourceTypeName);
        List<T> results = (List<T>) paging.getResults()
                .parallelStream()
                .map(res -> (T) parserPool.deserialize(res, clazz))
                .collect(Collectors.toList());
        return new Browsing<>(paging, results, labelsMap.get(resourceTypeName));
    }

    @Override
    public <T> Map<String, List<T>> getResultsGrouped(FacetFilter filter, String category) {
        Map<String, List<T>> result = new HashMap<>();
        Class<?> clazz = getClassFromResourceType(filter.getResourceType());

        Map<String, List<Resource>> resources;
        try {
            resources = searchService.searchByCategory(filter, category);
            for (Map.Entry<String, List<Resource>> bucket : resources.entrySet()) {
                List<T> bucketResults = new ArrayList<>();
                for (Resource res : bucket.getValue()) {
                    bucketResults.add((T) parserPool.deserialize(res, clazz));
                }
                result.put(bucket.getKey(), bucketResults);
            }
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ServiceException(e);
        }
    }

    @Override
    public Class<?> getClassFromResourceType(String resourceTypeName) {
        Class<?> tClass = null;
        try {
            tClass = Class.forName(resourceTypeService.getResourceType(resourceTypeName).getProperty("class"));
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (NullPointerException e) {
            logger.error("Class property is not defined", e);
            throw new ServiceException(String.format("ResourceType [%s] does not have properties field", resourceTypeName));
        }
        return tClass;
    }

    @Override
    public Resource searchResource(String resourceTypeName, String id, boolean throwOnNull) {
        Resource res = null;
        try {
            res = searchService.searchId(resourceTypeName, new SearchService.KeyValue(resourceTypeName + "_id", id));
        } catch (UnknownHostException e) {
            logger.error(e.getMessage(), e);
        }
        if (throwOnNull) {
            return Optional.ofNullable(res)
                    .orElseThrow(() -> new ResourceNotFoundException(id, resourceTypeName));
        }
        return res;
    }

    @Override
    public Resource searchResource(String resourceTypeName, SearchService.KeyValue... keyValues) {
        Resource res = null;
        try {
            res = searchService.searchId(resourceTypeName, keyValues);
        } catch (UnknownHostException e) {
            logger.error(e.getMessage(), e);
        }
        return res;
    }

    public Map<String, List<String>> getBrowseByMap() {
        return browseByMap;
    }

    public void setBrowseByMap(Map<String, List<String>> browseByMap) {
        this.browseByMap = browseByMap;
    }

    private <T> T validate(T resource, String resourceTypeName) {
        FacetFilter ff = new FacetFilter();
        ff.addFilter("resourceType", resourceTypeName);
        List<Model> models = modelService.browse(ff).getResults();
        if (models == null || models.size() != 1) {
            throw new RuntimeException(String.format("Found more than one models for [resourceType=%s]", resourceTypeName));
        }
        Model model = models.get(0);
        return (T) validateSections(resource, model.getSections());
    }

    private Object validateSections(Object obj, List<Section> sections) {
        for (Section section : sections) {
            if (section.getSubSections() != null) {
                validateSections(obj, section.getSubSections());
            }
            if (section.getFields() != null) {
                validateFields(obj, section.getFields(), true);
            }
        }
        return obj;
    }

    private boolean validateFields(Object object, List<UiField> fields, boolean mandatory) {
        boolean empty = true;
        for (UiField field : fields) {
            if (field.getSubFields() != null && !field.getSubFields().isEmpty()) {
                if (object == null) {
                    break;
                } else if (object instanceof List) {
                    for (Object ans : (List) object) {
                        empty = empty && validateFields(((LinkedHashMap) ans).get(field.getName()), field.getSubFields(), field.getForm().getMandatory());
                    }
                } else {
                    empty = validateFields(((LinkedHashMap) object).get(field.getName()), field.getSubFields(), field.getForm().getMandatory());
                }

                if ("composite".equals(field.getTypeInfo().getType()) && empty) {
                    if (object instanceof LinkedHashMap) {
                        ((LinkedHashMap) object).put(field.getName(), null);
                    } else {
                        object = null;
                    }
                }
            }
            if (mandatory && field.getForm().getMandatory()) {
                checkMandatoryField(object, field);
            }
            if (containsValue(object)) {
                empty = false;
            }
        }
        return empty;
    }

    private void checkMandatoryField(Object answer, UiField field) {
        if (answer == null) {
            throw new RuntimeException(String.format("Mandatory field %s is empty.", field.getName()));
        } else if (answer instanceof List) {
            if (((List) answer).isEmpty()) {
                throw new RuntimeException(String.format("Mandatory field %s is empty.", field.getName()));
            }
        } else if (((LinkedHashMap) answer).get(field.getName()) == null || ((LinkedHashMap) answer).get(field.getName()).equals("")) {
            throw new RuntimeException(String.format("Mandatory field %s is empty.", field.getName()));
        }
    }

    private boolean containsValue(Object obj) {
        boolean contains = false;
        if (obj instanceof LinkedHashMap) {
            for (Object key : ((LinkedHashMap) obj).keySet()) {
                if (((LinkedHashMap) obj).get(key) instanceof LinkedHashMap || ((LinkedHashMap) obj).get(key) instanceof List) {
                    contains = contains || containsValue(((LinkedHashMap) obj).get(key));
                } else if (((LinkedHashMap) obj).get(key) != null && !((LinkedHashMap) obj).get(key).equals("")) {
                    return true;
                }
            }
        } else if (obj instanceof List) {
            for (Object item : (List) obj) {
                contains = contains || containsValue(item);
                if (contains) {
                    break;
                }
            }
        }
        return contains;
    }
}
