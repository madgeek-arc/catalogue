package gr.athenarc.catalogue.ui.controller;

import gr.athenarc.catalogue.ui.domain.Prefill;
import gr.athenarc.catalogue.ui.service.RequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("request/default")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    ResponseEntity<Object> performRequest(@RequestBody Prefill request) {
        return new ResponseEntity<>(requestService.perform(request), HttpStatus.OK);
    }
}
