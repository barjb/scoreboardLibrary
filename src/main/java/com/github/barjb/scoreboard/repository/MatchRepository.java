package com.github.barjb.scoreboard.repository;

import com.github.barjb.scoreboard.model.Match;
import com.github.barjb.scoreboard.model.MatchId;

import java.util.List;
import java.util.Optional;

public interface MatchRepository {

    Match save(Match match);

    Optional<Match> findById(MatchId id);

    List<Match> findAllInProgress();

    void deleteById(MatchId id);
}
