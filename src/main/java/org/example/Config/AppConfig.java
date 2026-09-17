package org.example.Config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Конфигурация Spring-приложения
 */
@Configuration
@PropertySource("classpath:application.properties")
@ComponentScan(
        basePackages = "org.example",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Controller.class),
                @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = RestController.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)
        }
)
@EnableJpaRepositories(basePackages = "org.example.Repository")
@EnableTransactionManagement
public class AppConfig {
    /**
     * Создания бина-пула соединений к БД
     *
     * @param url      jdbc-url бд
     * @param username логин для подключения к бд
     * @param password пароль для подключения к бд
     * @return пул соединений HikariCP
     */
    @Bean
    public DataSource dataSource(@Value("${db.url}") String url,
                                 @Value("${db.username}") String username,
                                 @Value("${db.password}") String password) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setDriverClassName("org.postgresql.Driver");
        return new HikariDataSource(hikariConfig);
    }

    /**
     * Создание бина-фабрики
     *
     * @param dataSource  пул соединений с БД
     * @param dialect     диалект Hibernate для конкретной СУБД
     * @param generateDdl флаг генерации DDL
     * @param hbm2ddlAuto стратегия генерации схемы
     * @param showSql     флаг логирования sql-запросов
     * @return настроенная фабрика
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,
                                                                       @Value("${hibernate.dialect}") String dialect,
                                                                       @Value("${hibernate.generate_ddl:false}") String generateDdl,
                                                                       @Value("${hibernate.hbm2ddl.auto}") String hbm2ddlAuto,
                                                                       @Value("${hibernate.show_sql:true}") String showSql
    ) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(Boolean.parseBoolean(generateDdl));
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("org.example.Entity");
        emf.setJpaVendorAdapter(vendorAdapter);
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", dialect);
        properties.setProperty("hibernate.hbm2ddl.auto", hbm2ddlAuto);
        properties.setProperty("hibernate.show_sql", showSql);
        emf.setJpaProperties(properties);
        return emf;
    }

    /**
     * Создание бина-менеджера транзакций
     *
     * @param entityManagerFactory фабрика, к которой привязан менеджер транзакций
     * @return менеджер транзакций JPA
     */
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public SpringLiquibase liquibase(
            DataSource dataSource,
            @Value("${liquibase.changelog}") String changeLog,
            @Value("${liquibase.enabled:true}") boolean enabled
    ) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(changeLog);
        liquibase.setShouldRun(enabled);
        return liquibase;
    }

}
