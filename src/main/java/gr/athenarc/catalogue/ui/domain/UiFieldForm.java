package gr.athenarc.catalogue.ui.domain;

public class UiFieldForm {

    String id;
    String modelId;
    Form form;

    public UiFieldForm() {
        // no arg constructor
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }
}
