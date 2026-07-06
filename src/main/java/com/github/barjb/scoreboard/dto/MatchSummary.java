package com.github.barjb.scoreboard.dto;

import com.github.barjb.scoreboard.model.Match;
import com.github.barjb.scoreboard.model.MatchId;
import com.github.barjb.scoreboard.model.Score;
import com.github.barjb.scoreboard.model.Team;
import java.time.Instant;

public record MatchSummary(MatchId id, Team home, Team away, Score score, Instant startedAt) {

    public static MatchSummary from(Match match) {
        return new MatchSummary(match.id(), match.home(), match.away(), match.score(), match.startedAt());
    }
}
