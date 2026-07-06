# US-003: Finish an Active Match

**Actor:** Developer  
**Priority:** High

## Story

As a **Developer**  
I want to **finish an active in-progress match**  
So that **the match is marked as completed and no longer appears in the active summary**

## Acceptance Criteria

- Calling `finishMatch(matchId)` changes the match's status from `IN_PROGRESS` to `FINISHED`.
- `finishedAt` timestamp is set to approximately the current time.
- Calling `finishMatch` with an unknown `MatchId` throws `MatchNotFoundException`.
- Calling `finishMatch` on an already `FINISHED` match throws `IllegalStateException`.
- A finished match is excluded from `getSummary()` results.
- The finished match remains retrievable (for future archive/history features).

## Dependencies & Prerequisites

- US-001 (Start a Match) must be implemented.
- `MatchStatus` enum includes `FINISHED`.
- `Match` model includes a nullable `finishedAt` field.
