# US-002: Update the Score of a Match

**Actor:** Developer  
**Priority:** High

## Story

As a **Developer**  
I want to **update the score of an existing in-progress match**  
So that **the scoreboard reflects live game events**

## Acceptance Criteria

- Calling `updateScore(matchId, newScore)` updates the match's score to the provided `Score`.
- The provided `MatchId` must correspond to an existing match with status `IN_PROGRESS`.
- Calling `updateScore` with an unknown `MatchId` throws `MatchNotFoundException`.
- Calling `updateScore` on a `FINISHED` match throws `IllegalStateException`.
- Scores can be set to any non-negative integer values (including decreases for corrections).
- Updated scores are persisted and visible via subsequent retrieval or summary.

## Dependencies & Prerequisites

- US-001 (Start a Match) must be implemented — need a match to exist before updating.
- `Score` value object with home and away int fields, validates non-negative.
- `MatchRepository` with `findById()` and `save()`.
