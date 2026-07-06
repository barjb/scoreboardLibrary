package com.github.barjb.scoreboard.repository;

import com.github.barjb.scoreboard.model.Match;
import com.github.barjb.scoreboard.model.MatchId;
import com.github.barjb.scoreboard.model.MatchStatus;
import com.github.barjb.scoreboard.model.Team;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@Repository
public class InMemoryMatchRepository implements MatchRepository {

    private final ConcurrentHashMap<MatchId, Match> store = new ConcurrentHashMap<>();

    @Override
    public Match save(Match match) {
        store.put(match.id(), match);
        return match;
    }

    @Override
    public Optional<Match> findById(MatchId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Match> findAllInProgress() {
        return store.values().stream()
                .filter(m -> m.status() == MatchStatus.IN_PROGRESS)
                .toList();
    }

    @Override
    public void deleteById(MatchId id) {
        store.remove(id);
    }

    @Override
    public List<Match> findAllTeamMatches(Team team, Instant from, Instant to) {
        Objects.requireNonNull(team, "Home team must not be null");
        if (to != null && to.isBefore(from)) {
            throw new IllegalArgumentException("To can not be before from");
        }
        Predicate<Match> isPlayedByTeam = match -> match.home().equals(team) || match.away().equals(team);
        Predicate<Match> isStartedWithinInterval = match -> match.startedAt().isAfter(from);
        Predicate<Match> isEndedWithinInterval = match -> match.finishedAt() != null && (to == null || match.finishedAt().isBefore(to));

        return store.values().stream()
                .filter(isPlayedByTeam)
                .filter(isStartedWithinInterval)
                .filter(isEndedWithinInterval)
                .toList();
    }
}
