/*
 * Copyright 2021-2026 OpenAIRE AMKE
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

package gr.uoa.di.madgik.catalogue.config;

import gr.uoa.di.madgik.catalogue.domain.Model;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "catalogue-lib")
public class CatalogueLibProperties {

    /**
     * Validation-based properties.
     */
    private CatalogueValidation validation = new CatalogueValidation();

    @PostConstruct
    void validate() {
        if (validation.isEnabled() && (validation.getBaseUrl() == null || validation.getBaseUrl().isBlank())) {
            throw new RuntimeException("Could not resolve placeholder 'catalogue-lib.validation.base-url' in value \"${catalogue-lib.validation.base-url}\"");
        }
    }

    public CatalogueValidation getValidation() {
        return validation;
    }

    public void setValidation(CatalogueValidation validation) {
        this.validation = validation;
    }

    public static class CatalogueValidation {

        /**
         * Whether to enable validation of posted resources based on their {@link Model}.
         */
        private boolean enabled = true;

        /**
         * The base url for validating vocabularies (if absolute path is not provided in the field).
         */
        @jakarta.validation.constraints.Null
        private String baseUrl;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }
}
