package gr.uoa.di.madgik.catalogue.ui.domain.types;

public class UrlParameter {

    private String placeholder;
    private String valueFromField;

    public UrlParameter() {
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getValueFromField() {
        return valueFromField;
    }

    public void setValueFromField(String valueFromField) {
        this.valueFromField = valueFromField;
    }
}
