package com.github.barjb.scoreboard.repository;

import com.github.barjb.scoreboard.model.Match;
import com.github.barjb.scoreboard.model.MatchId;
import com.github.barjb.scoreboard.model.MatchStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
}
