# US-006: Swappable Storage Backend

**Actor:** Library Maintainer  
**Priority:** Low

## Story

As a **Library Maintainer**  
I want to **swap the storage implementation without changing the public API**  
So that **the library can be adapted to different infrastructure needs without breaking consumers**

## Acceptance Criteria

- `MatchRepository` is defined as an interface in the `repository` package, with all storage operations expressed as methods.
- The public `ScoreboardService` API depends only on the `MatchRepository` interface, not on any concrete implementation.
- A new storage backend (e.g., JDBC, Redis) can be added by implementing `MatchRepository` without modifying any existing source files.
- The `Service` can be instantiated with any implementation of `MatchRepository` via constructor injection.
- The existing `InMemoryMatchRepository` continues to work as the default when no external dependency is provided.

## Dependencies & Prerequisites

- US-001, US-002, US-003, US-004 must be implemented with `MatchRepository` as an interface.
- The `ScoreboardService` must accept `MatchRepository` via constructor.
