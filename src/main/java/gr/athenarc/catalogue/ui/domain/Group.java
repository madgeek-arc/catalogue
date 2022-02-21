package gr.athenarc.catalogue.ui.domain;

import java.util.List;

public class Group {

    String id;
    String name;
    String description;
    int order;
    List<UiField> fields;

    public Group() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public List<UiField> getFields() {
        return fields;
    }

    public void setFields(List<UiField> fields) {
        this.fields = fields;
    }
}
