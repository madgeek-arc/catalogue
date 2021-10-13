package gr.athenarc.catalogue.controller;

import eu.openminted.registry.core.domain.FacetFilter;
import eu.openminted.registry.core.domain.Paging;
import gr.athenarc.catalogue.service.GenericResourceService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

@RestController
@RequestMapping(path = "items")
public class GenericResourceController {

    private final GenericResourceService genericService;

    @Autowired
    GenericResourceController(GenericResourceService genericService) {
        this.genericService = genericService;
    }

    @PostMapping("{resourceType}/payload")
    public <T> ResponseEntity<?> createResource(@PathVariable("resourceType") String resourceType, @RequestBody String resource) {
        T createdResource = genericService.addRaw(resourceType, resource);
        return new ResponseEntity<>(createdResource, HttpStatus.OK);
    }

    @PostMapping("{resourceType}")
    public <T> ResponseEntity<?> createResource(@PathVariable("resourceType") String resourceType, @RequestBody T resource) {
        return new ResponseEntity<>(genericService.add(resourceType, resource), HttpStatus.OK);
    }

    @PutMapping("{resourceType}/{id}")
    public <T> ResponseEntity<?> updateResource(@PathVariable("resourceType") String resourceType, @PathVariable("id") String id, @RequestBody T resource) {
        return new ResponseEntity<>(genericService.update(resourceType, id, resource), HttpStatus.OK);
    }

    @DeleteMapping("{resourceType}/{id}")
    public <T> ResponseEntity<?> deleteResource(@PathVariable("resourceType") String resourceType, @PathVariable("id") String id) {
        return new ResponseEntity<>(genericService.delete(resourceType, id), HttpStatus.OK);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "query", value = "Keyword to refine the search", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "from", value = "Starting index in the result set", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "quantity", value = "Quantity to be fetched", dataType = "string", paramType = "query")
    })
    @GetMapping("{resourceType}")
    public ResponseEntity<Paging<?>> browseByResourceType(@PathVariable("resourceType") String resourceType, @ApiIgnore @RequestParam Map<String, Object> allRequestParams) {
        FacetFilter ff = createFacetFilter(allRequestParams, resourceType);
        return new ResponseEntity<>(genericService.getResults(ff), HttpStatus.OK);
    }

    @GetMapping("{resourceType}/{id}")
    public <T> ResponseEntity<T> get(@PathVariable("resourceType") String resourceType, @PathVariable("id") String id) {
        T ret = genericService.get(resourceType, id);
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @GetMapping("{resourceType}/search")
    public ResponseEntity<?> getByField(@PathVariable("resourceType") String resourceType, @RequestParam(value = "field") String field, @RequestParam(value = "value") String value) {
        Object ret = genericService.get(resourceType, field, value, true);
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    private FacetFilter createFacetFilter(Map<String, Object> allRequestParams, String resourceTypeName) {
        FacetFilter ff = new FacetFilter();
        ff.setKeyword(allRequestParams.get("query") != null ? (String) allRequestParams.remove("query") : "");
        ff.setFrom(allRequestParams.get("from") != null ? Integer.parseInt((String) allRequestParams.remove("from")) : 0);
        ff.setQuantity(allRequestParams.get("quantity") != null ? Integer.parseInt((String) allRequestParams.remove("quantity")) : 10);
        ff.setFilter(allRequestParams);
        ff.setResourceType(resourceTypeName);
        return ff;
    }
}
