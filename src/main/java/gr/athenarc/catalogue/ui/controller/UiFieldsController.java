package gr.athenarc.catalogue.ui.controller;

import gr.athenarc.catalogue.ui.domain.Group;
import gr.athenarc.catalogue.ui.domain.Survey;
import gr.athenarc.catalogue.ui.domain.UiField;
import gr.athenarc.catalogue.ui.service.UiFieldsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping()
public class UiFieldsController {

    private final UiFieldsService uiFieldsService;

    @Autowired
    public UiFieldsController(UiFieldsService uiFieldsService) {
        this.uiFieldsService = uiFieldsService;
    }

    @PostMapping("fields")
    public ResponseEntity<UiField> addField(@RequestBody UiField field) {
        return new ResponseEntity<>(uiFieldsService.addField(field), HttpStatus.CREATED);
    }

    @PutMapping("fields/{id}")
    public ResponseEntity<UiField> updateField(@PathVariable("id") String id, @RequestBody UiField field) {
        return new ResponseEntity<>(uiFieldsService.updateField(id, field), HttpStatus.OK);
    }

    @GetMapping("fields/{id}")
    public ResponseEntity<UiField> getField(@PathVariable("id") String id) {
        return new ResponseEntity<>(uiFieldsService.getField(id), HttpStatus.OK);
    }

    @GetMapping("fields")
    public ResponseEntity<List<UiField>> getFields() {
        return new ResponseEntity<>(uiFieldsService.getFields(), HttpStatus.OK);
    }

    @DeleteMapping("fields/{id}")
    public ResponseEntity<Void> deleteField(@PathVariable("id") String id) {
        uiFieldsService.deleteField(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("fields/import")
    public ResponseEntity<Void> importFields(@RequestBody List<UiField> fields) {
        for (UiField field : fields) {
            uiFieldsService.addField(field);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("fields/all")
    public ResponseEntity<List<UiField>> deleteAllFields() {
        List<UiField> fields = uiFieldsService.getFields();
        for (UiField field : fields) {
            uiFieldsService.deleteField(field.getId());
        }
        return new ResponseEntity<>(fields, HttpStatus.OK);
    }


    @PostMapping("groups")
    public ResponseEntity<Group> addGroup(@RequestBody Group group) {
        return new ResponseEntity<>(uiFieldsService.addGroup(group), HttpStatus.CREATED);
    }

    @PutMapping("groups/{id}")
    public ResponseEntity<Group> updateGroup(@PathVariable("id") String id, @RequestBody Group group) {
        return new ResponseEntity<>(uiFieldsService.updateGroup(id, group), HttpStatus.OK);
    }

    @GetMapping("groups/{id}")
    public ResponseEntity<Group> getGroup(@PathVariable("id") String id) {
        return new ResponseEntity<>(uiFieldsService.getGroup(id), HttpStatus.OK);
    }

    @GetMapping("groups")
    public ResponseEntity<List<Group>> getGroups() {
        return new ResponseEntity<>(uiFieldsService.getGroups(), HttpStatus.OK);
    }

    @DeleteMapping("groups/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable("id") String id) {
        uiFieldsService.deleteGroup(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("groups/import")
    public ResponseEntity<Void> importGroups(@RequestBody List<Group> groups) {
        for (Group group : groups) {
            uiFieldsService.addGroup(group);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("groups/all")
    public ResponseEntity<List<Group>> deleteAllGroups() {
        List<Group> groups = uiFieldsService.getGroups();
        for (Group group : groups) {
            uiFieldsService.deleteField(group.getId());
        }
        return new ResponseEntity<>(groups, HttpStatus.OK);
    }


    @PostMapping("surveys")
    public ResponseEntity<Survey> addSurvey(@RequestBody Survey group) {
        return new ResponseEntity<>(uiFieldsService.addSurvey(group), HttpStatus.CREATED);
    }

    @PutMapping("surveys/{id}")
    public ResponseEntity<Survey> updateSurvey(@PathVariable("id") String id, @RequestBody Survey survey) {
        return new ResponseEntity<>(uiFieldsService.updateSurvey(id, survey), HttpStatus.OK);
    }

    @GetMapping("surveys/{id}")
    public ResponseEntity<Survey> getSurvey(@PathVariable("id") String id) {
        return new ResponseEntity<>(uiFieldsService.getSurvey(id), HttpStatus.OK);
    }

    @GetMapping("surveys")
    public ResponseEntity<List<Survey>> getSurveys() {
        return new ResponseEntity<>(uiFieldsService.getSurveys(), HttpStatus.OK);
    }

    @DeleteMapping("surveys/{id}")
    public ResponseEntity<Void> deleteSurvey(@PathVariable("id") String id) {
        uiFieldsService.deleteSurvey(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("surveys/all")
    public ResponseEntity<List<Survey>> deleteAllSurveys() {
        List<Survey> surveys = uiFieldsService.getSurveys();
        for (Survey survey : surveys) {
            uiFieldsService.deleteSurvey(survey.getId());
        }
        return new ResponseEntity<>(surveys, HttpStatus.OK);
    }
}
