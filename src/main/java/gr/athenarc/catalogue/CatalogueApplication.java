package gr.athenarc.catalogue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class IntelcompRegistryApplication {

	public static void main(String[] args) {
		SpringApplication.run(IntelcompRegistryApplication.class, args);
	}

}
