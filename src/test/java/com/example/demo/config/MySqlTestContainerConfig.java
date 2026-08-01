package com.example.demo.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class MySqlTestContainerConfig {

    @Bean
    @ServiceConnection
    public MySQLContainer mysqlContainer() {
        return new MySQLContainer("mysql:8.4")
                .withDatabaseName("community_test")
                .withUsername("test_user")
                .withPassword("test_password");
    }
}