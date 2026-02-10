package gr.uoa.di.madgik.catalogue.ui.domain.types;

import java.net.URI;
import java.util.List;

public final class VocabularyProperties implements TypeProperties {

    /**
     * The url to fetch vocabulary values.
     * It may contain parameters as variables, which will be replaced by the values found in the urlParams field.
     * For example: url: "http://localhost/${var1}/items?filter=${var2}"
     */
    URI url;

    /**
     * The field name/path of the vocabulary's "ID".
     */
    String idField = "id";

    /**
     * The field name/path of the vocabulary's "label".
     */
    String labelField;

    /**
     * List of parameters (path/query) to apply on the url.
     */
    List<UrlParameter> urlParams;


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

    public List<UrlParameter> getUrlParams() {
        return urlParams;
    }

    public void setUrlParams(List<UrlParameter> urlParams) {
        this.urlParams = urlParams;
    }
}
