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

@RestController
@RequestMapping("forms/fields")
public class FormFieldsController {

    private static final Logger logger = LoggerFactory.getLogger(FormFieldsController.class);

    private final FormDisplayService formDisplayService;

    public FormFieldsController(FormDisplayService formDisplayService) {
        this.formDisplayService = formDisplayService;
    }


    @PutMapping("forms/{id}")
    public ResponseEntity<UiFieldForm> updateField(@PathVariable("id") String id, @RequestBody Form form) {
        return new ResponseEntity<>(formDisplayService.saveForm(id, form), HttpStatus.OK);
    }

    @GetMapping("forms/{id}")
    public ResponseEntity<Form> getForm(@PathVariable("id") String id) {
        return new ResponseEntity<>(formDisplayService.getForm(id), HttpStatus.OK);
    }

    @DeleteMapping("forms/{id}")
    public ResponseEntity<Void> deleteForm(@PathVariable("id") String id) {
        formDisplayService.deleteForm(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PutMapping("displays/{id}")
    public ResponseEntity<UiFieldDisplay> updateDisplay(@PathVariable("id") String id, @RequestBody Display display) {
        return new ResponseEntity<>(formDisplayService.saveDisplay(id, display), HttpStatus.OK);
    }

    @GetMapping("displays/{id}")
    public ResponseEntity<Display> getDisplay(@PathVariable("id") String id) {
        return new ResponseEntity<>(formDisplayService.getDisplay(id), HttpStatus.OK);
    }

    @DeleteMapping("displays/{id}")
    public ResponseEntity<Void> deleteDisplay(@PathVariable("id") String id) {
        formDisplayService.deleteDisplay(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
