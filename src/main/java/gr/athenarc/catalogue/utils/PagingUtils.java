/*
 * Copyright 2021-2024 OpenAIRE AMKE
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
