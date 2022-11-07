package gr.athenarc.catalogue.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "catalogue-lib")
public class CatalogueLibProperties {

    private CatalogueValidation validation = new CatalogueValidation();

    public CatalogueValidation getValidation() {
        return validation;
    }

    public void setValidation(CatalogueValidation validation) {
        this.validation = validation;
    }

    public static class CatalogueValidation {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
