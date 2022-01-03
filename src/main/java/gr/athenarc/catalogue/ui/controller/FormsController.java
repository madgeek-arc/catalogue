package gr.athenarc.catalogue.ui.controller;

import gr.athenarc.catalogue.ui.domain.Group;
import gr.athenarc.catalogue.ui.domain.Survey;
import gr.athenarc.catalogue.ui.domain.UiField;
import gr.athenarc.catalogue.ui.service.FormsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("forms")
public class FormsController {

    private final FormsService formsService;

    @Autowired
    public FormsController(FormsService formsService) {
        this.formsService = formsService;
    }

    @PostMapping("fields")
    public ResponseEntity<UiField> addField(@RequestBody UiField field) {
        return new ResponseEntity<>(formsService.addField(field), HttpStatus.CREATED);
    }

    @PutMapping("fields/{id}")
    public ResponseEntity<UiField> updateField(@PathVariable("id") String id, @RequestBody UiField field) {
        return new ResponseEntity<>(formsService.updateField(id, field), HttpStatus.OK);
    }

    @GetMapping("fields/{id}")
    public ResponseEntity<UiField> getField(@PathVariable("id") String id) {
        return new ResponseEntity<>(formsService.getField(id), HttpStatus.OK);
    }

    @GetMapping("fields")
    public ResponseEntity<List<UiField>> getFields() {
        return new ResponseEntity<>(formsService.getFields(), HttpStatus.OK);
    }

    @DeleteMapping("fields/{id}")
    public ResponseEntity<Void> deleteField(@PathVariable("id") String id) {
        formsService.deleteField(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("fields/import")
    public ResponseEntity<Void> importFields(@RequestBody List<UiField> fields) {
        for (UiField field : fields) {
            formsService.addField(field);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("fields/all")
    public ResponseEntity<List<UiField>> deleteAllFields() {
        List<UiField> fields = formsService.getFields();
        for (UiField field : fields) {
            formsService.deleteField(field.getId());
        }
        return new ResponseEntity<>(fields, HttpStatus.OK);
    }


    @PostMapping("groups")
    public ResponseEntity<Group> addGroup(@RequestBody Group group) {
        return new ResponseEntity<>(formsService.addGroup(group), HttpStatus.CREATED);
    }

    @PutMapping("groups/{id}")
    public ResponseEntity<Group> updateGroup(@PathVariable("id") String id, @RequestBody Group group) {
        return new ResponseEntity<>(formsService.updateGroup(id, group), HttpStatus.OK);
    }

    @GetMapping("groups/{id}")
    public ResponseEntity<Group> getGroup(@PathVariable("id") String id) {
        return new ResponseEntity<>(formsService.getGroup(id), HttpStatus.OK);
    }

    @GetMapping("groups")
    public ResponseEntity<List<Group>> getGroups() {
        return new ResponseEntity<>(formsService.getGroups(), HttpStatus.OK);
    }

    @DeleteMapping("groups/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable("id") String id) {
        formsService.deleteGroup(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("groups/import")
    public ResponseEntity<Void> importGroups(@RequestBody List<Group> groups) {
        for (Group group : groups) {
            formsService.addGroup(group);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("groups/all")
    public ResponseEntity<List<Group>> deleteAllGroups() {
        List<Group> groups = formsService.getGroups();
        for (Group group : groups) {
            formsService.deleteField(group.getId());
        }
        return new ResponseEntity<>(groups, HttpStatus.OK);
    }


    @PostMapping("surveys")
    public ResponseEntity<Survey> addSurvey(@RequestBody Survey survey) {
        return new ResponseEntity<>(formsService.addSurvey(survey), HttpStatus.CREATED);
    }

    @PutMapping("surveys/{id}")
    public ResponseEntity<Survey> updateSurvey(@PathVariable("id") String id, @RequestBody Survey survey) {
        return new ResponseEntity<>(formsService.updateSurvey(id, survey), HttpStatus.OK);
    }

    @GetMapping("surveys/{id}")
    public ResponseEntity<Survey> getSurvey(@PathVariable("id") String id) {
        return new ResponseEntity<>(formsService.getSurvey(id), HttpStatus.OK);
    }

    @GetMapping("surveys")
    public ResponseEntity<List<Survey>> getSurveys() {
        return new ResponseEntity<>(formsService.getSurveys(), HttpStatus.OK);
    }

    @DeleteMapping("surveys/{id}")
    public ResponseEntity<Void> deleteSurvey(@PathVariable("id") String id) {
        formsService.deleteSurvey(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("surveys/all")
    public ResponseEntity<List<Survey>> deleteAllSurveys() {
        List<Survey> surveys = formsService.getSurveys();
        for (Survey survey : surveys) {
            formsService.deleteSurvey(survey.getId());
        }
        return new ResponseEntity<>(surveys, HttpStatus.OK);
    }
}
