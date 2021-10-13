package gr.athenarc.catalogue.service;

import eu.openminted.registry.core.domain.Browsing;
import eu.openminted.registry.core.domain.FacetFilter;
import eu.openminted.registry.core.domain.Paging;
import eu.openminted.registry.core.domain.Resource;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public interface GenericResourceService {

    <T> T get(String resourceTypeName, String field, String value, boolean throwOnNull);

    <T> Browsing<T> cqlQuery(FacetFilter filter);

    <T> Browsing<T> getResults(FacetFilter filter);

    <T> Browsing<T> convertToBrowsing(@NotNull Paging<Resource> paging, String resourceTypeName);

    <T> Map<String, List<T>> getResultsGrouped(FacetFilter filter, String category);

    <T> T addRaw(String resourceTypeName, String payload);

    <T> T add(String resourceTypeName, T resource);

    <T> T update(String resourceTypeName, String id, T resource);

    <T> T delete(String resourceTypeName, String id);

    <T> T get(String resourceTypeName, String id);

    Class<?> getResourceTypeClass(String resourceTypeName);

}
