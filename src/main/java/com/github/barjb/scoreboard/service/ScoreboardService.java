package com.github.barjb.scoreboard.service;

import com.github.barjb.scoreboard.dto.MatchSummary;
import com.github.barjb.scoreboard.dto.TeamSummary;
import com.github.barjb.scoreboard.model.Match;
import com.github.barjb.scoreboard.model.MatchId;
import com.github.barjb.scoreboard.model.Score;
import com.github.barjb.scoreboard.model.Team;

import java.time.Instant;
import java.util.List;

public interface ScoreboardService {

    Match startMatch(Team home, Team away);

    Match updateScore(MatchId id, Score newScore);

    Match finishMatch(MatchId id);

    List<MatchSummary> getSummary();

    TeamSummary getTeamSummary(Team team, Instant from, Instant to);
}
