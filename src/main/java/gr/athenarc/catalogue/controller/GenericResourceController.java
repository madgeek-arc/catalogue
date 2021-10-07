package gr.athenarc.catalogue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openminted.registry.core.domain.FacetFilter;
import eu.openminted.registry.core.domain.Paging;
import gr.athenarc.catalogue.service.GenericResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.UnknownHostException;

@RestController
@RequestMapping(path = "items")
public class GenericResourceController {

    @Autowired
    GenericResourceService genericService;

    @GetMapping("/resources/{resourceType}")
    ResponseEntity<Paging<?>> getResourcesByResourceType(@PathVariable("resourceType") String resourceType, @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword) throws UnknownHostException {
        FacetFilter ff = new FacetFilter();
        ff.setResourceType(resourceType);
        ff.setKeyword(keyword);
        Paging<?> results = genericService.getResults(ff);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    @GetMapping("{resourceType}")
    ResponseEntity<Paging<?>> getByResourceType(@PathVariable("resourceType") String resourceType, @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword) throws UnknownHostException {
        FacetFilter ff = new FacetFilter();
        ff.setResourceType(resourceType);
        ff.setKeyword(keyword);
        return new ResponseEntity<>(genericService.getResults(ff), HttpStatus.OK);
    }

    @PostMapping("{resourceType}")
    <T> ResponseEntity<?> saveResource(@PathVariable("resourceType") String resourceType, @RequestBody T resource) {
        return new ResponseEntity<>(genericService.save(resourceType, resource), HttpStatus.OK);
    }

    @GetMapping("{resourceType}/query")
    ResponseEntity<?> getByField(@PathVariable("resourceType") String resourceType, @RequestParam(value = "field") String field, @RequestParam(value = "value") String value) {
        Object ret = genericService.get(resourceType, field, value, true);
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @GetMapping("{resourceType}/{id}")
    <T> ResponseEntity<T> getById(@PathVariable("resourceType") String resourceType, @RequestParam(value = "field") String field, @RequestParam(value = "value") String value) {
        return new ResponseEntity<>(genericService.get(resourceType, field, value, true), HttpStatus.OK);
    }
}
