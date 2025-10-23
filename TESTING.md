# Testing Strategy

We test using **Given-When-Then** and **AssertJ**.  
**@SpringBootTest** is preferred over **@WebMvcTest**.  
**Testcontainers** provides reproducible integration environments.

## Example
```java
@SpringBootTest
@Testcontainers
class ExampleIT {
  @Container
  static PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:16-alpine");

  @Test
  void example() {
    assertThat(db.isRunning()).isTrue();
  }
}
```
