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

package gr.athenarc.catalogue.service;

import gr.uoa.di.madgik.registry.domain.Browsing;
import gr.uoa.di.madgik.registry.domain.FacetFilter;
import gr.uoa.di.madgik.registry.domain.Paging;
import gr.uoa.di.madgik.registry.domain.Resource;
import gr.uoa.di.madgik.registry.service.SearchService;

import javax.validation.constraints.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public interface GenericItemService {

    <T> T get(String resourceTypeName, String field, String value, boolean throwOnNull);

    <T> Browsing<T> getResults(FacetFilter filter);

    <T> Browsing<T> convertToBrowsing(@NotNull Paging<Resource> paging, String resourceTypeName);

    <T> Map<String, List<T>> getResultsGrouped(FacetFilter filter, String category);

    <T> T add(String resourceTypeName, T resource);

    <T> T update(String resourceTypeName, String id, T resource) throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException;

    <T> T delete(String resourceTypeName, String id);

    <T> T get(String resourceTypeName, String id);

    Class<?> getClassFromResourceType(String resourceTypeName);

    Resource searchResource(String resourceTypeName, String id, boolean throwOnNull);

    Resource searchResource(String resourceTypeName, SearchService.KeyValue... keyValues);

}
