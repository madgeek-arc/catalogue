package gr.athenarc.catalogue.controller;

import eu.openminted.registry.core.domain.FacetFilter;
import eu.openminted.registry.core.domain.Paging;
import gr.athenarc.catalogue.annotations.Browse;
import gr.athenarc.catalogue.service.GenericItemService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static gr.athenarc.catalogue.utils.PagingUtils.createFacetFilter;

@RestController
@RequestMapping(path = "items")
public class GenericItemController {

    private final GenericItemService genericResourceService;

    @Autowired
    GenericItemController(@Qualifier("catalogueGenericItemService") GenericItemService genericResourceService) {
        this.genericResourceService = genericResourceService;
    }

    @PostMapping()
    public <T> ResponseEntity<?> create(@RequestParam("resourceType") String resourceType,
                                        @RequestBody T resource) {
        T createdResource;
        createdResource = genericResourceService.add(resourceType, resource);
        return new ResponseEntity<>(createdResource, HttpStatus.OK);
    }

    @PutMapping("{id}")
    public <T> ResponseEntity<?> update(@PathVariable("id") String id,
                                        @RequestParam("resourceType") String resourceType,
                                        @RequestParam(value = "raw", defaultValue = "false") boolean raw,
                                        @RequestBody T resource) throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
        T createdResource;
        createdResource = genericResourceService.update(resourceType, id, resource);
        return new ResponseEntity<>(createdResource, HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public <T> ResponseEntity<?> delete(@PathVariable("id") String id, @RequestParam("resourceType") String resourceType) {
        T deleted = genericResourceService.delete(resourceType, id);
        return new ResponseEntity<>(deleted, HttpStatus.OK);
    }

    @Browse
    @GetMapping()
    public ResponseEntity<Paging<?>> browseByResourceType(@Parameter(hidden = true) @RequestParam Map<String, Object> allRequestParams) {
        FacetFilter ff = createFacetFilter(allRequestParams);
        return new ResponseEntity<>(genericResourceService.getResults(ff), HttpStatus.OK);
    }

    @GetMapping("{id}")
    public <T> ResponseEntity<T> get(@RequestParam("resourceType") String resourceType, @PathVariable("id") String id) {
        T ret = genericResourceService.get(resourceType, id);
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @GetMapping("search")
    public ResponseEntity<?> getByField(@RequestParam("resourceType") String resourceType, @RequestParam(value = "field") String field, @RequestParam(value = "value") String value) {
        Object ret = genericResourceService.get(resourceType, field, value, true);
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }
}
