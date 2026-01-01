package turoran.classless.webserver.config;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "turoran.classless.webserver.repository.sqlite",
        entityManagerFactoryRef = "sqliteEntityManagerFactory",
        transactionManagerRef = "sqliteTransactionManager"
)
public class SqliteDataSourceConfig {

    @Bean(name = "sqliteDataSourceProperties")
    public DataSourceProperties sqliteDataSourceProperties(Environment env) {
        DataSourceProperties p = new DataSourceProperties();
        p.setUrl(env.getProperty("spring.datasource.sqlite.url"));
        p.setDriverClassName(env.getProperty("spring.datasource.sqlite.driver-class-name", "org.sqlite.JDBC"));
        // SQLite typically does not use username/password
        return p;
    }

    @Bean(name = "sqliteDataSource")
    public DataSource sqliteDataSource(@Qualifier("sqliteDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean(name = "sqliteEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean sqliteEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("sqliteDataSource") DataSource ds,
            Environment env) {

        Map<String, Object> props = new HashMap<>();
        // If using a custom dialect library, set the dialect class here (example)
        props.put("hibernate.dialect", env.getProperty("spring.jpa.sqlite-dialect", "org.hibernate.community.dialect.SQLiteDialect"));
        // For dev convenience; change to validate or none in production
        props.put("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.sqlite-ddl-auto", "update"));

        return builder
                .dataSource(ds)
                .packages("turoran.classless.webserver.repository.sqlite.entities")
                .persistenceUnit("sqlitePU")
                .properties(props)
                .build();
    }

    @Bean(name = "sqliteTransactionManager")
    public PlatformTransactionManager sqliteTransactionManager(
            @Qualifier("sqliteEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}