package gr.uoa.di.madgik.catalogue.ui.domain.types;

public final class UrlProperties implements TypeProperties {

    boolean strictValidation = false;

    public UrlProperties() {
    }

    public boolean isStrictValidation() {
        return strictValidation;
    }

    public void setStrictValidation(boolean strictValidation) {
        this.strictValidation = strictValidation;
    }
}
