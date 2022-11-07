package gr.athenarc.catalogue.ui.domain;

import javax.persistence.Transient;

public class FieldIdNameValue {

    String id;
    @Transient
    String name = null;
    String value;

    public FieldIdNameValue() {
    }

    public FieldIdNameValue(String id, String name, String value) {
        this.id = id;
        this.name = name;
        this.value = value;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
