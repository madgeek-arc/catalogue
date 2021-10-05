package gr.athenarc.catalogue.controller;

import eu.openminted.registry.core.domain.FacetFilter;
import eu.openminted.registry.core.domain.Paging;
import gr.athenarc.catalogue.service.GenericResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.xml.bind.JAXBElement;
import java.net.UnknownHostException;
import java.util.stream.Collectors;

@ApiIgnore
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
        Paging<Object> results = genericService.getResults(ff);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    @GetMapping("{resourceType}")
    ResponseEntity<Paging<?>> getByResourceType(@PathVariable("resourceType") String resourceType, @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword) throws UnknownHostException {
        FacetFilter ff = new FacetFilter();
        ff.setResourceType(resourceType);
        ff.setKeyword(keyword);
//        Paging<Object> results = genericService.convertToBrowsing(genericService.cqlQuery(ff));
//        Paging<Object> results = genericService.convertToBrowsing(genericService.searchService.searchKeyword(resourceType, keyword));
        Paging<Object> results = genericService.getResults(ff);

        results.setResults(results.getResults().stream().parallel().map(item -> ((JAXBElement) item).getValue()).collect(Collectors.toList()));
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    @GetMapping("{resourceType}/query")
    <T> ResponseEntity<?> getByField(@PathVariable("resourceType") String resourceType, @RequestParam(value = "field") String field, @RequestParam(value = "value") String value) {
        T ret = genericService.get(resourceType, field, value, true);
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @GetMapping("{resourceType}/object")
    ResponseEntity<?> getObjectByField(@PathVariable("resourceType") String resourceType, @RequestParam(value = "field") String field, @RequestParam(value = "value") String value) {
        Object ret = genericService.getObject(resourceType, field, value, true);
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @GetMapping("{resourceType}/{id}")
    ResponseEntity<?> getById(@PathVariable("resourceType") String resourceType, @RequestParam(value = "field") String field, @RequestParam(value = "value") String value) {
        return new ResponseEntity<>(genericService.get(resourceType, field, value, true), HttpStatus.OK);
    }
}
