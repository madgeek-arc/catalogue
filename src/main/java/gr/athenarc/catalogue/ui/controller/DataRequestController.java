/**
 * Copyright 2021-2025 OpenAIRE AMKE
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
