# ADR-004: Use Lombok in TO

Date: 2025-10-23

## Status

Accepted

## Context

The TO (Technical Operations) project involves a significant amount of boilerplate code, such as
getters, setters, constructors, and logging. Writing and maintaining this repetitive code increases
development time and the likelihood of errors. A solution is needed to reduce boilerplate while
maintaining code readability and consistency.

## Decision

The project will use Lombok to reduce boilerplate code. Lombok annotations will be applied to
generate commonly used methods like getters, setters, constructors, and `toString()` automatically.
This will improve developer productivity and maintain cleaner code.

## Consequences

- **Positive**:
    - Reduced boilerplate code.
    - Improved readability and maintainability.
    - Faster development time.

- **Negative**:
    - Adds a dependency to the project.
    - Potential learning curve for developers unfamiliar with Lombok.
    - IDE support may require additional configuration.

## Implementation

1. Add Lombok as a dependency in the `pom.xml`:
   ```xml
   <dependency>
       <groupId>org.projectlombok</groupId>
       <artifactId>lombok</artifactId>
       <version>1.18.30</version>
       <scope>provided</scope>
   </dependency>
   

2. Use Lombok for all TO, VO and entities:
   - Use `@Data` for simple data carriers.
   - Use `@Builder` for complex object construction.
   - Use `@Slf4j` for logging.
