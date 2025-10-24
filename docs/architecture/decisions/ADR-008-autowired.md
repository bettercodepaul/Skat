# ADR-008: Use `@SpringBootTest` Instead of `@MockMvc`

Date: 2025-10-23

## Status

Accepted

## Context

In the current project, integration tests are written using both `@SpringBootTest` and `@MockMvc`.
While `@MockMvc` is useful for testing specific layers (e.g., controllers) in isolation, it does not
load the full application context. This can lead to inconsistencies when testing features that rely
on the complete Spring Boot configuration. A unified approach is needed to ensure reliable and
consistent integration testing.

## Decision

The project will use `@SpringBootTest` for all integration tests. This ensures that the full
application context is loaded, providing a more comprehensive testing environment. While `@MockMvc`
will no longer be used for integration tests, alternatives such as `TestRestTemplate` or
`WebTestClient` can be considered for specific scenarios.

## Alternatives

1. **`@MockMvc`**:
    - **Pros**:
        - Faster as it does not load the full application context.
        - Useful for testing controllers in isolation.
    - **Cons**:
        - Limited to the web layer.
        - Does not test the full application behavior.

2. **`TestRestTemplate`**:
    - **Pros**:
        - Provides a way to test REST endpoints with the full application context.
    - **Cons**:
        - Slower than `@MockMvc`.

3. **`WebTestClient`**:
    - **Pros**:
        - Non-blocking and supports reactive applications.
    - **Cons**:
        - Requires additional setup for non-reactive applications.

## Consequences

- **Positive**:
    - Ensures consistent testing by loading the full application context.
    - Tests the application as a whole, including configuration and dependencies.
    - Reduces the need for mocking in integration tests.

- **Negative**:
    - Slower test execution due to the full context initialization.
    - May require additional resources for running tests.

## Implementation

1. Replace `@MockMvc` with `@SpringBootTest` in all integration tests:
   ```java
   @SpringBootTest
   public class ExampleIntegrationTest {

       @Autowired
       private TestRestTemplate restTemplate;

       @Test
       public void testEndpoint() {
           ResponseEntity<String> response = restTemplate.getForEntity("/example", String.class);
           assertEquals(HttpStatus.OK, response.getStatusCode());
       }
   }
