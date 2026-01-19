package gr.uoa.di.madgik.catalogue.ui.domain.types;

import java.net.URI;

public final class VocabularyProperties implements TypeProperties {

    URI url;
    String idField = "id";
    String labelField;

    public VocabularyProperties() {
    }

    public URI getUrl() {
        return url;
    }

    public void setUrl(URI url) {
        this.url = url;
    }

    public String getIdField() {
        return idField;
    }

    public void setIdField(String idField) {
        this.idField = idField;
    }

    public String getLabelField() {
        return labelField;
    }

    public void setLabelField(String labelField) {
        this.labelField = labelField;
    }
}
