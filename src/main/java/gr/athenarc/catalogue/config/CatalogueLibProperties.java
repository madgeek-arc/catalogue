package gr.athenarc.catalogue.config;

import gr.athenarc.catalogue.ui.controller.UiController;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "catalogue-lib")
public class CatalogueLibProperties {

    /**
     * Validation-based properties.
     */
    private CatalogueValidation validation = new CatalogueValidation();

    /**
     * Jaxb properties.
     */
    private JaxbProperties jaxb = new JaxbProperties();

    public CatalogueValidation getValidation() {
        return validation;
    }

    public void setValidation(CatalogueValidation validation) {
        this.validation = validation;
    }

    public JaxbProperties getJaxb() {
        return jaxb;
    }

    public void setJaxb(JaxbProperties jaxb) {
        this.jaxb = jaxb;
    }

    public static class CatalogueValidation {

        /**
         * Whether to enable validation of posted resources based on their {@link gr.athenarc.catalogue.ui.domain.Model}.
         */
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class JaxbProperties {

        /**
         * List of packages to scan for classes and add to the {@link javax.xml.bind.JAXBContext}.
         * Any {@link Enum} classes found are considered vocabularies and will be exposed through {@link UiController#getVocabularies()}.
         */
        private List<String> includePackages = new ArrayList<>();

        /**
         * Base package of generated classes. Will be scanned for classes to add to the {@link javax.xml.bind.JAXBContext}.
         * Any {@link Enum} classes found are considered vocabularies and will be exposed through {@link UiController#getVocabularies()}.
         */
        private String generatedClassesPackageName = "gr.athenarc.xsd2java";

        public List<String> getIncludePackages() {
            return includePackages;
        }

        public void setIncludePackages(List<String> includePackages) {
            this.includePackages = includePackages;
        }

        public String getGeneratedClassesPackageName() {
            return generatedClassesPackageName;
        }

        public void setGeneratedClassesPackageName(String generatedClassesPackageName) {
            this.generatedClassesPackageName = generatedClassesPackageName;
        }

        /**
         * Returns a concatenation of {@link CatalogueLibProperties.JaxbProperties#includePackages}
         * and {@link CatalogueLibProperties.JaxbProperties#generatedClassesPackageName}.
         */
        public List<String> getAllPackages() {
            List<String> allPackages = new ArrayList<>(includePackages);
            allPackages.add(generatedClassesPackageName);
            return allPackages;
        }
    }
}
