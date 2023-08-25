package io.github.marcodiri.gameservice.repository.eventstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eventstore.dbclient.EventData;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

import io.github.marcodiri.core.domain.Aggregate;
import io.github.marcodiri.core.domain.event.DomainEvent;
import io.github.marcodiri.gameservice.api.event.GameCreated;
import io.github.marcodiri.gameservice.api.event.MovePlayed;
import io.github.marcodiri.gameservice.domain.GameAggregate;
import io.github.marcodiri.gameservice.domain.GameFactory;
import io.github.marcodiri.gameservice.domain.GameNotInProgressException;
import io.github.marcodiri.gameservice.domain.IllegalMoveException;
import io.github.marcodiri.gameservice.domain.command.CreateGameCommand;
import io.github.marcodiri.gameservice.domain.command.PlayMoveCommand;

@ExtendWith(MockitoExtension.class)
public class GameESRepositoryTest {

    @Mock
    GameAggregate game;

    @Mock
    private GameFactory gameFactory;

    private class TestGameESRepository extends GameESRepository {

        public TestGameESRepository() {
            super(null, gameFactory);
        }

        @Override
        public List<DomainEvent> readEventsForAggregate(UUID gameId)
                throws InterruptedException, ExecutionException, IOException {
            return null;
        }

        @Override
        protected List<EventData> applyEventsToAggregate(Aggregate aggregate, List<DomainEvent> events)
                throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
            return null;
        }

        @Override
        protected void applyAndWriteEvents(Aggregate aggregate, List<DomainEvent> events) {
        }

    }

    @Spy
    private TestGameESRepository gameESRepository;

    @Nested
    class save {

        private CreateGameCommand cmd;

        @BeforeEach
        void setup() {
            when(gameFactory.createAggregate()).thenReturn(game);
            cmd = new CreateGameCommand(UUID.randomUUID(), UUID.randomUUID());
        }

        @Test
        void saveCreatesNewGame() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException {
            gameESRepository.save(cmd);

            verify(gameFactory).createAggregate();
        }

        @Test
        void saveCallsProcessAndApplyAndWriteEvents() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException {
            List<DomainEvent> events = Arrays.asList(
                    new GameCreated(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()),
                    new MovePlayed(UUID.randomUUID(), UUID.randomUUID(), ""));
            when(game.process(isA(CreateGameCommand.class))).thenReturn(events);

            gameESRepository.save(cmd);

            verify(game).process(cmd);
            verify(gameESRepository).applyAndWriteEvents(game, events);
        }

        @Test
        void saveReturnsGame() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException {
            GameAggregate returnedGame = gameESRepository.save(cmd);

            assertThat(returnedGame).isInstanceOf(GameAggregate.class);
        }

    }

    @Nested
    class updateWithPlayMoveCommand {

        private UUID gameId;
        private PlayMoveCommand cmd;

        @BeforeEach
        void setup() {
            gameId = UUID.randomUUID();
            when(gameFactory.createAggregate()).thenReturn(game);
            cmd = new PlayMoveCommand(UUID.randomUUID(), "e4");
        }

        @Test
        void updateCreatesNewGameAndAppliesPastEvents() throws InterruptedException, ExecutionException, IOException,
                IllegalAccessException, InvocationTargetException, NoSuchMethodException, GameNotInProgressException,
                IllegalMoveException {
            List<DomainEvent> events = Arrays.asList(
                    new GameCreated(gameId, UUID.randomUUID(), UUID.randomUUID()),
                    new MovePlayed(gameId, UUID.randomUUID(), "e4"));
            doReturn(events).when(gameESRepository).readEventsForAggregate(gameId);

            gameESRepository.update(gameId, cmd);

            verify(gameFactory).createAggregate();
            InOrder inOrder = inOrder(gameESRepository);
            inOrder.verify(gameESRepository).readEventsForAggregate(gameId);
            inOrder.verify(gameESRepository).applyEventsToAggregate(game, events);
        }

        @Test
        void updateCallsProcessAndApplyAndWriteEvents() throws GameNotInProgressException, IllegalMoveException,
                StreamReadException, DatabindException, IllegalAccessException, InvocationTargetException,
                NoSuchMethodException, InterruptedException, ExecutionException, IOException {
            List<DomainEvent> newEvents = Arrays.asList(
                    new GameCreated(gameId, UUID.randomUUID(), UUID.randomUUID()),
                    new MovePlayed(gameId, UUID.randomUUID(), "e4"));
            when(game.process(isA(PlayMoveCommand.class))).thenReturn(newEvents);

            gameESRepository.update(gameId, cmd);

            verify(game).process(cmd);
            verify(gameESRepository).applyAndWriteEvents(game, newEvents);
        }

        @Test
        void updateReturnsGame() throws StreamReadException, DatabindException, IllegalAccessException,
                InvocationTargetException, NoSuchMethodException, InterruptedException, ExecutionException, IOException,
                GameNotInProgressException, IllegalMoveException {
            GameAggregate returnedGame = gameESRepository.update(gameId, cmd);
            assertThat(returnedGame).isInstanceOf(GameAggregate.class);
        }

    }

}
