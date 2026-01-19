package gr.uoa.di.madgik.catalogue.ui.domain.types;

public final class TextProperties implements TypeProperties {

    int minLength;
    int maxLength;

    public TextProperties() {
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
}
