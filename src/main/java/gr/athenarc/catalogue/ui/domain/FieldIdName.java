package gr.athenarc.catalogue.ui.domain;

public class FieldIdName {

    String id;
    String name = null;

    public FieldIdName() {
    }

    public FieldIdName(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
