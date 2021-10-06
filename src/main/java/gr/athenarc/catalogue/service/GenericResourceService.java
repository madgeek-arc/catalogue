package gr.athenarc.catalogue.service;

import eu.openminted.registry.core.domain.*;
import eu.openminted.registry.core.domain.index.IndexField;
import eu.openminted.registry.core.service.*;
import gr.athenarc.catalogue.exception.ResourceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBElement;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GenericResourceService {

    private static final Logger logger = LogManager.getLogger(GenericResourceService.class);

    public final SearchService searchService;
    public final ResourceService resourceService;
    public final ResourceTypeService resourceTypeService;
    public final ParserService parserPool;

    @Value("${elastic.index.max_result_window:10000}")
    protected int maxQuantity;

    private Map<String, List<String>> browseByMap;
    private Map<String, Map<String, String>> labelsMap;

    @Autowired
    public GenericResourceService(SearchService searchService,
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
            labels = new HashMap<>();
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
        if (ret instanceof JAXBElement<?>) {
            ret = (T) ((JAXBElement<?>) ret).getValue();
        }
        return ret;
    }

    public <T> Browsing<T> cqlQuery(FacetFilter filter) {
//        filter.setResourceType(getResourceType());
        filter.setBrowseBy(browseByMap.get(filter.getResourceType()));
        return convertToBrowsing(searchService.cqlQuery(filter), filter.getResourceType());
    }

    public <T> Browsing<T> getResults(FacetFilter filter) {
//        filter.setResourceType(getResourceType());
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

    public <T> Browsing<T> convertToBrowsing(@NotNull Paging<Resource> paging, String resourceTypeName) {
        Class<?> clazz = getResourceTypeClass(resourceTypeName);
        List<T> results = (List<T>) paging.getResults()
                .parallelStream()
                .map(res -> (T) parserPool.deserialize(res, clazz))
                .collect(Collectors.toList());
        return new Browsing<>(paging, results, labelsMap.get(resourceTypeName));
    }


    protected <T> Map<String, List<T>> getResultsGrouped(FacetFilter filter, String category) {
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

    private Class<?> getResourceTypeClass(String resourceTypeName) {
        Class<?> tClass = null;
        try {
            tClass = Class.forName(resourceTypeService.getResourceType(resourceTypeName).getProperty("class"));
        } catch (ClassNotFoundException e) {
            logger.error(e);
        }
        return tClass;
    }

    public Map<String, List<String>> getBrowseByMap() {
        return browseByMap;
    }

    public void setBrowseByMap(Map<String, List<String>> browseByMap) {
        this.browseByMap = browseByMap;
    }
}
