package com.bancatlan.atmauthorizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;



@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "sqlEntityManagerFactory", transactionManagerRef = "sqlTransactionManager",
        basePackages = { "com.bancatlan.atmauthorizer.repo"})
public class SQLserverConfig {
    @Autowired
    private Environment env;

    @Bean(name = "sqlDataSource")
    public DataSource sqlDataSource() {
        System.out.println("HOLA ESTA ES LA CONEXION CON SQL SERVER \n");
//        System.out.println("AMBIENTE: "+ springProfileActive);
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(env.getProperty("spring.datasource.url"));
        dataSource.setUsername(env.getProperty			("spring.datasource.username"));
        dataSource.setPassword(env.getProperty			("spring.datasource.password"));
        dataSource.setDriverClassName(env.getProperty	("spring.datasource.driver-class-name"));
        return dataSource;
    }

    @Primary
    @Bean(name = "sqlEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(sqlDataSource());
        em.setPackagesToScan("com.bancatlan.atmauthorizer.model");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<String, Object>();
//        properties.put("hibernate.hbm2ddl.auto", env.getProperty("sql.jpa.hibernate.none"));
//        properties.put("hibernate.show-sql", env.getProperty("sql.jpa.show-sql"));
//        properties.put("hibernate.dialect", env.getProperty("sql.jpa.properties.hibernate.dialect"));


        em.setJpaPropertyMap(properties);


        return em;

    }
    @Primary
    @Bean(name = "sqlTransactionManager")
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());

        return transactionManager;

    }

}
