# US-004: Get Summary of In-Progress Matches

**Actor:** Developer  
**Priority:** High

## Story

As a **Developer**  
I want to **get a summary of all in-progress matches ordered by total score**  
So that **I can display a live leaderboard to end users with the most exciting matches first**

## Acceptance Criteria

- Calling `getSummary()` returns a `List<MatchSummary>`, where `MatchSummary` is a read-only projection containing: `matchId`, `homeTeam`, `awayTeam`, `score`, `startedAt`.
- Only matches with status `IN_PROGRESS` are included in the summary.
- Results are sorted by **total score (home + away) descending**.
- Matches with equal total score are sorted by **most recently started first** (`startedAt` descending).
- An empty list is returned when no matches are in progress.
- The returned list is a snapshot (immutable or defensive copy) — subsequent mutations to the underlying matches do not affect the already-returned list.

## Dependencies & Prerequisites

- US-001 (Start a Match) must be implemented.
- `MatchSummary` DTO class exists.
- `MatchRepository` with `findAllInProgress()` returns stable data.
