package gr.athenarc.catalogue.controller;

import gr.athenarc.catalogue.service.ResourcePayloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("payloads")
public class ResourcePayloadController {

    private final ResourcePayloadService resourcePayloadService;

    @Autowired
    public ResourcePayloadController(ResourcePayloadService resourcePayloadService) {
        this.resourcePayloadService = resourcePayloadService;
    }

    @GetMapping("{id}")
    public ResponseEntity<String> get(@RequestParam("resourceType") String resourceType, @PathVariable("id") String id) {
        String payload = resourcePayloadService.getRaw(resourceType, id);
        return new ResponseEntity<>(payload, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<String> createResource(@RequestParam("resourceType") String resourceType,
                                                @RequestBody String resource) {
        return new ResponseEntity<>(resourcePayloadService.addRaw(resourceType, resource), HttpStatus.OK);
    }

    @PutMapping("{id}")
    public <T> ResponseEntity<?> update(@PathVariable("id") String id,
                                        @RequestParam("resourceType") String resourceType,
                                        @RequestBody T resource) throws NoSuchFieldException {
        return new ResponseEntity<>(resourcePayloadService.updateRaw(resourceType, id, (String) resource), HttpStatus.OK);
    }
}
