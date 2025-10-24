# ADR-005: Repositories Should Prefer Returning Entities

Date: 2025-10-23

## Status

Accepted

## Context

In the current project, repositories are responsible for data access and retrieval. There is
inconsistency in the return types of repository methods, with some returning entities and others
returning DTOs or projections. This inconsistency can lead to confusion, increased complexity, and
duplication of logic across layers. A unified approach is needed to ensure clarity and
maintainability.

## Decision

Repository methods should prefer returning entities instead of DTOs or projections. This ensures
that the domain model remains the central representation of data and avoids duplicating business
logic in multiple layers.

## Consequences

- **Positive**:
    - Simplifies repository implementation.
    - Reduces duplication of logic across layers.
    - Ensures consistency in data handling.
    - Promotes the use of domain-driven design principles.

- **Negative**:
    - May require additional mapping to convert entities to DTOs in the service layer.
    - Potential over-fetching of data if entities contain unnecessary relationships.

## Implementation

1. Update repository methods to return entities:
    - Use `@Entity`-annotated classes as return types for repository methods.
    - Avoid returning DTOs or projections directly from repositories.

2. Perform any necessary mapping from entities to DTOs in the service layer.

3. Document this decision in the project's coding standards to ensure consistency across the team.
