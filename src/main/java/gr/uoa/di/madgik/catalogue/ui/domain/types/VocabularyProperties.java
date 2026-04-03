/*
 * Copyright 2026-2026 OpenAIRE AMKE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
