package com.github.barjb.scoreboard.exception;

import com.github.barjb.scoreboard.model.MatchId;

public class MatchNotFoundException extends RuntimeException {

    public MatchNotFoundException(MatchId id) {
        super("Match not found: " + id.value());
    }
}
