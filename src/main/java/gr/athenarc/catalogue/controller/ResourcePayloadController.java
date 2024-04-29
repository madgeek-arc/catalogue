/*
 * Copyright 2021-2024 OpenAIRE AMKE
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

package gr.athenarc.catalogue.controller;

import gr.uoa.di.madgik.registry.dao.ResourceTypeDao;
import gr.uoa.di.madgik.registry.domain.ResourceType;
import gr.athenarc.catalogue.service.ResourcePayloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

@RestController
@RequestMapping("payloads")
public class ResourcePayloadController {

    private final ResourcePayloadService resourcePayloadService;
    private final ResourceTypeDao resourceTypeDao;

    @Autowired
    public ResourcePayloadController(ResourcePayloadService resourcePayloadService,
                                     ResourceTypeDao resourceTypeDao) {
        this.resourcePayloadService = resourcePayloadService;
        this.resourceTypeDao = resourceTypeDao;
    }

    @GetMapping("{id}")
    public ResponseEntity<String> get(@RequestParam("resourceType") String resourceType, @PathVariable("id") String id) {
        String payload = resourcePayloadService.getRaw(resourceType, id);
        return new ResponseEntity<>(payload, createContentType(resourceType), HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<String> createResource(@RequestParam("resourceType") String resourceType,
                                                 @RequestBody String resource) throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
        return new ResponseEntity<>(resourcePayloadService.addRaw(resourceType, resource), createContentType(resourceType), HttpStatus.OK);
    }

    @PutMapping("{id}")
    public <T> ResponseEntity<?> update(@PathVariable("id") String id,
                                        @RequestParam("resourceType") String resourceType,
                                        @RequestBody String resource) throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
        return new ResponseEntity<>(resourcePayloadService.updateRaw(resourceType, id, resource), createContentType(resourceType), HttpStatus.OK);
    }

    private HttpHeaders createContentType(String resourceTypeName) {
        ResourceType resourceType = resourceTypeDao.getResourceType(resourceTypeName);
        HttpHeaders headers = new HttpHeaders();
        if (resourceType.getPayloadType().equals("xml")) {
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
        } else if (resourceType.getPayloadType().equals("json")) {
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        } else {
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE);
        }
        return headers;
    }
}
