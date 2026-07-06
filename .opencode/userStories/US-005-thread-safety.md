# US-005: Thread-Safe Concurrent Operations

**Actor:** Developer  
**Priority:** Medium

## Story

As a **Developer**  
I want to **perform match operations from multiple threads concurrently**  
So that **the scoreboard works correctly under simultaneous updates from multiple live matches**

## Acceptance Criteria

- Starting matches concurrently from multiple threads must not produce duplicate IDs or corrupt repository state.
- Concurrent `updateScore` calls on the same match must not lose or corrupt data (last-write-wins is acceptable; no interleaved corruption).
- Concurrent `startMatch` checks for team uniqueness must not allow the same team to be used in two matches due to a race condition.
- A match being finished while a concurrent score update is in progress must result in a clean state (either the update is applied before finish or the update gets a consistent rejection).
- `getSummary()` read operations must not block or be blocked by write operations unnecessarily.

## Dependencies & Prerequisites

- US-001, US-002, US-003 must be implemented.
- `InMemoryMatchRepository` uses thread-safe data structures (`ConcurrentHashMap`).
- `ScoreboardServiceImpl` uses adequate synchronisation around multi-step validation+write operations.
