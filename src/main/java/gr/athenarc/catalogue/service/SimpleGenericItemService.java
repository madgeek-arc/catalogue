package gr.athenarc.catalogue.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openminted.registry.core.domain.*;
import eu.openminted.registry.core.domain.index.IndexField;
import eu.openminted.registry.core.service.*;
import gr.athenarc.catalogue.ReflectUtils;
import gr.athenarc.catalogue.exception.ResourceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SimpleGenericItemService implements GenericItemService, ResourcePayloadService {

    private static final Logger logger = LogManager.getLogger(SimpleGenericItemService.class);

    public final SearchService searchService;
    public final ResourceService resourceService;
    public final ResourceTypeService resourceTypeService;
    public final ParserService parserPool;
    public final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${elastic.index.max_result_window:10000}")
    protected int maxQuantity;

    private Map<String, List<String>> browseByMap;
    private Map<String, Map<String, String>> labelsMap;

    @Autowired
    public SimpleGenericItemService(SearchService searchService,
                                    ResourceService resourceService,
                                    ResourceTypeService resourceTypeService,
                                    ParserService parserPool) {
        this.searchService = searchService;
        this.resourceService = resourceService;
        this.resourceTypeService = resourceTypeService;
        this.parserPool = parserPool;
    }

    @PostConstruct
    void initResourceTypesBrowseFields() {
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
            logger.info("Generating browse fields for [{}]", rt.getName());
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
            ret = (T) parserPool.deserialize(res, getResourceTypeClass(resourceTypeName));
        } catch (UnknownHostException e) {
            throw new ResourceException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ret;
    }

    @Override
    public String getRaw(String resourceTypeName, String id) {
        Resource res = searchResource(resourceTypeName, id);
        return res.getPayload();
    }

    @Override
    public String addRaw(String resourceTypeName, String payload) {
        Class<?> clazz = getResourceTypeClass(resourceTypeName);
        ResourceType resourceType = resourceTypeService.getResourceType(resourceTypeName);
        payload = payload.replaceAll("[\n\t]", "");

        Resource res = new Resource();
        res.setResourceTypeName(resourceTypeName);
        res.setResourceType(resourceType);
        Date now = new Date();
        res.setCreationDate(now);
        res.setModificationDate(now);
        res.setPayload(payload);
        res.setPayloadFormat(resourceType.getPayloadType());

        // create Java class and set ID using reflection
        Object item = parserPool.deserialize(res, clazz);
        ReflectUtils.setId(clazz, item, UUID.randomUUID().toString());

        // return to Resource class and save
        payload = parserPool.serialize(item, ParserService.ParserServiceTypes.XML);
        res.setPayload(payload);
        resourceService.addResource(res);
        return payload;
    }

    @Override
    public String updateRaw(String resourceTypeName, String id, String payload) throws NoSuchFieldException {
        Class<?> clazz = getResourceTypeClass(resourceTypeName);
        payload = payload.replaceAll("[\n\t]", "");

        String existingId = ReflectUtils.getId(clazz, payload);
        if (!id.equals(existingId)) {
            throw new ResourceException("Resource body id different than path id", HttpStatus.CONFLICT);
        }
        Resource res = this.searchResource(resourceTypeName, id);
        Date now = new Date();
        res.setModificationDate(now);
        res.setPayload(payload);

        // save resource
        res = resourceService.addResource(res);
        return res.getPayload();
    }

    @Override
    public <T> T add(String resourceTypeName, T resource) {
        Class<?> clazz = getResourceTypeClass(resourceTypeName);
        resource = (T) objectMapper.convertValue(resource, clazz);

        Resource res = new Resource();
        res.setResourceTypeName(resourceTypeName);
        res.setResourceType(resourceTypeService.getResourceType(resourceTypeName));
        res.setCreationDate(new Date());
        res.setModificationDate(new Date());
        String ret;

        ReflectUtils.setId(clazz, resource, UUID.randomUUID().toString());
        ret = parserPool.serialize(resource, ParserService.ParserServiceTypes.XML);
        res.setPayload(ret);
        resourceService.addResource(res);

        return resource;
    }

    @Override
    public <T> T update(String resourceTypeName, String id, T resource) throws NoSuchFieldException {
        Class<?> clazz = getResourceTypeClass(resourceTypeName);
        resource = (T) objectMapper.convertValue(resource, clazz);

        String existingId = ReflectUtils.getId(clazz, resource);
        if (!id.equals(existingId)) {
            throw new ResourceException("Resource body id different than path id", HttpStatus.CONFLICT);
        }

        Resource res = searchResource(resourceTypeName, id);
        res.setModificationDate(new Date());
        String ret;
        ret = parserPool.serialize(resource, ParserService.ParserServiceTypes.XML);
        res.setPayload(ret);
        resourceService.updateResource(res);

        return resource;
    }

    @Override
    public <T> T delete(String resourceTypeName, String id) {
        Resource res = searchResource(resourceTypeName, id);
        resourceService.deleteResource(res.getId());
        return (T) parserPool.deserialize(res, getResourceTypeClass(resourceTypeName));
    }

    @Override
    public <T> T get(String resourceTypeName, String id) {
        Resource res = searchResource(resourceTypeName, id);
        return (T) parserPool.deserialize(res, getResourceTypeClass(resourceTypeName));
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
            logger.fatal("getResults", e);
            throw new ServiceException(e);
        }
        return browsing;
    }

    @Override
    public <T> Browsing<T> convertToBrowsing(@NotNull Paging<Resource> paging, String resourceTypeName) {
        Class<?> clazz = getResourceTypeClass(resourceTypeName);
        List<T> results = (List<T>) paging.getResults()
                .parallelStream()
                .map(res -> (T) parserPool.deserialize(res, clazz))
                .collect(Collectors.toList());
        return new Browsing<>(paging, results, labelsMap.get(resourceTypeName));
    }


    @Override
    public <T> Map<String, List<T>> getResultsGrouped(FacetFilter filter, String category) {
        Map<String, List<T>> result = new HashMap<>();
        Class<?> clazz = getResourceTypeClass(filter.getResourceType());

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
            logger.fatal(e);
            throw new ServiceException(e);
        }
    }

    public Class<?> getResourceTypeClass(String resourceTypeName) {
        Class<?> tClass = null;
        try {
            tClass = Class.forName(resourceTypeService.getResourceType(resourceTypeName).getProperty("class"));
        } catch (ClassNotFoundException e) {
            logger.error(e);
        } catch (NullPointerException e) {
            logger.error("Class property is not defined", e);
            throw new ServiceException("ResourceType [" + resourceTypeName + "] does not have properties field");
        }
        return tClass;
    }

    public Map<String, List<String>> getBrowseByMap() {
        return browseByMap;
    }

    public void setBrowseByMap(Map<String, List<String>> browseByMap) {
        this.browseByMap = browseByMap;
    }

    private Resource searchResource(String resourceTypeName, String id) {
        Resource res = null;
        try {
            res = searchService.searchId(resourceTypeName, new SearchService.KeyValue(resourceTypeName + "_id", id));
        } catch (UnknownHostException e) {
            logger.error(e);
        }
        return res;
    }
}
