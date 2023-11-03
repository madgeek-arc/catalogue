package gr.athenarc.catalogue.ui.controller;

import gr.athenarc.catalogue.ui.domain.DataRequest;
import gr.athenarc.catalogue.ui.service.DataRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("request/default")
public class DataRequestController {

    private final DataRequestService dataRequestService;

    public DataRequestController(DataRequestService dataRequestService) {
        this.dataRequestService = dataRequestService;
    }

    @PostMapping
    Mono<? extends ResponseEntity<?>> performRequest(@RequestBody DataRequest request) {
        Mono<?> response = dataRequestService.retrieve(request);
        return response
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }
}
