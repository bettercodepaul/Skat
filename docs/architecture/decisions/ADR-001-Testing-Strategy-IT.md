# ADR-001: Testing Strategy with SpringBootTest and Testcontainers

Date: 2025-10-23

## Status

Accepted

## Context

Integration tests are essential to verify the interaction between multiple components of the system,
including the database, external services, and the application itself. These tests aim to ensure
that the system behaves as expected in a realistic environment.

## Decision

To achieve reliable and maintainable integration tests, we adopt the following tools and practices:

- **@SpringBootTest**: This annotation is used to load the full application context, enabling tests
  to run in an environment close to production.
- **Testcontainers**: We use Testcontainers to provide lightweight, disposable containers for
  external dependencies such as PostgreSQL. This ensures that tests are isolated and reproducible.
- **Given-When-Then**: Tests are structured using the Given-When-Then pattern to improve readability
  and maintainability.
- **AssertJ**: Assertions are written using AssertJ for a fluent and expressive API.

### Key Practices

1. **Database Integration**: Testcontainers is configured to spin up a PostgreSQL container for each
   test run, ensuring a clean and isolated database state.
2. **Configuration Management**: Application properties are overridden in the test environment to
   connect to the Testcontainers-managed database.
3. **Test Coverage**: Focus is placed on testing critical integration points, such as repository
   methods, service-to-service communication, and REST API endpoints.
4. **Performance Considerations**: While integration tests are slower than unit tests, they provide
   higher confidence in the correctness of the system.

## Consequences

- **Advantages**:
    - High confidence in the correctness of the system as a whole.
    - Tests run in an environment that closely resembles production.
    - Reduced risk of integration issues in production.

- **Disadvantages**:
    - Longer execution times compared to unit tests.
    - Increased complexity in test setup and maintenance.

### Maven Configuration (JUnit 5)
```xml
<build>
  <plugins>

    <!-- Integration tests (see ADR-001) -->
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-failsafe-plugin</artifactId>
      <version>3.2.5</version>
      <executions>
        <execution>
          <goals>
            <goal>integration-test</goal>
            <goal>verify</goal>
          </goals>
          <configuration>
            <includes>
              <include>**/*IT.java</include>
              <include>**/*ITCase.java</include>
            </includes>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```
