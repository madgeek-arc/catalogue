package gr.athenarc.catalogue.ui.domain;

public class UiFieldForm {

    String id;
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

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }
}
