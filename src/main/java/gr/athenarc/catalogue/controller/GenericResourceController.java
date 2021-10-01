package gr.athenarc.catalogue.controller;

import eu.openminted.registry.core.domain.FacetFilter;
import eu.openminted.registry.core.domain.Paging;
import gr.athenarc.catalogue.ReflectUtils;
import gr.athenarc.catalogue.service.GenericResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.UnknownHostException;
import java.util.stream.Collectors;

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

        results.setResults(results.getResults().stream().parallel().map(item -> ReflectUtils.getFieldValue("value", item)).collect(Collectors.toList()));
        return new ResponseEntity<>(results, HttpStatus.OK);
    }
}
