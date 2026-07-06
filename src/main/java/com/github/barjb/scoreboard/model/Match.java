package com.github.barjb.scoreboard.model;

import java.time.Instant;
import java.util.Objects;

public record Match(
        MatchId id,
        Team home,
        Team away,
        Score score,
        MatchStatus status,
        Instant startedAt,
        Instant finishedAt) {

    public Match {
        Objects.requireNonNull(id);
        Objects.requireNonNull(home);
        Objects.requireNonNull(away);
        Objects.requireNonNull(score);
        Objects.requireNonNull(status);
        Objects.requireNonNull(startedAt);
        // finishedAt is allowed to be null
    }

    public Match withScore(Score newScore) {
        Objects.requireNonNull(newScore);
        return new Match(id, home, away, newScore, status, startedAt, finishedAt);
    }

    public Match withStatus(MatchStatus newStatus) {
        Objects.requireNonNull(newStatus);
        Instant newFinishedAt = finishedAt;
        if (newStatus == MatchStatus.FINISHED) {
            newFinishedAt = Instant.now();
        } else if (newStatus == MatchStatus.IN_PROGRESS) {
            newFinishedAt = null;
        }
        return new Match(id, home, away, score, newStatus, startedAt, newFinishedAt);
    }
}
