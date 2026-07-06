package com.github.barjb.scoreboard.model;

import java.util.UUID;

public record MatchId(UUID value) {

    public static MatchId random() {
        return new MatchId(UUID.randomUUID());
    }
}
