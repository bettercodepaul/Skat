
# ADR-011: Use Flyway for All DDL Operations

Date: 2025-10-24

## Status

Accepted

## Context

Database schema changes (DDL operations) are currently managed manually or through ad-hoc scripts.
This approach can lead to inconsistencies, difficulties in tracking changes, and challenges in
maintaining a reliable migration history. Flyway provides a structured and version-controlled way to
manage database migrations, ensuring consistency and traceability.

## Decision

All DDL operations will be managed using Flyway scripts. Each table will have its own dedicated
Flyway script to ensure modularity and clarity. This approach will standardize database migrations
and simplify collaboration among developers.

## Consequences

- **Positive**:
    - Ensures a consistent and version-controlled approach to database schema changes.
    - Simplifies tracking and auditing of database changes.
    - Reduces the risk of conflicts and errors during migrations.
    - Modular scripts improve clarity and maintainability.

- **Negative**:
    - Requires developers to learn and adopt Flyway conventions.
    - Initial setup effort to migrate existing schema changes into Flyway scripts.

## Implementation

1. Add Flyway as a dependency in the `pom.xml`:
   ```xml
   <dependency>
       <groupId>org.flywaydb</groupId>
       <artifactId>flyway-core</artifactId>
       <version>9.0.0</version>
   </dependency>
   ```

2. Configure Flyway in the `application.properties` file:
   ```properties
   spring.flyway.enabled=true
   spring.flyway.locations=classpath:db/migration
   spring.flyway.baseline-on-migrate=true
   ```

3. Create a new Flyway script for each table:
    - Scripts should follow the naming convention `V<version>__<description>.sql`.
    - Example for a `users` table:
      ```sql
      -- File: V1__Create_users_table.sql
      CREATE TABLE users (
          id BIGINT PRIMARY KEY,
          username VARCHAR(255) NOT NULL,
          password VARCHAR(255) NOT NULL,
          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
      
      ```
      
the table and the columns in the table should be described using SQL comments.

Use varchar without limitation instead of varchar(xxx)

4. Store all Flyway scripts in the `src/main/resources/db/migration` directory.

5. Document this decision in the project's development guidelines to ensure all new DDL operations
   are added as Flyway scripts.
````
