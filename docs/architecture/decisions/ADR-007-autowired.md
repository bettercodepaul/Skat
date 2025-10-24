# ADR-007: Use `@Autowired` on Private Members Instead of Constructors

Date: 2025-10-23

## Status

Accepted

## Context

In the current project, dependency injection is primarily performed through constructors. While this approach is explicit and aligns with best practices, it can lead to verbose constructors when there are many dependencies. Using `@Autowired` on private members can simplify the code and improve readability. Additionally, integration tests require a consistent approach to mocking dependencies, and SpringMocks provides a convenient way to handle this.

## Decision

The project will use `@Autowired` on private members for dependency injection instead of constructors. Integration tests will use SpringMocks to mock dependencies and ensure consistent testing behavior.

## Consequences

- **Positive**:
    - Reduces verbosity in constructors.
    - Simplifies class structure by removing explicit constructor injection.
    - Improves readability and focuses on the business logic.
    - SpringMocks simplifies mocking in integration tests.

- **Negative**:
    - May reduce clarity as dependencies are not explicitly listed in constructors.
    - Private member injection is less conventional and may require explanation for new developers.

## Implementation

1. Use `@Autowired` on private members for dependency injection:
   ```java
   @Service
   public class ExampleService {

       @Autowired
       private ExampleRepository exampleRepository;

       public void performAction() {
           exampleRepository.save(new ExampleEntity());
       }
   }

    @SpringBootTest
    public class ExampleServiceTest {
    
        @Autowired
        private ExampleService exampleService;
    
        @MockBean
        private ExampleRepository exampleRepository;
    
        @Test
        public void testPerformAction() {
            exampleService.performAction();
            verify(exampleRepository).save(any(ExampleEntity.class));
        }
   }
```
