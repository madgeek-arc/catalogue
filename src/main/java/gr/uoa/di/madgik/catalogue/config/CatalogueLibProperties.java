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
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;

@ConfigurationProperties(prefix = "catalogue-lib")
@Validated
public class CatalogueLibProperties {

    /**
     * Model-based catalogue behavior.
     */
    @Valid
    private ModelProperties model = new ModelProperties();

    /**
     * Settings of the generic data request endpoint ({@code POST /request/default}).
     */
    private DataRequestProperties dataRequest = new DataRequestProperties();

    /**
     * Switches for the library's web beans.
     */
    private WebProperties web = new WebProperties();

    public ModelProperties getModel() {
        return model;
    }

    public void setModel(ModelProperties model) {
        this.model = model;
    }

    public DataRequestProperties getDataRequest() {
        return dataRequest;
    }

    public void setDataRequest(DataRequestProperties dataRequest) {
        this.dataRequest = dataRequest;
    }

    public WebProperties getWeb() {
        return web;
    }

    public void setWeb(WebProperties web) {
        this.web = web;
    }

    public static class WebProperties {

        /**
         * Model management endpoints ({@code /forms/**}).
         */
        private Toggle forms = new Toggle();

        /**
         * Generic data request endpoint ({@code POST /request/default}).
         */
        private Toggle dataRequest = new Toggle();

        /**
         * Exception advice that renders errors as {@code ProblemDetail}.
         */
        private Toggle errorHandler = new Toggle();

        public Toggle getForms() {
            return forms;
        }

        public void setForms(Toggle forms) {
            this.forms = forms;
        }

        public Toggle getDataRequest() {
            return dataRequest;
        }

        public void setDataRequest(Toggle dataRequest) {
            this.dataRequest = dataRequest;
        }

        public Toggle getErrorHandler() {
            return errorHandler;
        }

        public void setErrorHandler(Toggle errorHandler) {
            this.errorHandler = errorHandler;
        }
    }

    public static class Toggle {

        /**
         * Whether the bean is registered.
         */
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class DataRequestProperties {

        /**
         * Whether requests may target loopback, private, link-local and other non-public addresses.
         */
        private boolean allowPrivateAddresses = false;

        /**
         * Timeout for connecting to the requested URL.
         */
        private Duration connectTimeout = Duration.ofSeconds(10);

        /**
         * Timeout for receiving the response of the requested URL.
         */
        private Duration responseTimeout = Duration.ofSeconds(30);

        public boolean isAllowPrivateAddresses() {
            return allowPrivateAddresses;
        }

        public void setAllowPrivateAddresses(boolean allowPrivateAddresses) {
            this.allowPrivateAddresses = allowPrivateAddresses;
        }

        public Duration getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public Duration getResponseTimeout() {
            return responseTimeout;
        }

        public void setResponseTimeout(Duration responseTimeout) {
            this.responseTimeout = responseTimeout;
        }
    }

    @ValidCatalogueValidation
    public static class CatalogueValidation {

        /**
         * Whether to enable validation of posted resources based on their {@link Model}.
         */
        private boolean enabled = true;

        /**
         * The base url for validating vocabularies (if absolute path is not provided in the field).
         */
        private String baseUrl;

        /**
         * Whether URL field values may point to loopback, private, link-local and other non-public addresses.
         * Vocabulary checks against the configured base url or vocabulary urls are not affected.
         */
        private boolean allowPrivateAddresses = false;

        public boolean isAllowPrivateAddresses() {
            return allowPrivateAddresses;
        }

        public void setAllowPrivateAddresses(boolean allowPrivateAddresses) {
            this.allowPrivateAddresses = allowPrivateAddresses;
        }

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
        @Valid
        private CatalogueValidation validation = new CatalogueValidation();

        /**
         * Resource type synchronization behavior for model create/update operations.
         */
        @Valid
        private ResourceTypeSyncProperties resourceTypeSync = new ResourceTypeSyncProperties();

        /**
         * Maximum age of a cached snippet template; bounds how long a template can be stale when its model changes
         * without the cache being told.
         */
        private Duration snippetTemplateTtl = Duration.ofMinutes(10);

        public Duration getSnippetTemplateTtl() {
            return snippetTemplateTtl;
        }

        public void setSnippetTemplateTtl(Duration snippetTemplateTtl) {
            this.snippetTemplateTtl = snippetTemplateTtl;
        }

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

        /**
         * Executor that applies resource type updates in the background.
         */
        @Valid
        private ExecutorProperties executor = new ExecutorProperties();

        public ExecutorProperties getExecutor() {
            return executor;
        }

        public void setExecutor(ExecutorProperties executor) {
            this.executor = executor;
        }

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

    public static class ExecutorProperties {

        /**
         * Number of threads. With more than one thread, updates of the same model may be applied
         * out of order.
         */
        @Positive
        private int poolSize = 1;

        /**
         * Number of pending updates kept before further updates run on the caller's thread.
         */
        @Positive
        private int queueCapacity = 100;

        public int getPoolSize() {
            return poolSize;
        }

        public void setPoolSize(int poolSize) {
            this.poolSize = poolSize;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }
    }

    @Documented
    @Constraint(validatedBy = CatalogueValidationValidator.class)
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ValidCatalogueValidation {

        String message() default "catalogue-lib.model.validation.base-url is required when catalogue-lib.model.validation.enabled is true";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }

    public static class CatalogueValidationValidator implements ConstraintValidator<ValidCatalogueValidation, CatalogueValidation> {

        @Override
        public boolean isValid(CatalogueValidation validation, ConstraintValidatorContext context) {
            if (validation == null || !validation.isEnabled() || hasText(validation.getBaseUrl())) {
                return true;
            }

            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("baseUrl")
                    .addConstraintViolation();
            return false;
        }

        private boolean hasText(String value) {
            return value != null && !value.isBlank();
        }
    }
}
