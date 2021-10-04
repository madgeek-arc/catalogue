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
import org.springframework.util.MultiValueMap;

import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBElement;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GenericResourceService {

    public final SearchService searchService;
    public final ResourceService resourceService;
    public final ResourceTypeService resourceTypeService;
    public final ParserService parserPool;

    @Value("${elastic.index.max_result_window:10000}")
    protected int maxQuantity;

    private static final Logger logger = LogManager.getLogger(GenericResourceService.class);
    private List<String> browseBy;

    private Map<String, String> labels;

    protected ResourceType resourceType;
    protected Class<?> typeParameterClass;

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

    protected String getResourceType() {
        return resourceType.getName();
    }

    void initBrowseFields(String resourceTypeName) {
        resourceType = resourceTypeService.getResourceType(resourceTypeName);
        Set<String> browseSet = new HashSet<>();
        Map<String, Set<String>> sets = new HashMap<>();
        labels = new HashMap<>();
        labels.put("resourceType", "Resource Type");
//        for (IndexField f : resourceTypeService.getResourceTypeIndexFields(getResourceType())) {
        for (IndexField f : resourceType.getIndexFields()) {
            sets.putIfAbsent(f.getResourceType().getName(), new HashSet<>());
            labels.put(f.getName(), f.getLabel());
            if (f.getLabel() != null) {
                sets.get(f.getResourceType().getName()).add(f.getName());
            }
        }
        boolean flag = true;
        for (Map.Entry<String, Set<String>> entry : sets.entrySet()) {
            if (flag) {
                browseSet.addAll(entry.getValue());
                flag = false;
            } else {
                browseSet.retainAll(entry.getValue());
            }
        }
        browseBy = new ArrayList<>();
        browseBy.addAll(browseSet);
//        browseBy.add("resourceType");
        java.util.Collections.sort(browseBy);
        logger.info("Generating browse fields for [{}]", getResourceType());
    }

    public Object getObject(String resourceTypeName, String field, String value, boolean throwOnNull) {
        Resource res;
        Object ret;
        try {
            res = searchService.searchId(resourceTypeName, new SearchService.KeyValue(field, value));
            if (throwOnNull && res == null) {
                throw new ResourceException(String.format("%s '%s' does not exist!", resourceTypeName, value), HttpStatus.NOT_FOUND);
            }
            ret = parserPool.deserialize(res, Object.class);
        } catch (UnknownHostException e) {
            throw new ResourceException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (ret instanceof JAXBElement<?>) {
            ret = ((JAXBElement<?>) ret).getValue();
        }
        return ret;
    }

    public <T> T get(String resourceTypeName, String field, String value, boolean throwOnNull) {
        Resource res;
        T ret;
        try {
            res = searchService.searchId(resourceTypeName, new SearchService.KeyValue(field, value));
            if (throwOnNull && res == null) {
                throw new ResourceException(String.format("%s '%s' does not exist!", resourceTypeName, value), HttpStatus.NOT_FOUND);
            }
            ret = (T) parserPool.deserialize(res, MultiValueMap.class);
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
        initBrowseFields(filter.getResourceType());
        filter.setBrowseBy(browseBy);
        return convertToBrowsing(searchService.cqlQuery(filter));
    }

    public <T> Browsing<T> getResults(FacetFilter filter) {
        initBrowseFields(filter.getResourceType());
//        filter.setResourceType(getResourceType());
        filter.setBrowseBy(browseBy);
        Browsing<T> browsing;
        filter.setResourceType(getResourceType());
        try {
            browsing = convertToBrowsing(searchService.search(filter));
        } catch (UnknownHostException e) {
            logger.fatal("getResults", e);
            throw new ServiceException(e);
        }
        return browsing;
    }

    public <T> Browsing<T> convertToBrowsing(@NotNull Paging<Resource> paging) {
        List<T> results = (List<T>) paging.getResults()
                .parallelStream()
                .map(res -> (T) parserPool.deserialize(res, typeParameterClass))
                .collect(Collectors.toList());
        return new Browsing<>(paging, results, labels);
    }


    protected <T> Map<String, List<T>> getResultsGrouped(FacetFilter filter, String category) {
        Map<String, List<T>> result = new HashMap<>();

        filter.setResourceType(getResourceType());
        Map<String, List<Resource>> resources;
        try {
            resources = searchService.searchByCategory(filter, category);
            for (Map.Entry<String, List<Resource>> bucket : resources.entrySet()) {
                List<T> bucketResults = new ArrayList<>();
                for (Resource res : bucket.getValue()) {
                    bucketResults.add((T) parserPool.deserialize(res, typeParameterClass));
                }
                result.put(bucket.getKey(), bucketResults);
            }
            return result;
        } catch (Exception e) {
            logger.fatal(e);
            throw new ServiceException(e);
        }
    }


    protected List<String> getBrowseBy() {
        return browseBy;
    }

    public void setBrowseBy(List<String> browseBy) {
        this.browseBy = browseBy;
    }

}
