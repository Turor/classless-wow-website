package turoran.classless.webserver.config;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "turoran.classless.webserver.repository.mysql",
        entityManagerFactoryRef = "mysqlEntityManagerFactory",
        transactionManagerRef = "mysqlTransactionManager"
)
public class MySqlDataSourceConfig {

    @Primary
    @Bean(name = "mysqlDataSourceProperties")
    public DataSourceProperties mysqlDataSourceProperties(Environment env) {
        DataSourceProperties p = new DataSourceProperties();
        p.setUrl(env.getProperty("spring.datasource.mysql.url"));
        p.setUsername(env.getProperty("spring.datasource.mysql.username"));
        p.setPassword(env.getProperty("spring.datasource.mysql.password"));
        p.setDriverClassName(env.getProperty("spring.datasource.mysql.driver-class-name"));
        return p;
    }

    @Primary
    @Bean(name = "mysqlDataSource")
    public DataSource mysqlDataSource(@Qualifier("mysqlDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Primary
    @Bean(name = "mysqlEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean mysqlEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("mysqlDataSource") DataSource ds,
            Environment env) {

        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.mysql.hibernate.ddl-auto", "none"));
        props.put("hibernate.dialect", env.getProperty("spring.jpa.mysql.properties.hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect"));

        return builder
                .dataSource(ds)
                .packages("turoran.classless.webserver.repository.mysql.entities")
                .persistenceUnit("mysqlPU")
                .properties(props)
                .build();
    }

    @Primary
    @Bean(name = "mysqlTransactionManager")
    public PlatformTransactionManager mysqlTransactionManager(
            @Qualifier("mysqlEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}