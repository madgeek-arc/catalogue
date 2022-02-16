package gr.athenarc.catalogue.ui.domain;

import java.util.List;

public class GroupedFields <T> {

    private Section section;
    private List<T> fields;
    private RequiredFields required;

    public GroupedFields() {
    }

    public Section getGroup() {
        return section;
    }

    public void setGroup(Section section) {
        this.section = section;
    }

    public List<T> getFields() {
        return fields;
    }

    public void setFields(List<T> fields) {
        this.fields = fields;
    }

    public RequiredFields getRequired() {
        return required;
    }

    public void setRequired(RequiredFields required) {
        this.required = required;
    }
}
