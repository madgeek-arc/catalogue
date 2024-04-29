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

package gr.athenarc.catalogue.ui.controller;

import gr.uoa.di.madgik.registry.domain.Browsing;
import gr.uoa.di.madgik.registry.domain.FacetFilter;
import gr.athenarc.catalogue.annotations.Browse;
import gr.athenarc.catalogue.ui.domain.Model;
import gr.athenarc.catalogue.ui.service.ModelService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static gr.athenarc.catalogue.utils.PagingUtils.createFacetFilter;

@RestController
@RequestMapping("forms")
public class FormsController {

    private final ModelService modelService;

    @Autowired
    public FormsController(ModelService modelService) {
        this.modelService = modelService;
    }


    @PostMapping("models")
    public ResponseEntity<Model> addModel(@RequestBody Model model) {
        return new ResponseEntity<>(modelService.add(model), HttpStatus.CREATED);
    }

    @PutMapping("models/{id}")
    public ResponseEntity<Model> updateModel(@PathVariable("id") String id, @RequestBody Model model) {
        return new ResponseEntity<>(modelService.update(id, model), HttpStatus.OK);
    }

    @GetMapping("models/{id}")
    public ResponseEntity<Model> getModel(@PathVariable("id") String id) {
        return new ResponseEntity<>(modelService.get(id), HttpStatus.OK);
    }

    @Browse
    @GetMapping("models")
    public ResponseEntity<Browsing<Model>> getModels(@Parameter(hidden = true) @RequestParam Map<String, Object> allRequestParams) {
        String resourceType = (String) allRequestParams.get("resourceType");
        FacetFilter ff = createFacetFilter(allRequestParams);
        if (resourceType != null && !"".equals(resourceType)) {
            ff.addFilter("resourceType", resourceType);
        }
        return new ResponseEntity<>(modelService.browse(ff), HttpStatus.OK);
    }

    @DeleteMapping("models/{id}")
    public ResponseEntity<Void> deleteModel(@PathVariable("id") String id) {
        modelService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("models")
    public ResponseEntity<List<Model>> deleteAllModels() {
        FacetFilter filter = new FacetFilter();
        filter.setQuantity(1000);
        List<Model> models = modelService.browse(filter).getResults();
        for (Model model : models) {
            modelService.delete(model.getId());
        }
        return new ResponseEntity<>(models, HttpStatus.OK);
    }
}
