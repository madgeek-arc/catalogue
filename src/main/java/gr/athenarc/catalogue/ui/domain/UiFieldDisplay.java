package gr.athenarc.catalogue.ui.domain;

public class UiFieldDisplay {

    String id;
    Display display;

    public UiFieldDisplay() {
        // no arg constructor
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Display getDisplay() {
        return display;
    }

    public void setDisplay(Display display) {
        this.display = display;
    }
}
