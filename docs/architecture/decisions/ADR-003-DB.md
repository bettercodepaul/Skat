# ADR-002: Persistence Layer

Date: 2025-10-23

## Status

Accepted

## Context

The application requires a robust persistence layer to store and manage data related to players,
games, and scores. A well-defined database schema is essential for ensuring data integrity,
performance, and scalability.

## Decision

Hibernate with Repository Pattern will be used for the persistence layer. 
