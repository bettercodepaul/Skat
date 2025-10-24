package com.skat.backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Test configuration for PostgreSQL Testcontainers integration.
 * Following ADR-012, this configuration provides a PostgreSQL container
 * for integration tests with automatic lifecycle management by Spring Boot 3.1+.
 */
@TestConfiguration(proxyBeanMethods = false)
public class PostgresTestcontainersConfig {

	/**
	 * Provides a PostgreSQL 18 container for integration tests.
	 * The @ServiceConnection annotation automatically configures the datasource properties.
	 * Spring Boot 3.1+ handles container start/stop lifecycle automatically.
	 *
	 * @return PostgreSQL container instance
	 */
	@Bean
	@ServiceConnection
	PostgreSQLContainer<?> postgres() {
		return new PostgreSQLContainer<>("postgres:18");
		// No start() here! Spring Boot 3.1+ starts/stops the container automatically.
	}
}
