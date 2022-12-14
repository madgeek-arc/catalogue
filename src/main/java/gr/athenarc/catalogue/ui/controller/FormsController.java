package gr.athenarc.catalogue.ui.controller;

import eu.openminted.registry.core.domain.Browsing;
import eu.openminted.registry.core.domain.FacetFilter;
import gr.athenarc.catalogue.ui.domain.Model;
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

    @ApiImplicitParams({
            @ApiImplicitParam(name = "query", value = "Keyword to refine the search", dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "from", value = "Starting index in the result set", dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "quantity", value = "Quantity to be fetched", dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "order", value = "asc / desc", dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "orderField", value = "Order field", dataTypeClass = String.class, paramType = "query")
    })
    @GetMapping("models")
    public ResponseEntity<Browsing<Model>> getModels(@ApiIgnore @RequestParam Map<String, Object> allRequestParams) {
        String resourceType = (String) allRequestParams.get("resourceType");
        FacetFilter ff = createFacetFilter(allRequestParams);
        ff.addFilter("resourceType", resourceType);
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
