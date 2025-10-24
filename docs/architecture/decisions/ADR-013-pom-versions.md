# ADR-013: Define All Versions in `pom.xml` as Properties

Date: 2025-10-24

## Status

Accepted

## Context

In the current project, dependency versions are scattered throughout the `pom.xml` file. This makes
it difficult to manage and update versions consistently. By defining all versions as properties in
the `pom.xml`, we can centralize version management, improve maintainability, and reduce the risk of
version conflicts.

## Decision

All dependency and plugin versions will be defined as properties in the `pom.xml` file. These
properties will be declared in a dedicated `<properties>` section at the top of the file.

## Consequences

- **Positive**:
    - Centralized version management simplifies updates and ensures consistency.
    - Reduces duplication of version numbers across the `pom.xml` file.
    - Improves readability and maintainability of the `pom.xml` file.

- **Negative**:
    - Requires developers to reference properties instead of hardcoding versions.
    - Initial effort to refactor the existing `pom.xml` file.

## Implementation

1. Define all versions in the `<properties>` section of the `pom.xml`:
   ```xml
   <properties>
       <spring.version>2.5.6</spring.version>
       <flyway.version>9.22.0</flyway.version>
       <java.version>17</java.version>
   </properties>
   ```

2. Reference these properties in the dependencies and plugins:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter</artifactId>
       <version>${spring.version}</version>
   </dependency>

   <plugin>
       <groupId>org.flywaydb</groupId>
       <artifactId>flyway-maven-plugin</artifactId>
       <version>${flyway.version}</version>
   </plugin>
   ```

3. Document this decision in the project's development guidelines to ensure all new versions are
   added as properties.
