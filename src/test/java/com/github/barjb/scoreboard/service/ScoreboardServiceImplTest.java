package com.github.barjb.scoreboard.service;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import com.github.barjb.scoreboard.dto.MatchSummary;
import com.github.barjb.scoreboard.exception.MatchNotFoundException;
import com.github.barjb.scoreboard.model.Match;
import com.github.barjb.scoreboard.model.MatchId;
import com.github.barjb.scoreboard.model.MatchStatus;
import com.github.barjb.scoreboard.model.Score;
import com.github.barjb.scoreboard.model.Team;
import com.github.barjb.scoreboard.repository.InMemoryMatchRepository;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
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

    @Test
    void shouldFinishMatch() {
        // given
        Match match = scoreboardService.startMatch(new Team("Mexico"), new Team("Canada"));

        // when
        Match finished = scoreboardService.finishMatch(match.id());

        // then
        assertThat(finished.id()).isEqualTo(match.id());
        assertThat(finished.status()).isEqualTo(MatchStatus.FINISHED);
        assertThat(finished.finishedAt()).isCloseTo(Instant.now(), within(1, SECONDS));
        assertThat(finished.score()).isEqualTo(new Score(0, 0));

        // persisted
        Match persisted = matchRepository.findById(match.id()).orElseThrow();
        assertThat(persisted.status()).isEqualTo(MatchStatus.FINISHED);
        assertThat(persisted.finishedAt()).isCloseTo(Instant.now(), within(1, SECONDS));
    }

    @Test
    void shouldThrowMatchNotFoundExceptionWhenFinishingUnknownMatch() {
        // given
        MatchId unknownId = MatchId.random();

        // when / then
        assertThatThrownBy(() -> scoreboardService.finishMatch(unknownId))
                .isInstanceOf(MatchNotFoundException.class)
                .hasMessageContaining(unknownId.value().toString());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenFinishingAlreadyFinishedMatch() {
        // given
        Match match = scoreboardService.startMatch(new Team("Mexico"), new Team("Canada"));
        scoreboardService.finishMatch(match.id());

        // when / then
        assertThatThrownBy(() -> scoreboardService.finishMatch(match.id()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FINISHED");
    }

    @Test
    void shouldThrowWhenFinishMatchIdIsNull() {
        assertThatThrownBy(() -> scoreboardService.finishMatch(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldReturnSummaryOrderedByTotalScoreDescending() {
        // given
        Match matchA = scoreboardService.startMatch(new Team("Mexico"), new Team("Canada"));
        Match matchB = scoreboardService.startMatch(new Team("Spain"), new Team("Brazil"));
        Match matchC = scoreboardService.startMatch(new Team("Germany"), new Team("France"));

        scoreboardService.updateScore(matchA.id(), new Score(3, 2));  // total 5
        scoreboardService.updateScore(matchB.id(), new Score(0, 4));  // total 4
        scoreboardService.updateScore(matchC.id(), new Score(2, 1));  // total 3

        // when
        var summary = scoreboardService.getSummary();

        // then
        assertThat(summary).hasSize(3);
        assertThat(summary.get(0).id()).isEqualTo(matchA.id());  // 5 → first
        assertThat(summary.get(1).id()).isEqualTo(matchB.id());  // 4 → second
        assertThat(summary.get(2).id()).isEqualTo(matchC.id());  // 3 → third
    }

    @Test
    void shouldOrderByMostRecentlyStartedWhenScoresAreEqual() {
        // given — two matches with same total score but different startedAt
        Match older = new Match(
                MatchId.random(),
                new Team("Older"), new Team("Team"),
                new Score(2, 2),          // total 4
                MatchStatus.IN_PROGRESS,
                Instant.now().minusSeconds(10),
                null
        );
        Match newer = new Match(
                MatchId.random(),
                new Team("Newer"), new Team("Team"),
                new Score(3, 1),          // total 4
                MatchStatus.IN_PROGRESS,
                Instant.now(),
                null
        );
        matchRepository.save(older);
        matchRepository.save(newer);

        // when
        var summary = scoreboardService.getSummary();

        // then — newer (most recent) should come first
        assertThat(summary).hasSize(2);
        assertThat(summary.get(0).id()).isEqualTo(newer.id());
        assertThat(summary.get(1).id()).isEqualTo(older.id());
    }

    @Test
    void shouldExcludeFinishedMatchesFromSummary() {
        // given
        Match match = scoreboardService.startMatch(new Team("Mexico"), new Team("Canada"));
        scoreboardService.finishMatch(match.id());

        // when
        var summary = scoreboardService.getSummary();

        // then
        assertThat(summary).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenNoMatchesInProgress() {
        // when
        var summary = scoreboardService.getSummary();

        // then
        assertThat(summary).isEmpty();
    }

    @Test
    void shouldReturnSnapshotNotAffectedByLaterMutations() {
        // given
        Match match = scoreboardService.startMatch(new Team("Mexico"), new Team("Canada"));
        scoreboardService.updateScore(match.id(), new Score(1, 0));

        // when — capture first summary
        var firstSnapshot = scoreboardService.getSummary();

        // mutate the match after the snapshot
        scoreboardService.updateScore(match.id(), new Score(5, 3));

        // then — first snapshot still has old score
        assertThat(firstSnapshot).hasSize(1);
        assertThat(firstSnapshot.get(0).score()).isEqualTo(new Score(1, 0));

        // second summary has the new score
        var secondSnapshot = scoreboardService.getSummary();
        assertThat(secondSnapshot.get(0).score()).isEqualTo(new Score(5, 3));
    }

    @Test
    void shouldNotAllowDuplicateTeamUnderConcurrentStart() throws InterruptedException {
        // given
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        Team home = new Team("Mexico");
        Team away = new Team("Canada");

        // when — all threads fire startMatch simultaneously
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    scoreboardService.startMatch(home, away);
                    successCount.incrementAndGet();
                } catch (IllegalStateException e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        // then — only one match started, the rest got IllegalStateException
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(errorCount.get()).isEqualTo(threadCount - 1);
        assertThat(matchRepository.findAllInProgress()).hasSize(1);
    }

    @Test
    void shouldHandleConcurrentUpdateScoreOnSameMatch() throws InterruptedException {
        // given
        Match match = scoreboardService.startMatch(new Team("Mexico"), new Team("Canada"));
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // when — all threads update the same match concurrently
        for (int i = 0; i < threadCount; i++) {
            final int score = i + 1;
            executor.submit(() -> {
                try {
                    scoreboardService.updateScore(match.id(), new Score(score, score));
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        // then — final score is one of the updates (last-write-wins), no corruption
        Match persisted = matchRepository.findById(match.id()).orElseThrow();
        assertThat(persisted.status()).isEqualTo(MatchStatus.IN_PROGRESS);
        // score should be one of the valid updates (1-10 range)
        assertThat(persisted.score().homeScore()).isBetween(1, 10);
        assertThat(persisted.score().awayScore()).isBetween(1, 10);
        assertThat(persisted.score().homeScore()).isEqualTo(persisted.score().awayScore());
    }

    @Test
    void shouldHandleConcurrentFinishAndUpdateScore() throws InterruptedException {
        // given
        Match match = scoreboardService.startMatch(new Team("Mexico"), new Team("Canada"));
        CountDownLatch latch = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // when — one thread finishes, one updates, simultaneously
        executor.submit(() -> {
            try {
                scoreboardService.finishMatch(match.id());
            } finally {
                latch.countDown();
            }
        });
        executor.submit(() -> {
            try {
                scoreboardService.updateScore(match.id(), new Score(3, 1));
            } catch (IllegalStateException e) {
                // expected if finish happened first — race is acceptable
            } finally {
                latch.countDown();
            }
        });
        latch.await();
        executor.shutdown();

        // then — the end state is always consistent, never corrupted
        Match persisted = matchRepository.findById(match.id()).orElseThrow();
        // Two possible valid outcomes:
        // 1) FINISHED with original score (finish won)
        // 2) IN_PROGRESS with new score 3-1 (update won after finish was submitted but before it executed)
        assertThat(persisted.status()).isIn(MatchStatus.IN_PROGRESS, MatchStatus.FINISHED);
        if (persisted.status() == MatchStatus.FINISHED) {
            assertThat(persisted.finishedAt()).isNotNull();
            assertThat(persisted.score()).isEqualTo(new Score(0, 0));
        } else {
            assertThat(persisted.score()).isEqualTo(new Score(3, 1));
        }
    }
}
