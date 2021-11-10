package gr.athenarc.catalogue.ui.controller;

import gr.athenarc.catalogue.ui.domain.UiField;
import gr.athenarc.catalogue.ui.service.UiFieldsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("fields")
public class UiFieldsController {

    private final UiFieldsService uiFieldsService;

    @Autowired
    public UiFieldsController(UiFieldsService uiFieldsService) {
        this.uiFieldsService = uiFieldsService;
    }

    @PostMapping()
    public ResponseEntity<UiField> add(@RequestBody UiField field) {
        return new ResponseEntity<>(uiFieldsService.addField(field), HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    public ResponseEntity<UiField> update(@PathVariable("id") String id, @RequestBody UiField field) {
        return new ResponseEntity<>(uiFieldsService.updateField(id, field), HttpStatus.CREATED);
    }

    @GetMapping("{id}")
    public ResponseEntity<UiField> get(@PathVariable("id") String id) {
        return new ResponseEntity<>(uiFieldsService.getField(id), HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<UiField>> getFields() {
        return new ResponseEntity<>(uiFieldsService.getFields(), HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        uiFieldsService.deleteField(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importFields(@RequestBody List<UiField> fields) {
        for (UiField field : fields) {
            uiFieldsService.addField(field);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/all")
    public ResponseEntity<List<UiField>> deleteAll() {
        List<UiField> fields = uiFieldsService.getFields();
        for (UiField field : fields) {
            uiFieldsService.deleteField(field.getId());
        }
        return new ResponseEntity<>(fields, HttpStatus.OK);
    }
}
