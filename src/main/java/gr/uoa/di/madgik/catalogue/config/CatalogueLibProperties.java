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
import jakarta.validation.constraints.AssertTrue;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "catalogue-lib")
@Validated
public class CatalogueLibProperties {

    /**
     * Model-based catalogue behavior.
     */
    private ModelProperties model = new ModelProperties();

    @AssertTrue(message = "catalogue-lib.model.validation.base-url is required when catalogue-lib.model.validation.enabled is true")
    public boolean isModelValidationBaseUrlConfigured() {
        return !model.getValidation().isEnabled()
                || (model.getValidation().getBaseUrl() != null && !model.getValidation().getBaseUrl().isBlank());
    }

    public ModelProperties getModel() {
        return model;
    }

    public void setModel(ModelProperties model) {
        this.model = model;
    }

    public static class CatalogueValidation {

        /**
         * Whether to enable validation of posted resources based on their {@link Model}.
         */
        private boolean enabled = true;

        /**
         * The base url for validating vocabularies (if absolute path is not provided in the field).
         */
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

    public static class ModelProperties {

        /**
         * Validation-based properties.
         */
        private CatalogueValidation validation = new CatalogueValidation();

        /**
         * Resource type synchronization behavior for model create/update operations.
         */
        private ResourceTypeSyncProperties resourceTypeSync = new ResourceTypeSyncProperties();

        public CatalogueValidation getValidation() {
            return validation;
        }

        public void setValidation(CatalogueValidation validation) {
            this.validation = validation;
        }

        public ResourceTypeSyncProperties getResourceTypeSync() {
            return resourceTypeSync;
        }

        public void setResourceTypeSync(ResourceTypeSyncProperties resourceTypeSync) {
            this.resourceTypeSync = resourceTypeSync;
        }
    }

    public static class ResourceTypeSyncProperties {

        /**
         * Whether creating a model should also create the mapped Registry resource type.
         */
        private boolean onCreate = false;

        /**
         * Whether updating a model should also update the mapped Registry resource type.
         */
        private boolean onUpdate = false;

        public boolean isOnCreate() {
            return onCreate;
        }

        public void setOnCreate(boolean onCreate) {
            this.onCreate = onCreate;
        }

        public boolean isOnUpdate() {
            return onUpdate;
        }

        public void setOnUpdate(boolean onUpdate) {
            this.onUpdate = onUpdate;
        }
    }
}
