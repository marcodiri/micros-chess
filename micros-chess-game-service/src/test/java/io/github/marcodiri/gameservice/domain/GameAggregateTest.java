package io.github.marcodiri.gameservice.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.marcodiri.core.domain.event.DomainEvent;
import io.github.marcodiri.gameservice.api.event.GameCreated;
import io.github.marcodiri.gameservice.api.event.MovePlayed;
import io.github.marcodiri.gameservice.domain.command.CreateGameCommand;
import io.github.marcodiri.gameservice.domain.command.PlayMoveCommand;

@ExtendWith(MockitoExtension.class)
public class GameAggregateTest {

    @Spy
    private GameAggregate game;

    @Nested
    class processCreateGame {

        private UUID gameId, player1Id, player2Id;
        private CreateGameCommand cmd;

        @BeforeEach
        void createCommand() {
            gameId = UUID.randomUUID();
            player1Id = UUID.randomUUID();
            player2Id = UUID.randomUUID();
            cmd = new CreateGameCommand(player1Id, player2Id);
        }

        @Test
        void processReturnsEventsForCreation() {
            GameCreated expectedEvent = new GameCreated(gameId, player1Id, player2Id);
            when(game.generateId()).thenReturn(gameId);

            List<DomainEvent> events = game.process(cmd);

            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isEqualTo(expectedEvent);
        }

    }

    @Nested
    class processPlayMove {

        private UUID gameId, playerId;
        private PlayMoveCommand cmd;
        private String move = "e4";

        @BeforeEach
        void createCommand() {
            gameId = UUID.randomUUID();
            playerId = UUID.randomUUID();
            cmd = new PlayMoveCommand(playerId, move);
        }

        @Test
        void processReturnsEventsForPlayingMove() throws GameNotInProgressException, IllegalMoveException {
            MovePlayed expectedEvent = new MovePlayed(gameId, playerId, move);
            when(game.getId()).thenReturn(gameId);
            when(game.getState()).thenReturn(GameState.IN_PROGRESS);

            List<DomainEvent> events = game.process(cmd);

            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isEqualTo(expectedEvent);
        }

        @ParameterizedTest
        @NullSource
        @EnumSource(
        value = GameState.class,
        names = {"IN_PROGRESS"},
        mode = EnumSource.Mode.EXCLUDE)
        void processThrowsIfGameIsNotInCorrectState(GameState state)
        {
            when(game.getState()).thenReturn(state);

            assertThatThrownBy(() -> game.process(cmd))
                .isInstanceOf(GameNotInProgressException.class)
                .hasMessage("Move cannot be played because the game is in state %s", state);
        }

        @Test
        void processThrowsIfMoveIsIllegal()
        {
            when(game.getState()).thenReturn(GameState.IN_PROGRESS);
            when(game.moveIsLegal()).thenReturn(false);

            assertThatThrownBy(() -> game.process(cmd))
                .isInstanceOf(IllegalMoveException.class)
                .hasMessage("Move %s is illegal for the current position", move);
        }

    }

    @Nested
    class applyGameCreated {

        private UUID gameId, player1Id, player2Id;
        private GameCreated event;

        @BeforeEach
        void createCommand() {
            gameId = UUID.randomUUID();
            player1Id = UUID.randomUUID();
            player2Id = UUID.randomUUID();
            event = new GameCreated(gameId, player1Id, player2Id);
        }

        @Test
        void applyChangesAggregateState() {
            game.apply(event);

            assertThat(game.getId()).isEqualTo(gameId);
            assertThat(game.getPlayer1Id()).isEqualTo(player1Id);
            assertThat(game.getPlayer2Id()).isEqualTo(player2Id);
            assertThat(game.getMovesList()).isEmpty();
            assertThat(game.getState()).isEqualTo(GameState.IN_PROGRESS);
        }

    }

    @Nested
    class applyMovePlayed {

        private UUID gameId, player1Id, player2Id;
        private String move1 = "e4";
        private String move2 = "e5";
        private MovePlayed event1, event2;

        @BeforeEach
        void createCommand() {
            gameId = UUID.randomUUID();
            player1Id = UUID.randomUUID();
            player2Id = UUID.randomUUID();
            event1 = new MovePlayed(gameId, player1Id, move1);
            event2 = new MovePlayed(gameId, player2Id, move2);
        }

        @Test
        void applyMovePlayedAppendsMoveToMovesList() {
            when(game.getMovesList()).thenReturn(new ArrayList<>());

            game.apply(event1);
            game.apply(event2);

            assertThat(game.getMovesList()).hasSize(2);
            assertThat(game.getMovesList()).first().isEqualTo(ImmutablePair.of(player1Id, move1));
            assertThat(game.getMovesList()).last().isEqualTo(ImmutablePair.of(player2Id, move2));
        }

    }

}
