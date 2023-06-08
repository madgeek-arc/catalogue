package gr.athenarc.catalogue.utils;

import eu.openminted.registry.core.domain.FacetFilter;

import java.util.HashMap;
import java.util.Map;

public class PagingUtils {

    public static FacetFilter createFacetFilter(Map<String, Object> params) {
        FacetFilter ff = new FacetFilter();
        ff.setResourceType(params.get("resourceType") != null ? (String) params.remove("resourceType") : "");
        ff.setKeyword(params.get("query") != null ? (String) params.remove("query") : "");
        ff.setFrom(params.get("from") != null ? Integer.parseInt((String) params.remove("from")) : 0);
        ff.setQuantity(params.get("quantity") != null ? Integer.parseInt((String) params.remove("quantity")) : 10);
        Map<String, Object> sort = new HashMap<>();
        Map<String, Object> order = new HashMap<>();
        String orderDirection = params.get("order") != null ? (String) params.remove("order") : "asc";
        String orderField = params.get("orderField") != null ? (String) params.remove("orderField") : null;
        if (orderField != null) {
            order.put("order", orderDirection);
            sort.put(orderField, order);
            ff.setOrderBy(sort);
        }
        ff.setFilter(params);
        return ff;
    }

    private PagingUtils() {
    }
}
