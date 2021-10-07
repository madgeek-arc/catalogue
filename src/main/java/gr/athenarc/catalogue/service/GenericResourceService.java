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

    <T> T save(String resourceTypeName, T resource);

    Class<?> getResourceTypeClass(String resourceTypeName);

}
