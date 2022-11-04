package gr.athenarc.catalogue.ui.controller;

import gr.athenarc.catalogue.ui.domain.Display;
import gr.athenarc.catalogue.ui.domain.Form;
import gr.athenarc.catalogue.ui.domain.UiFieldDisplay;
import gr.athenarc.catalogue.ui.domain.UiFieldForm;
import gr.athenarc.catalogue.ui.service.FormDisplayService;
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
