package gr.athenarc.catalogue;

import gr.athenarc.catalogue.config.CatalogueConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(
        scanBasePackageClasses = { CatalogueConfiguration.class },
        exclude = { DataSourceAutoConfiguration.class })
public class CatalogueApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatalogueApplication.class, args);
	}

}
