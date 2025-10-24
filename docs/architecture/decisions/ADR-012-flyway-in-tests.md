# ADR-012: Use Flyway Scripts for Database Initialization in Integration Tests

Date: 2025-10-24

## Status

Accepted

## Context

Integration tests (IT) require a consistent and reliable database state to ensure accurate and
reproducible results. Currently, database initialization for integration tests is managed manually
or through ad-hoc scripts, which can lead to inconsistencies and errors. Using the same Flyway
scripts for both production and integration tests ensures that the database schema and data are
consistent across environments.

## Decision

Flyway scripts will be used to initialize the database for integration tests. This ensures that the
database schema and data are consistent with the production environment, reducing the risk of
discrepancies and improving test reliability.

## Consequences

- **Positive**:
    - Ensures consistency between production and test environments.
    - Reduces duplication of effort in maintaining separate initialization scripts.
    - Simplifies debugging by using the same schema and data definitions.

- **Negative**:
    - May increase test setup time due to Flyway migrations.
    - Requires integration tests to handle potential migration errors.

## Implementation

1. Configure Flyway in the `application-test.properties` file:
   ```properties
   spring.flyway.enabled=true
   spring.flyway.locations=classpath:db/migration
   spring.flyway.clean-on-validation-error=true
   spring.flyway.baseline-on-migrate=true
   ```

2. Ensure that the integration test database is cleaned and migrated before each test run:
    - Use the `@BeforeEach` or `@BeforeAll` lifecycle methods to trigger Flyway migrations.
    - Example:
      ```java
      @SpringBootTest
      public class ExampleIntegrationTest {
 
          @Autowired
          private Flyway flyway;
 
          @BeforeEach
          public void setupDatabase() {
              flyway.clean();
              flyway.migrate();
          }
 
          @Test
          public void testExample() {
              // Test logic here
          }
      }
      ```
