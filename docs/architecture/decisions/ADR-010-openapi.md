# ADR-010: Use OpenAPI Swagger Annotations for All REST API Endpoints

Date: 2025-10-24

## Status

Accepted

## Context

To ensure that the REST API is well-documented and easily understandable for developers and external
consumers, it is essential to provide clear and standardized documentation. OpenAPI (Swagger)
annotations allow for the automatic generation of API documentation, which can be visualized using
tools like Swagger UI. Currently, not all endpoints are annotated, and OpenAPI is not fully enabled
in the Spring Boot application.

## Decision

All REST API endpoints in the project will be annotated with OpenAPI Swagger annotations.
Additionally, OpenAPI will be enabled in the Spring Boot application to generate and serve the API
documentation.

## Consequences

- **Positive**:
    - Provides a standardized and comprehensive API documentation.
    - Simplifies integration for external consumers by offering a clear contract.
    - Reduces manual effort in maintaining API documentation.

- **Negative**:
    - Requires additional effort to annotate all existing endpoints.
    - Developers need to be familiar with OpenAPI annotations.

## Implementation

1. Add the `springdoc-openapi` dependency to the `pom.xml`:
   ```xml
   <dependency>
       <groupId>org.springdoc</groupId>
       <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
       <version>2.1.0</version>
   </dependency>
   ```

2. Annotate all REST API endpoints with OpenAPI annotations:
   ```java
   @RestController
   @RequestMapping("/example")
   @Tag(name = "Example", description = "Example API")
   public class ExampleController {

       @Operation(summary = "Get example data", description = "Fetches example data by ID")
       @ApiResponses(value = {
           @ApiResponse(responseCode = "200", description = "Successful operation"),
           @ApiResponse(responseCode = "404", description = "Example not found")
       })
       @GetMapping("/{id}")
       public ResponseEntity<ExampleDto> getExample(@PathVariable Long id) {
           // Implementation here
           return ResponseEntity.ok(new ExampleDto());
       }
   }
   ```

3. Enable OpenAPI in the Spring Boot application:
    - No additional configuration is required as `springdoc-openapi` automatically enables Swagger
      UI at `/swagger-ui.html`.

4. Document this decision in the project's development guidelines to ensure all new endpoints are
   annotated.

5. Verify the generated documentation by accessing the Swagger UI at
   `http://localhost:8080/swagger-ui.html`.
