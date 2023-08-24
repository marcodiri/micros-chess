package io.github.marcodiri.gameservice.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.marcodiri.core.domain.event.DomainEvent;
import io.github.marcodiri.gameservice.api.event.GameCreated;
import io.github.marcodiri.gameservice.domain.command.CreateGameCommand;

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
}
