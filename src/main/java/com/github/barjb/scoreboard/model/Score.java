package com.github.barjb.scoreboard.model;

public record Score(int homeScore, int awayScore) {

    public Score {
        if (homeScore < 0 || awayScore < 0) {
            throw new IllegalArgumentException("Scores must be non-negative");
        }
    }

    public int total() {
        return homeScore + awayScore;
    }
}
