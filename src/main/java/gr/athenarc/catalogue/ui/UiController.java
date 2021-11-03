package gr.athenarc.catalogue.ui;

import gr.athenarc.catalogue.ui.domain.FieldGroup;
import gr.athenarc.catalogue.ui.domain.GroupedFields;
import gr.athenarc.catalogue.ui.domain.UiField;
import gr.athenarc.catalogue.ui.service.UiFieldsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("ui")
public class UiController {

    private final UiFieldsService uiFieldsService;

    @Autowired
    public UiController(UiFieldsService uiFieldsService) {
        this.uiFieldsService = uiFieldsService;
    }

    @GetMapping(value = "{className}/fields/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UiField> createFields(@PathVariable("className") String name) throws ClassNotFoundException {
        return uiFieldsService.createFields(name, null);
    }

    @GetMapping(value = "{className}/fields", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UiField> getFields(@PathVariable("className") String name) {
        return uiFieldsService.getFields();
    }

    @GetMapping(value = "form/model/flat", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GroupedFields<UiField>> getFlatModel() {
        return uiFieldsService.getFlatModel();
    }

    @GetMapping(value = "form/model", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GroupedFields<FieldGroup>> getModel() {
        return uiFieldsService.getModel();
    }

}
