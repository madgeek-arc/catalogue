package gr.uoa.di.madgik.catalogue.ui.domain.types;

public final class TextProperties implements TypeProperties {

    Integer minLength;
    Integer maxLength;

    public TextProperties() {
    }

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }
}
