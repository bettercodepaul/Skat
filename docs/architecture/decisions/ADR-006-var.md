# ADR-007: Use `var` Instead of Full Class Names

Date: 2025-10-23

## Status

Accepted

## Context

In Java, local variable type inference (`var`) was introduced in Java 10 to reduce verbosity and
improve code readability. Currently, the project uses explicit type declarations for local
variables, which can make the code more verbose and harder to read. Adopting `var` can simplify the
code while maintaining clarity.

## Decision

The project will use `var` for local variable declarations where the type is obvious from the
context. This will reduce verbosity and improve code readability.

## Consequences

- **Positive**:
    - Reduces boilerplate code.
    - Improves readability by focusing on variable names and logic.
    - Simplifies refactoring, as the type is inferred automatically.

- **Negative**:
    - May reduce clarity in cases where the type is not immediately obvious.
    - Requires developers to be familiar with type inference.

## Implementation

1. Use `var` for local variables where the type is clear:
   ```java
   var list = new ArrayList<String>();
   var count = 10;
