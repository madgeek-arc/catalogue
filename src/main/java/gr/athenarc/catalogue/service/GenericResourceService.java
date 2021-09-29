package gr.athenarc.catalogue.service;

import eu.openminted.registry.core.domain.*;
import eu.openminted.registry.core.domain.index.IndexField;
import eu.openminted.registry.core.service.*;
import eu.openminted.registry.core.service.ResourceService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GenericResourceService {

    @Autowired
    public SearchService searchService;

    @Autowired
    public ResourceService resourceService;

    @Autowired
    public ResourceTypeService resourceTypeService;
    @Autowired
    public ParserService parserPool;

    @Value("${elastic.index.max_result_window:10000}")
    protected int maxQuantity;

    private static final Logger logger = LogManager.getLogger(AbstractGenericService.class);
    private List<String> browseBy;

    private Map<String, String> labels;

    protected ResourceType resourceType;
    protected Class typeParameterClass;

    public GenericResourceService() {
    }

    protected String getResourceType() {
        return resourceType.getName();
    }

//    @PostConstruct
//    void init() {
//        resourceType = resourceTypeService.getResourceType(getResourceType());
//        Set<String> browseSet = new HashSet<>();
//        Map<String, Set<String>> sets = new HashMap<>();
//        labels = new HashMap<>();
//        labels.put("resourceType", "Resource Type");
//        for (IndexField f : resourceTypeService.getResourceTypeIndexFields(getResourceType())) {
//            sets.putIfAbsent(f.getResourceType().getName(), new HashSet<>());
//            labels.put(f.getName(), f.getLabel());
//            if (f.getLabel() != null) {
//                sets.get(f.getResourceType().getName()).add(f.getName());
//            }
//        }
//        boolean flag = true;
//        for (Map.Entry<String, Set<String>> entry : sets.entrySet()) {
//            if (flag) {
//                browseSet.addAll(entry.getValue());
//                flag = false;
//            } else {
//                browseSet.retainAll(entry.getValue());
//            }
//        }
//        browseBy = new ArrayList<>();
//        browseBy.addAll(browseSet);
//        browseBy.add("resourceType");
//        logger.info("Generated generic service for " + getResourceType() + "[" + getClass().getSimpleName() + "]");
//    }

    public <T> Browsing<T> cqlQuery(FacetFilter filter) {
//        filter.setResourceType(getResourceType());
        return convertToBrowsing(cqlQuery(filter));
    }

    protected <T> Browsing<T> getResults(FacetFilter filter) {
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
