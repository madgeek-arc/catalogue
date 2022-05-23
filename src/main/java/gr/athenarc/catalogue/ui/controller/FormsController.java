package gr.athenarc.catalogue.ui.controller;

import eu.openminted.registry.core.domain.Browsing;
import eu.openminted.registry.core.domain.FacetFilter;
import gr.athenarc.catalogue.ui.domain.Model;
import gr.athenarc.catalogue.ui.domain.Section;
import gr.athenarc.catalogue.ui.domain.UiField;
import gr.athenarc.catalogue.ui.service.FormsService;
import gr.athenarc.catalogue.ui.service.ModelService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

import static gr.athenarc.catalogue.controller.GenericItemController.createFacetFilter;

@RestController
@RequestMapping("forms")
public class FormsController {

    private final FormsService formsService;
    private final ModelService modelService;

    @Autowired
    public FormsController(FormsService formsService, ModelService modelService) {
        this.formsService = formsService;
        this.modelService = modelService;
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

    @DeleteMapping("fields/{id}")
    public ResponseEntity<Void> deleteField(@PathVariable("id") String id) {
        formsService.deleteField(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("fields")
    public ResponseEntity<List<UiField>> getFields() {
        return new ResponseEntity<>(formsService.getFields(), HttpStatus.OK);
    }

    @PutMapping("fields")
    public ResponseEntity<List<UiField>> updateFields(@RequestBody List<UiField> fields) {
        return new ResponseEntity<>(formsService.updateFields(fields), HttpStatus.OK);
    }

    @PostMapping("fields/import")
    public ResponseEntity<List<UiField>> importFields(@RequestBody List<UiField> fields) {
        return new ResponseEntity<>(formsService.importFields(fields), HttpStatus.OK);
    }

    @DeleteMapping("fields/all")
    public ResponseEntity<List<UiField>> deleteAllFields() {
        List<UiField> fields = formsService.getFields();
        for (UiField field : fields) {
            formsService.deleteField(field.getId());
        }
        return new ResponseEntity<>(fields, HttpStatus.OK);
    }


    @PostMapping("sections")
    public ResponseEntity<Section> addSection(@RequestBody Section section) {
        return new ResponseEntity<>(formsService.addSection(section), HttpStatus.CREATED);
    }

    @PutMapping("sections/{id}")
    public ResponseEntity<Section> updateSection(@PathVariable("id") String id, @RequestBody Section section) {
        return new ResponseEntity<>(formsService.updateSection(id, section), HttpStatus.OK);
    }

    @GetMapping("sections/{id}")
    public ResponseEntity<Section> getSection(@PathVariable("id") String id) {
        return new ResponseEntity<>(formsService.getSection(id), HttpStatus.OK);
    }

    @DeleteMapping("sections/{id}")
    public ResponseEntity<Void> deleteSection(@PathVariable("id") String id) {
        formsService.deleteSection(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("sections")
    public ResponseEntity<List<Section>> getSections() {
        return new ResponseEntity<>(formsService.getSections(), HttpStatus.OK);
    }

    @PutMapping("sections")
    public ResponseEntity<List<Section>> updateSections(@RequestBody List<Section> sections) {
        return new ResponseEntity<>(formsService.updateSections(sections), HttpStatus.OK);
    }

    @PostMapping("sections/import")
    public ResponseEntity<List<Section>> importSections(@RequestBody List<Section> sections) {
        return new ResponseEntity<>(formsService.importSections(sections), HttpStatus.OK);
    }

    @DeleteMapping("sections/all")
    public ResponseEntity<List<Section>> deleteAllSections() {
        List<Section> sections = formsService.getSections();
        for (Section section : sections) {
            formsService.deleteField(section.getId());
        }
        return new ResponseEntity<>(sections, HttpStatus.OK);
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

    @ApiImplicitParams({
            @ApiImplicitParam(name = "query", value = "Keyword to refine the search", dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "from", value = "Starting index in the result set", dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "quantity", value = "Quantity to be fetched", dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "order", value = "asc / desc", dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "orderField", value = "Order field", dataTypeClass = String.class, paramType = "query")
    })
    @GetMapping("models")
    public ResponseEntity<Browsing<Model>> getModels(@ApiIgnore @RequestParam Map<String, Object> allRequestParams) {
        FacetFilter ff = createFacetFilter(allRequestParams);
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
