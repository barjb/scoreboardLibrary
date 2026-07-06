package com.github.barjb.scoreboard.dto;

import com.github.barjb.scoreboard.model.Team;

import java.time.Instant;

public record TeamSummary(Team team, int matchplayed, int goalsScored, int goalsConceeded, Instant start, Instant end) {

}