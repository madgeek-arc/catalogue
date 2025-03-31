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
package gr.uoa.di.madgik.catalogue.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uoa.di.madgik.registry.domain.*;
import gr.uoa.di.madgik.registry.domain.index.IndexField;
import gr.uoa.di.madgik.registry.service.*;
import gr.uoa.di.madgik.registry.exception.ResourceException;
import gr.uoa.di.madgik.registry.exception.ResourceNotFoundException;
import gr.uoa.di.madgik.catalogue.utils.LoggingUtils;
import gr.uoa.di.madgik.catalogue.utils.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.UnaryOperator;

@Service
public class GenericResourceManager implements GenericResourceService {
    private static final Logger logger = LoggerFactory.getLogger(GenericResourceManager.class);

    public final SearchService searchService;
    public final ResourceService resourceService;
    public final ResourceTypeService resourceTypeService;
    public final VersionService versionService;
    public final ParserService parserPool;
    public final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${elastic.index.max_result_window:10000}")
    protected int maxQuantity;

    private Map<String, List<String>> browseByMap;
    private Map<String, Map<String, String>> labelsMap;

    protected GenericResourceManager(SearchService searchService,
                                     ResourceService resourceService,
                                     ResourceTypeService resourceTypeService,
                                     VersionService versionService,
                                     ParserService parserPool) {
        this.searchService = searchService;
        this.resourceService = resourceService;
        this.resourceTypeService = resourceTypeService;
        this.versionService = versionService;
        this.parserPool = parserPool;
    }

    @PostConstruct
    void initResourceTypesBrowseFields() { // TODO: move this to a bean to avoid running multiple times ??
        browseByMap = new TreeMap<>();
        labelsMap = new TreeMap<>();
        Map<String, Set<String>> aliasGroupBrowse = new TreeMap<>();
        Map<String, Map<String, String>> aliasGroupLabels = new TreeMap<>();
        for (ResourceType rt : resourceTypeService.getAllResourceType()) {
            Set<String> browseSet = new TreeSet<>();
            Map<String, Set<String>> sets = new TreeMap<>();
            Map<String, String> labels = new TreeMap<>();

            labels.put("resourceType", "Resource Type");
            for (IndexField f : rt.getIndexFields()) {
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
            if (rt.getAliases() != null) {
                for (String alias : rt.getAliases()) {
                    if (aliasGroupBrowse.get(alias) == null) {
                        aliasGroupBrowse.put(alias, new TreeSet<>(browseSet));
                        aliasGroupLabels.put(alias, new TreeMap<>(labels));
                    } else {
                        aliasGroupBrowse.get(alias).retainAll(browseSet);
                        aliasGroupLabels.get(alias).keySet().retainAll(labels.keySet());
                    }
                }
            }

            labelsMap.put(rt.getName(), labels);
            browseByMap.put(rt.getName(), new ArrayList<>(browseSet));
            logger.debug("Generating browse fields for [{}]", rt.getName());
        }
        for (String alias : aliasGroupBrowse.keySet()) {
            browseByMap.put(alias, aliasGroupBrowse.get(alias).stream().sorted().toList());
            labelsMap.put(alias, aliasGroupLabels.get(alias));
        }
    }

    @Override
    public <T> T get(String resourceTypeName, String field, String value, boolean throwOnNull) {
        Resource res;
        T ret;
        res = searchService.searchFields(resourceTypeName, new SearchService.KeyValue(field, value));
        if (throwOnNull && res == null) {
            throw new ResourceException(String.format("%s '%s' does not exist!", resourceTypeName, value), HttpStatus.NOT_FOUND);
        }
        ret = (T) parserPool.deserialize(res, getClassFromResourceType(resourceTypeName));
        return ret;
    }

    @Override
    public <T> T add(String resourceTypeName, T resource) {
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
        return (T) parserPool.deserialize(res, getClassFromResourceType(res.getResourceTypeName()));
    }

    public <T> Browsing<T> getResults(FacetFilter filter) {
        filter.setBrowseBy(createBrowseBy(filter));
        return convertToBrowsing(searchService.search(filter), filter.getResourceType());
    }

    @Override
    public <T> Browsing<T> getResults(FacetFilter filter, UnaryOperator<List<Facet>> transformer) {
        Browsing<T> browsing = getResults(filter);
        browsing.setFacets(transformer.apply(browsing.getFacets()));
        return browsing;
    }

    @Override
    public <T> Browsing<T> convertToBrowsing(@NotNull Paging<Resource> paging, String resourceTypeName) {
        Class<?> clazz = getClassFromResourceType(resourceTypeName);
        List<T> results;
        if (clazz != null) { // all resources are from the same resourceType
            results = (List<T>) paging.getResults()
                    .parallelStream()
                    .map(res -> (T) parserPool.deserialize(res, clazz))
                    .toList();
        } else { // mixed resources
            results = (List<T>) paging.getResults()
                    .stream()
                    .map(resource -> {
                        T item = null;
                        try {
                            item = (T) parserPool.deserialize(resource, getClassFromResourceType(resource.getResourceTypeName()));
                        } catch (Exception e) {
                            logger.warn("Problem encountered: ", e);
                        }
                        return item;
                    })
                    .filter(Objects::nonNull)
                    .toList();
        }
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
                    if (clazz != null) {
                        bucketResults.add((T) parserPool.deserialize(res, clazz));
                    } else {
                        bucketResults.add((T) parserPool.deserialize(res, getClassFromResourceType(res.getResourceTypeName())));
                    }
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
            ResourceType resourceType = resourceTypeService.getResourceType(resourceTypeName);
            if (resourceType == null) { // search was performed using alias
                return null;
            }
            tClass = Class.forName(resourceType.getProperty("class"));
        } catch (ClassNotFoundException e) {
            logger.warn(e.getMessage(), e);
            tClass = Map.class;
        } catch (NullPointerException e) {
            logger.error("Class property is not defined", e);
            throw new ServiceException(String.format("ResourceType [%s] does not have properties field", resourceTypeName));
        }
        return tClass;
    }

    @Override
    public Resource searchResource(String resourceTypeName, String id, boolean throwOnNull) {
        Resource res = searchService.searchFields(resourceTypeName, new SearchService.KeyValue("resource_internal_id", id));
        if (throwOnNull) {
            return Optional.ofNullable(res)
                    .orElseThrow(() -> new ResourceNotFoundException(id, resourceTypeName));
        }
        return res;
    }

    @Override
    public Resource searchResource(String resourceTypeName, SearchService.KeyValue... keyValues) {
        return searchService.searchFields(resourceTypeName, keyValues);
    }

    public Map<String, List<String>> getBrowseByMap() {
        return browseByMap;
    }

    public void setBrowseByMap(Map<String, List<String>> browseByMap) {
        this.browseByMap = browseByMap;
    }

    private List<String> createBrowseBy(FacetFilter filter) {
        Set<String> browseBy = new HashSet<>();
        if (filter.getBrowseBy() != null) {
            browseBy.addAll(filter.getBrowseBy());
        }
        if (filter.getResourceType() != null && browseByMap.containsKey(filter.getResourceType())) {
            browseBy.addAll(browseByMap.get(filter.getResourceType()));
        }
        return new ArrayList<>(browseBy);
    }
}
