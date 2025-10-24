# ADR-002: Unit Testing Strategy with Maven Surefire and Mockito
Date: 2025-10-23

## Status
Accepted

## Context
We already use **integration tests** (IT) with **@SpringBootTest** and **Testcontainers** executed by the **Maven Failsafe** plugin (see ADR-001).  
We also need a **fast unit-test lane** to validate pure business logic without starting the Spring context. These tests should run quickly on every build, locally and in CI, and allow **Mockito** for isolation.

## Decision
- **Runner:** Use **Maven Surefire** for unit tests located in `src/test/java`.
- **Scope:** Unit tests cover **pure Java/business logic** that does **not** depend on the Spring container (no autowiring, no Spring configuration).
- **Mockito:** Allowed and encouraged to isolate dependencies; prefer `@ExtendWith(MockitoExtension.class)` over starting the Spring context.
- **Naming/Patterns:**  
  - Unit tests: `*Test.java` (picked up by Surefire).  
  - Integration tests: `*IT.java` or `*ITCase.java` (picked up by Failsafe).
- **Escalation rule:** If a test targets **Spring Boot components** (e.g., configuration, repositories, controllers involving serialization, transactional boundaries, or Spring-managed beans), **prefer an Integration Test** executed by **Failsafe** with `@SpringBootTest` (see ADR-001) rather than mocking the framework.
- **Optional slices:** `@WebMvcTest` or other slices **may** be used sparingly for very focused MVC tests, but the default for Spring components remains **IT**.

## Implementation

### Maven Configuration (JUnit 5)
```xml
<build>
  <plugins>
    <!-- Fast unit tests -->
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-surefire-plugin</artifactId>
      <version>3.2.5</version>
      <configuration>
        <useModulePath>false</useModulePath>
        <includes>
          <include>**/*Test.java</include>
        </includes>
      </configuration>
    </plugin>

   </plugins>
</build>
```

### Example: Pure Unit Test with Mockito (Given–When–Then + AssertJ)
```java
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScoreServiceTest {

  @Mock ScoringRuleEngine ruleEngine;

  @InjectMocks ScoreService service;

  @Test
  void given_valid_input_when_calculate_then_returns_expected_score() {
    // Given
    var input = List.of(10, 5, 6);
    when(ruleEngine.apply(input)).thenReturn(21);

    // When
    int score = service.calculate(input);

    // Then
    assertThat(score).isEqualTo(21);
  }
}
```

### When to Choose Unit vs. Integration
- **Choose Unit (Surefire + Mockito)** when testing:
  - Deterministic business rules, pure functions, simple orchestrations.
  - Behavior that can be isolated with mocks **without** relying on Spring.
- **Choose Integration (Failsafe + @SpringBootTest + Testcontainers)** when testing:
- ** Avooid using 
  - Spring DI, configuration, serialization, validation, repositories, transactions.
  - HTTP layer behavior, real database interactions, or cross-cutting concerns.

## Consequences
- **Pros (Unit lane):** Very fast feedback, cheap to run on every change, easy isolation with Mockito.
- **Cons:** Risk of over-mocking and false confidence if Spring behavior is approximated; mitigate by escalating to **IT** for Spring-dependent code.
- **Net Effect:** A balanced pyramid—many fast **unit tests** via **Surefire**, fewer but comprehensive **integration tests** via **Failsafe**.
