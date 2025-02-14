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
package gr.uoa.di.madgik.catalogue.ui.controller;

import gr.uoa.di.madgik.catalogue.ui.domain.Display;
import gr.uoa.di.madgik.catalogue.ui.domain.Form;
import gr.uoa.di.madgik.catalogue.ui.domain.UiFieldDisplay;
import gr.uoa.di.madgik.catalogue.ui.domain.UiFieldForm;
import gr.uoa.di.madgik.catalogue.ui.service.FormDisplayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("forms/models/")
public class FormFieldsController {

    private static final Logger logger = LoggerFactory.getLogger(FormFieldsController.class);

    private final FormDisplayService formDisplayService;

    public FormFieldsController(FormDisplayService formDisplayService) {
        this.formDisplayService = formDisplayService;
    }


    @PostMapping("/fields/forms")
    public ResponseEntity<Void> importForms(@RequestBody List<UiFieldForm> forms) {
        for (UiFieldForm form : forms) {
            formDisplayService.saveForm(form.getModelId(), form.getId(), form.getForm());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("{model}/fields/{field}/form")
    public ResponseEntity<UiFieldForm> updateField(@PathVariable("model") String model, @PathVariable("field") String field, @RequestBody Form form) {
        return new ResponseEntity<>(formDisplayService.saveForm(model, field, form), HttpStatus.OK);
    }

    @GetMapping("{model}/fields/{field}/form")
    public ResponseEntity<Form> getForm(@PathVariable("model") String model, @PathVariable("field") String field) {
        return new ResponseEntity<>(formDisplayService.getForm(model, field), HttpStatus.OK);
    }

    @DeleteMapping("{model}/fields/{field}/form")
    public ResponseEntity<Void> deleteForm(@PathVariable("model") String model, @PathVariable("field") String field) {
        formDisplayService.deleteForm(model, field);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PostMapping("/fields/displays")
    public ResponseEntity<Void> importDisplays(@RequestBody List<UiFieldDisplay> displaysList) {
        for (UiFieldDisplay display : displaysList) {
            formDisplayService.saveDisplay(display.getModelId(), display.getId(), display.getDisplay());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("{model}/fields/{field}/display")
    public ResponseEntity<UiFieldDisplay> updateDisplay(@PathVariable("model") String model, @PathVariable("field") String field, @RequestBody Display display) {
        return new ResponseEntity<>(formDisplayService.saveDisplay(model, field, display), HttpStatus.OK);
    }

    @GetMapping("{model}/fields/{field}/display")
    public ResponseEntity<Display> getDisplay(@PathVariable("model") String model, @PathVariable("field") String field) {
        return new ResponseEntity<>(formDisplayService.getDisplay(model, field), HttpStatus.OK);
    }

    @DeleteMapping("{model}/fields/{field}/display")
    public ResponseEntity<Void> deleteDisplay(@PathVariable("model") String model, @PathVariable("field") String field) {
        formDisplayService.deleteDisplay(model, field);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
