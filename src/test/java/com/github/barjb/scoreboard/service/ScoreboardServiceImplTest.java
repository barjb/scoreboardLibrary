package com.github.barjb.scoreboard.service;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import com.github.barjb.scoreboard.exception.MatchNotFoundException;
import com.github.barjb.scoreboard.model.Match;
import com.github.barjb.scoreboard.model.MatchId;
import com.github.barjb.scoreboard.model.MatchStatus;
import com.github.barjb.scoreboard.model.Score;
import com.github.barjb.scoreboard.model.Team;
import com.github.barjb.scoreboard.repository.InMemoryMatchRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScoreboardServiceImplTest {

    private ScoreboardServiceImpl scoreboardService;
    private InMemoryMatchRepository matchRepository;

    @BeforeEach
    void setUp() {
        matchRepository = new InMemoryMatchRepository();
        scoreboardService = new ScoreboardServiceImpl(matchRepository);
    }

    @Test
    void shouldStartMatchWithInitialScore() {
        // given
        Team home = new Team("Mexico");
        Team away = new Team("Canada");

        // when
        Match result = scoreboardService.startMatch(home, away);

        // then
        assertThat(result.id()).isNotNull();
        assertThat(result.home()).isEqualTo(home);
        assertThat(result.away()).isEqualTo(away);
        assertThat(result.score()).isEqualTo(new Score(0, 0));
        assertThat(result.status()).isEqualTo(MatchStatus.IN_PROGRESS);
        assertThat(result.startedAt()).isCloseTo(Instant.now(), within(1, SECONDS));
        assertThat(result.finishedAt()).isNull();

        // persisted
        Match persisted = matchRepository.findById(result.id()).orElseThrow();
        assertThat(persisted).isEqualTo(result);
    }

    @Test
    void shouldGenerateUniqueMatchIdEachTime() {
        // when
        Match match1 = scoreboardService.startMatch(new Team("A"), new Team("B"));
        Match match2 = scoreboardService.startMatch(new Team("C"), new Team("D"));

        // then
        assertThat(match1.id()).isNotEqualTo(match2.id());
    }

    @Test
    void shouldThrowWhenHomeTeamAlreadyInProgress() {
        // given
        scoreboardService.startMatch(new Team("Mexico"), new Team("Brazil"));

        // when / then
        assertThatThrownBy(() ->
                scoreboardService.startMatch(new Team("Mexico"), new Team("Canada")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Mexico");
    }

    @Test
    void shouldThrowWhenAwayTeamAlreadyInProgress() {
        // given
        scoreboardService.startMatch(new Team("Brazil"), new Team("Mexico"));

        // when / then
        assertThatThrownBy(() ->
                scoreboardService.startMatch(new Team("Canada"), new Team("Mexico")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Mexico");
    }

    @Test
    void shouldThrowWhenHomeTeamIsAwayInAnotherMatch() {
        // given
        scoreboardService.startMatch(new Team("Spain"), new Team("Mexico"));

        // when / then
        assertThatThrownBy(() ->
                scoreboardService.startMatch(new Team("Mexico"), new Team("Canada")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Mexico");
    }

    @Test
    void shouldThrowWhenAwayTeamIsHomeInAnotherMatch() {
        // given
        scoreboardService.startMatch(new Team("Mexico"), new Team("Spain"));

        // when / then
        assertThatThrownBy(() ->
                scoreboardService.startMatch(new Team("Canada"), new Team("Mexico")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Mexico");
    }

    @Test
    void shouldAllowMatchAfterPreviousTeamFinishes() {
        // given
        Match first = scoreboardService.startMatch(new Team("Mexico"), new Team("Canada"));

        // when — finish the match so teams become free
        Match finished = first.withStatus(MatchStatus.FINISHED);
        matchRepository.save(finished);

        // then — same teams can play again
        Match second = scoreboardService.startMatch(new Team("Mexico"), new Team("Canada"));
        assertThat(second.status()).isEqualTo(MatchStatus.IN_PROGRESS);
    }

    @Test
    void shouldThrowWhenHomeTeamIsNull() {
        assertThatThrownBy(() -> scoreboardService.startMatch(null, new Team("Away")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowWhenAwayTeamIsNull() {
        assertThatThrownBy(() -> scoreboardService.startMatch(new Team("Home"), null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldStartMultipleMatchesWithDifferentTeams() {
        // when
        Match match1 = scoreboardService.startMatch(new Team("Mexico"), new Team("Canada"));
        Match match2 = scoreboardService.startMatch(new Team("Spain"), new Team("Brazil"));
        Match match3 = scoreboardService.startMatch(new Team("Germany"), new Team("France"));

        // then
        assertThat(match1.status()).isEqualTo(MatchStatus.IN_PROGRESS);
        assertThat(match2.status()).isEqualTo(MatchStatus.IN_PROGRESS);
        assertThat(match3.status()).isEqualTo(MatchStatus.IN_PROGRESS);

        assertThat(matchRepository.findAllInProgress()).hasSize(3);
    }

    @Test
    void shouldUpdateScore() {
        // given
        Match match = scoreboardService.startMatch(new Team("Mexico"), new Team("Canada"));

        // when
        Match updated = scoreboardService.updateScore(match.id(), new Score(2, 1));

        // then
        assertThat(updated.id()).isEqualTo(match.id());
        assertThat(updated.score()).isEqualTo(new Score(2, 1));
        assertThat(updated.status()).isEqualTo(MatchStatus.IN_PROGRESS);

        // persisted
        Match persisted = matchRepository.findById(match.id()).orElseThrow();
        assertThat(persisted.score()).isEqualTo(new Score(2, 1));
    }

    @Test
    void shouldThrowMatchNotFoundExceptionWhenUpdatingUnknownMatch() {
        // given
        MatchId unknownId = MatchId.random();

        // when / then
        assertThatThrownBy(() -> scoreboardService.updateScore(unknownId, new Score(1, 0)))
                .isInstanceOf(MatchNotFoundException.class)
                .hasMessageContaining(unknownId.value().toString());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenUpdatingFinishedMatch() {
        // given
        Match match = scoreboardService.startMatch(new Team("Mexico"), new Team("Canada"));
        scoreboardService.updateScore(match.id(), new Score(3, 1));
        Match finished = match.withStatus(MatchStatus.FINISHED);
        matchRepository.save(finished);

        // when / then
        assertThatThrownBy(() -> scoreboardService.updateScore(finished.id(), new Score(4, 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FINISHED");
    }

    @Test
    void shouldAllowScoreDecrease() {
        // given
        Match match = scoreboardService.startMatch(new Team("Mexico"), new Team("Canada"));
        scoreboardService.updateScore(match.id(), new Score(5, 3));

        // when — decrease the score
        Match updated = scoreboardService.updateScore(match.id(), new Score(2, 1));

        // then
        assertThat(updated.score()).isEqualTo(new Score(2, 1));
    }

    @Test
    void shouldThrowWhenUpdateScoreMatchIdIsNull() {
        assertThatThrownBy(() -> scoreboardService.updateScore(null, new Score(1, 0)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowWhenUpdateScoreScoreIsNull() {
        // given
        Match match = scoreboardService.startMatch(new Team("Mexico"), new Team("Canada"));

        assertThatThrownBy(() -> scoreboardService.updateScore(match.id(), null))
                .isInstanceOf(NullPointerException.class);
    }
}
