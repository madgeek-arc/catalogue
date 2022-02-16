package gr.athenarc.catalogue.ui.config;

import com.zaxxer.hikari.HikariDataSource;
import gr.athenarc.catalogue.CatalogueApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan(basePackages = {
        "gr.athenarc.catalogue.ui",
},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = CatalogueApplication.class)
        })
@EntityScan(basePackages = "gr.athenarc.catalogue.ui.domain")
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "catalogueEntityManagerFactory",
        transactionManagerRef = "catalogueTransactionManager",
        basePackages = {"gr.athenarc.catalogue.ui.dao"})
public class DatabaseConfig {

    @Autowired
    private Environment environment;

    @Bean(name = "catalogueDataSourceProperties")
    @ConfigurationProperties("spring.datasource-catalogue")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "catalogueDataSource")
    @ConfigurationProperties("spring.datasource-catalogue.configuration")
    public HikariDataSource dataSource(@Qualifier("catalogueDataSourceProperties") DataSourceProperties catalogueDataSourceProperties) {
        return catalogueDataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean(name = "catalogueEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean catalogueEntityManagerFactory(
            EntityManagerFactoryBuilder catalogueEntityManagerFactoryBuilder, @Qualifier("catalogueDataSource") DataSource authDataSource) {

        Map<String, String> catalogueJpaProperties = new HashMap<>();
        catalogueJpaProperties.put("hibernate.dialect", environment.getRequiredProperty("spring.jpa-catalogue.hibernate.dialect"));
        catalogueJpaProperties.put("hibernate.hbm2ddl.auto", environment.getRequiredProperty("spring.jpa-catalogue.hibernate.hbm2ddl.auto"));

        return catalogueEntityManagerFactoryBuilder
                .dataSource(authDataSource)
                .packages("gr.athenarc.catalogue.ui.domain")
                .persistenceUnit("catalogueDataSource")
                .properties(catalogueJpaProperties)
                .build();
    }

    @Bean(name = "catalogueTransactionManager")
    public PlatformTransactionManager catalogueTransactionManager(
            @Qualifier("catalogueEntityManagerFactory") EntityManagerFactory catalogueEntityManagerFactory) {
        return new JpaTransactionManager(catalogueEntityManagerFactory);
    }

}
