package gr.athenarc.catalogue.ui.domain;

import java.util.List;

public class Section {

    private String id;
    private String name;
    private String description;
    private String subType;
    private int order;
    private List<Section> subSections;
    private List<UiField> fields;

    public Section() {
        // no arg constructor
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

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public List<Section> getSubSections() {
        return subSections;
    }

    public void setSubSections(List<Section> subSections) {
        this.subSections = subSections;
    }

    public List<UiField> getFields() {
        return fields;
    }

    public void setFields(List<UiField> fields) {
        this.fields = fields;
    }
}
