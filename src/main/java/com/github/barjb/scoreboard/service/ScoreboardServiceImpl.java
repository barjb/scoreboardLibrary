package com.github.barjb.scoreboard.service;

import com.github.barjb.scoreboard.dto.MatchSummary;
import com.github.barjb.scoreboard.model.Match;
import com.github.barjb.scoreboard.model.MatchId;
import com.github.barjb.scoreboard.model.MatchStatus;
import com.github.barjb.scoreboard.model.Score;
import com.github.barjb.scoreboard.model.Team;
import com.github.barjb.scoreboard.exception.MatchNotFoundException;
import com.github.barjb.scoreboard.repository.MatchRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class ScoreboardServiceImpl implements ScoreboardService {

    private final MatchRepository matchRepository;
    private final Object mutex = new Object();

    public ScoreboardServiceImpl(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Override
    public Match startMatch(Team home, Team away) {
        Objects.requireNonNull(home, "Home team must not be null");
        Objects.requireNonNull(away, "Away team must not be null");

        synchronized (mutex) {
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
    }

    @Override
    public Match updateScore(MatchId id, Score newScore) {
        Objects.requireNonNull(id, "MatchId must not be null");
        Objects.requireNonNull(newScore, "Score must not be null");

        synchronized (mutex) {
            Match match = matchRepository.findById(id)
                    .orElseThrow(() -> new MatchNotFoundException(id));

            if (match.status() != MatchStatus.IN_PROGRESS) {
                throw new IllegalStateException(
                        "Cannot update score: match " + id.value() + " is " + match.status());
            }

            Match updated = match.withScore(newScore);
            return matchRepository.save(updated);
        }
    }

    @Override
    public Match finishMatch(MatchId id) {
        Objects.requireNonNull(id, "MatchId must not be null");

        synchronized (mutex) {
            Match match = matchRepository.findById(id)
                    .orElseThrow(() -> new MatchNotFoundException(id));

            if (match.status() == MatchStatus.FINISHED) {
                throw new IllegalStateException(
                        "Cannot finish: match " + id.value() + " is already FINISHED");
            }

            Match finished = match.withStatus(MatchStatus.FINISHED);
            return matchRepository.save(finished);
        }
    }

    @Override
    public List<MatchSummary> getSummary() {
        return matchRepository.findAllInProgress().stream()
                .map(MatchSummary::from)
                .sorted(Comparator
                        .comparingInt((MatchSummary s) -> s.score().total()).reversed()
                        .thenComparing(Comparator.comparing(MatchSummary::startedAt).reversed()))
                .toList();
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
