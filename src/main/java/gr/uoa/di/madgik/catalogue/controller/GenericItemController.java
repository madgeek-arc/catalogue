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
package gr.uoa.di.madgik.catalogue.controller;

import gr.uoa.di.madgik.catalogue.service.GenericResourceService;
import gr.uoa.di.madgik.registry.annotation.BrowseParameters;
import gr.uoa.di.madgik.registry.domain.FacetFilter;
import gr.uoa.di.madgik.registry.domain.Paging;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;

@RestController
@RequestMapping(path = "items")
public class GenericItemController {

    private final GenericResourceService genericResourceService;

    GenericItemController(@Qualifier("catalogueGenericResourceService") GenericResourceService genericResourceService) {
        this.genericResourceService = genericResourceService;
    }

    @PostMapping()
    public <T> ResponseEntity<T> create(@RequestParam("resourceType") String resourceType,
                                        @RequestBody T resource) {
        T createdResource;
        createdResource = genericResourceService.add(resourceType, resource);
        return new ResponseEntity<>(createdResource, HttpStatus.OK);
    }

    @PutMapping("{id}")
    public <T> ResponseEntity<T> update(@PathVariable("id") String id,
                                        @RequestParam("resourceType") String resourceType,
                                        @RequestParam(value = "raw", defaultValue = "false") boolean raw,
                                        @RequestBody T resource) throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
        T createdResource;
        createdResource = genericResourceService.update(resourceType, id, resource);
        return new ResponseEntity<>(createdResource, HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public <T> ResponseEntity<T> delete(@PathVariable("id") String id, @RequestParam("resourceType") String resourceType) {
        T deleted = genericResourceService.delete(resourceType, id);
        return new ResponseEntity<>(deleted, HttpStatus.OK);
    }

    @BrowseParameters
    @Parameter(in = ParameterIn.QUERY, name = "resourceType", required = true, description = "Resource Type to search",
            content = @Content(schema = @Schema(type = "string", defaultValue = "resourceTypes")))
    @GetMapping()
    public <T> ResponseEntity<Paging<T>> browseByResourceType(@Parameter(hidden = true) @RequestParam MultiValueMap<String, Object> allRequestParams) {
        FacetFilter ff = FacetFilter.from(allRequestParams);
        return new ResponseEntity<>(genericResourceService.getResults(ff), HttpStatus.OK);
    }

    @GetMapping("{id}")
    public <T> ResponseEntity<T> get(@RequestParam("resourceType") String resourceType, @PathVariable("id") String id) {
        T ret = genericResourceService.get(resourceType, id);
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @GetMapping("search")
    public <T> ResponseEntity<T> getByField(@RequestParam("resourceType") String resourceType, @RequestParam(value = "field") String field, @RequestParam(value = "value") String value) {
        T ret = genericResourceService.get(resourceType, field, value, true);
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }
}
