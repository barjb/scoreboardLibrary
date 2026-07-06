package com.github.barjb.scoreboard.service;

import com.github.barjb.scoreboard.model.Match;
import com.github.barjb.scoreboard.model.MatchId;
import com.github.barjb.scoreboard.model.Score;
import com.github.barjb.scoreboard.model.Team;

public interface ScoreboardService {

    Match startMatch(Team home, Team away);

    Match updateScore(MatchId id, Score newScore);
}
