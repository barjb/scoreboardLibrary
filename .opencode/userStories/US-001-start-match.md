# US-001: Start a New Match

**Actor:** Developer  
**Priority:** High

## Story

As a **Developer**  
I want to **start a new match by providing two teams**  
So that **a new game is created on the scoreboard with an initial score of 0–0 and tracked as in-progress**

## Acceptance Criteria

- Calling `startMatch(Team home, Team away)` returns a `Match` object with a unique `MatchId`.
- The returned `Match` has status `IN_PROGRESS` and score `(0, 0)`.
- `startedAt` timestamp is set to approximately the current time.
- The match is persisted in the repository and retrievable.
- Multiple matches can be started simultaneously (different team pairs).
- Calling `startMatch` with the same team as either home or away in an already in-progress match throws an `IllegalStateException` (or similar).

## Dependencies & Prerequisites

- `MatchId`, `Team`, `Score`, `Match`, `MatchStatus` model classes exist.
- `MatchRepository` interface with `save()` and `findAllInProgress()` methods.
- `InMemoryMatchRepository` implementation is available.
