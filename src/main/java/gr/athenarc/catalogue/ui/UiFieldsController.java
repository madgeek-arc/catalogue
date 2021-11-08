package gr.athenarc.catalogue.ui;

import gr.athenarc.catalogue.ui.domain.UiField;
import gr.athenarc.catalogue.ui.service.SimpleUiFieldService;
import gr.athenarc.catalogue.ui.service.UiFieldsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("fields")
public class UiFieldsController {

    private final UiFieldsService uiFieldsService;

    @Autowired
    public UiFieldsController(UiFieldsService uiFieldsService) {
        this.uiFieldsService = uiFieldsService;
    }

    @GetMapping("{id}")
    public UiField get(@PathVariable("id") Integer id) {
        return uiFieldsService.getField(id);
    }

    @PostMapping()
    public UiField get(@RequestBody UiField field) {
//        return uiFieldsService.addField();
        return null;
    }


}
