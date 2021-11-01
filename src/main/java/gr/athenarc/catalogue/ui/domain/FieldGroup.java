package gr.athenarc.catalogue.ui.domain;

import java.util.ArrayList;
import java.util.List;

public class FieldGroup {

    private UiField field;
    private List<FieldGroup> subFieldGroups = new ArrayList<>();

    public FieldGroup() {}

    public FieldGroup(UiField field) {
        this.field = field;
    }

    public FieldGroup(UiField field, List<FieldGroup> subFieldGroups) {
        this.field = field;
        this.subFieldGroups = subFieldGroups;
    }

    public UiField getField() {
        return field;
    }

    public void setField(UiField field) {
        this.field = field;
    }

    public List<FieldGroup> getSubFieldGroups() {
        return subFieldGroups;
    }

    public void setSubFieldGroups(List<FieldGroup> subFieldGroups) {
        this.subFieldGroups = subFieldGroups;
    }
}
