package gr.athenarc.catalogue.ui.domain;


import java.util.List;

public class Form {

    FieldIdNameValue dependsOn;
    List<FieldIdNameValue> affects = null;
    String group;
    StyledString description;
    StyledString suggestion;
    String placeholder;
    Boolean mandatory;
    Boolean immutable;
    Display display = new Display();


    public Form() {
    }

    public FieldIdNameValue getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(FieldIdNameValue dependsOn) {
        this.dependsOn = dependsOn;
    }

    public List<FieldIdNameValue> getAffects() {
        return affects;
    }

    public void setAffects(List<FieldIdNameValue> affects) {
        this.affects = affects;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public StyledString getDescription() {
        return description;
    }

    public void setDescription(StyledString description) {
        this.description = description;
    }

    public StyledString getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(StyledString suggestion) {
        this.suggestion = suggestion;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public Boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }

    public Boolean getImmutable() {
        return immutable;
    }

    public void setImmutable(Boolean immutable) {
        this.immutable = immutable;
    }

    public Display getDisplay() {
        return display;
    }

    public void setDisplay(Display display) {
        this.display = display;
    }
}
