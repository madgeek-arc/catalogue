package gr.athenarc.catalogue.config;

import org.springframework.stereotype.Component;

@Component
public class LibConfiguration implements CatalogueLibConfiguration {

    @Override
    public String generatedClassesPackageName() {
        // TODO: refactor (find a way to get the package from pom? even for dependent projects)
        return "gr.athenarc.xsd2java";
    }
}
