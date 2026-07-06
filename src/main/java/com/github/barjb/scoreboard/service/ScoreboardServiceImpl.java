package com.github.barjb.scoreboard.service;

import com.github.barjb.scoreboard.model.Match;
import com.github.barjb.scoreboard.model.MatchId;
import com.github.barjb.scoreboard.model.MatchStatus;
import com.github.barjb.scoreboard.model.Score;
import com.github.barjb.scoreboard.model.Team;
import com.github.barjb.scoreboard.repository.MatchRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;

@Service
public class ScoreboardServiceImpl implements ScoreboardService {

    private final MatchRepository matchRepository;

    public ScoreboardServiceImpl(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Override
    public Match startMatch(Team home, Team away) {
        Objects.requireNonNull(home, "Home team must not be null");
        Objects.requireNonNull(away, "Away team must not be null");

        assertTeamsNotAlreadyPlaying(home, away);

        Match match = new Match(
                MatchId.random(),
                home,
                away,
                new Score(0, 0),
                MatchStatus.IN_PROGRESS,
                Instant.now(),
                null
        );

        return matchRepository.save(match);
    }

    private void assertTeamsNotAlreadyPlaying(Team home, Team away) {
        for (Match inProgress : matchRepository.findAllInProgress()) {
            if (inProgress.home().equals(home) || inProgress.away().equals(home)) {
                throw new IllegalStateException(
                        "Team '" + home.name() + "' is already playing in match " + inProgress.id().value());
            }
            if (inProgress.home().equals(away) || inProgress.away().equals(away)) {
                throw new IllegalStateException(
                        "Team '" + away.name() + "' is already playing in match " + inProgress.id().value());
            }
        }
    }
}
