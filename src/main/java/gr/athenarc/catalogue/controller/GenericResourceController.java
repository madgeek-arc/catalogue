package gr.athenarc.catalogue.controller;

import eu.openminted.registry.core.domain.Browsing;
import eu.openminted.registry.core.domain.FacetFilter;
import eu.openminted.registry.core.domain.Paging;
import gr.athenarc.catalogue.service.GenericResourceService;
import gr.athenarc.xsd2java.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.UnknownHostException;

@RestController
@RequestMapping(path = "items")
public class GenericResourceController<T> {

    @Autowired
    GenericResourceService genericService;

    @GetMapping("/resources/{resourceType}")
    ResponseEntity<Paging<?>> getResourcesByResourceType(@PathVariable("resourceType") String resourceType, @RequestParam("keyword") String keyword) throws UnknownHostException {
        Paging<?> results = genericService.searchService.searchKeyword(resourceType, keyword);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    @GetMapping("{resourceType}")
    ResponseEntity<Paging<?>> getByResourceType(@PathVariable("resourceType") String resourceType, @RequestParam("keyword") String keyword) throws UnknownHostException {
        FacetFilter ff = new FacetFilter();
        ff.setResourceType(resourceType);
        ff.setKeyword(keyword);
//        Paging<Resource> results = genericService.cqlQuery(ff);
        Paging<Resource> results = genericService.convertToBrowsing(genericService.searchService.searchKeyword(resourceType, keyword));
        return new ResponseEntity<>(results, HttpStatus.OK);
    }
}
