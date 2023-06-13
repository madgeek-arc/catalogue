package gr.athenarc.catalogue.config;

import org.springframework.stereotype.Component;

@Deprecated
@Component
public class LibConfiguration implements CatalogueLibConfiguration {

    @Override
    public String generatedClassesPackageName() {
        return "gr.athenarc.xsd2java";
    }
}
