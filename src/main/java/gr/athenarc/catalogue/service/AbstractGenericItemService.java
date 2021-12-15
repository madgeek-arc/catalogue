package gr.athenarc.catalogue.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openminted.registry.core.domain.*;
import eu.openminted.registry.core.domain.index.IndexField;
import eu.openminted.registry.core.service.*;
import gr.athenarc.catalogue.LoggingUtils;
import gr.athenarc.catalogue.ReflectUtils;
import gr.athenarc.catalogue.exception.ResourceException;
import gr.athenarc.catalogue.exception.ResourceNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractGenericItemService implements GenericItemService {
    private static final Logger logger = LogManager.getLogger(AbstractGenericItemService.class);

    public final SearchService searchService;
    public final ResourceService resourceService;
    public final ResourceTypeService resourceTypeService;
    public final ParserService parserPool;
    public final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${elastic.index.max_result_window:10000}")
    protected int maxQuantity;

    private Map<String, List<String>> browseByMap;
    private Map<String, Map<String, String>> labelsMap;

    protected AbstractGenericItemService(SearchService searchService,
                                         ResourceService resourceService,
                                         ResourceTypeService resourceTypeService,
                                         ParserService parserPool) {
        this.searchService = searchService;
        this.resourceService = resourceService;
        this.resourceTypeService = resourceTypeService;
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
            logger.error(e);
            throw new ServiceException(e);
        }
        return ret;
    }

    @Override
    public <T> T add(String resourceTypeName, T resource) {
        Class<?> clazz = getClassFromResourceType(resourceTypeName);
        resource = (T) objectMapper.convertValue(resource, clazz);

        ResourceType resourceType = resourceTypeService.getResourceType(resourceTypeName);
        Resource res = new Resource();
        res.setResourceTypeName(resourceTypeName);
        res.setResourceType(resourceType);

        String id = UUID.randomUUID().toString();
        ReflectUtils.setId(clazz, resource, id);
        String payload = parserPool.serialize(resource, ParserService.ParserServiceTypes.fromString(resourceType.getPayloadType()));
        res.setPayload(payload);
        logger.info(LoggingUtils.addResource(resourceTypeName, id, resource));
        resourceService.addResource(res);

        return resource;
    }

    @Override
    public <T> T update(String resourceTypeName, String id, T resource) throws NoSuchFieldException {
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
            logger.fatal("getResults", e);
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
            logger.fatal(e);
            throw new ServiceException(e);
        }
    }

    @Override
    public Class<?> getClassFromResourceType(String resourceTypeName) {
        Class<?> tClass = null;
        try {
            tClass = Class.forName(resourceTypeService.getResourceType(resourceTypeName).getProperty("class"));
        } catch (ClassNotFoundException e) {
            logger.error(e);
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
            logger.error(e);
        }
        if (throwOnNull) {
            return Optional.ofNullable(res)
                    .orElseThrow(() -> new ResourceNotFoundException(id, resourceTypeName));
        }
        return res;
    }


    public Map<String, List<String>> getBrowseByMap() {
        return browseByMap;
    }

    public void setBrowseByMap(Map<String, List<String>> browseByMap) {
        this.browseByMap = browseByMap;
    }
}
