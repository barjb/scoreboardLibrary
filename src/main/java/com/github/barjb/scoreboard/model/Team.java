package com.github.barjb.scoreboard.model;

public record Team(String name) {

    public Team {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Team name must not be null or blank");
        }
    }
}
