package io.github.marcodiri.gameservice.repository.eventstore;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.EventStoreDBClientSettings;
import com.eventstore.dbclient.EventStoreDBConnectionString;
import com.eventstore.dbclient.ReadResult;
import com.eventstore.dbclient.ReadStreamOptions;
import com.eventstore.dbclient.ResolvedEvent;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.marcodiri.core.domain.event.DomainEvent;
import io.github.marcodiri.gameservice.api.event.GameCreated;
import io.github.marcodiri.gameservice.api.event.MovePlayed;
import io.github.marcodiri.gameservice.domain.GameAggregate;
import io.github.marcodiri.gameservice.domain.GameFactory;
import io.github.marcodiri.gameservice.domain.command.CreateGameCommand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class GameESRepositoryIT {

    private static final String CONNECTION_STRING = "esdb://localhost:2113?tls=false";

    private static EventStoreDBClientSettings setts;
    private static EventStoreDBClient client;

    @Mock
    GameAggregate game;

    @Mock
    private GameFactory gameFactory;

    private GameESRepository gameESRepository;

    @BeforeAll
    static void setupESConnection() {
        setts = EventStoreDBConnectionString.parseOrThrow(CONNECTION_STRING);
        client = EventStoreDBClient.create(setts);
    }

    @AfterAll
    static void teardownESConnection() throws ExecutionException, InterruptedException {
        client.shutdown();
    }

    @BeforeEach
    void setup() {
        gameESRepository = new GameESRepository(client, gameFactory);
    }

    @Test
    void writeEventsForAggregateThrowsIfAggregateIdIsNull() {
        when(game.getId()).thenReturn(null);

        assertThatThrownBy(() -> {
            gameESRepository.writeEventsForAggregate(game, Collections.emptyList());
        }).isInstanceOf(RuntimeException.class)
        .hasMessage("Game id is null");
    }

    @Nested
    class save {

        private CreateGameCommand cmd;
        private UUID gameId;

        @BeforeEach
        void setup() {
            gameId = UUID.randomUUID();
            when(game.getId()).thenReturn(gameId);
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
        void saveCallsProcessAndApplyForASingleEvent() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException {
            List<DomainEvent> event = Collections
                    .singletonList(new GameCreated(gameId, UUID.randomUUID(), UUID.randomUUID()));
            when(game.process(isA(CreateGameCommand.class))).thenReturn(event);

            gameESRepository.save(cmd);

            InOrder inOrder = inOrder(game);
            inOrder.verify(game).process(cmd);
            inOrder.verify(game).apply((GameCreated) event.get(0));
        }

        @Test
        void saveCallsProcessAndApplyForMultipleEvents() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException {
            List<DomainEvent> events = Arrays.asList(
                    new GameCreated(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()),
                    new MovePlayed(gameId, UUID.randomUUID(), ""));
            when(game.process(isA(CreateGameCommand.class))).thenReturn(events);

            gameESRepository.save(cmd);

            InOrder inOrder = inOrder(game);
            inOrder.verify(game).process(cmd);
            inOrder.verify(game).apply((GameCreated) events.get(0));
            inOrder.verify(game).apply((MovePlayed) events.get(1));
        }

        @Test
        void savePersistsEventsInEventStore() throws InterruptedException, ExecutionException, IllegalAccessException,
                IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException,
                StreamReadException, DatabindException, IOException {
            List<DomainEvent> events = Arrays.asList(
                    new GameCreated(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()),
                    new MovePlayed(gameId, UUID.randomUUID(), ""));
            when(game.process(isA(CreateGameCommand.class))).thenReturn(events);

            gameESRepository.save(cmd);

            String streamName = String.format("Game_%s", gameId);
            List<ResolvedEvent> resolvedEvents = readAllEventsFromStream(streamName);

            GameCreated firstWrittenEvent = new ObjectMapper().readValue(
                    resolvedEvents.get(0).getOriginalEvent().getEventData(),
                    GameCreated.class);
            MovePlayed secondWrittenEvent = new ObjectMapper().readValue(
                    resolvedEvents.get(1).getOriginalEvent().getEventData(),
                    MovePlayed.class);

            assertThat(firstWrittenEvent).isEqualTo(events.get(0));
            assertThat(secondWrittenEvent).isEqualTo(events.get(1));
        }

        @Test
        void saveReturnsGame() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException {
            GameAggregate returnedGame = gameESRepository.save(cmd);

            assertThat(returnedGame).isInstanceOf(GameAggregate.class);
        }

    }

    private List<ResolvedEvent> readAllEventsFromStream(String streamName)
            throws InterruptedException, ExecutionException {
        ReadStreamOptions readLastEvent = ReadStreamOptions.get()
                .forwards()
                .fromStart();

        ReadResult result = client.readStream(streamName, readLastEvent)
                .get();

        List<ResolvedEvent> resolvedEvents = result.getEvents();
        return resolvedEvents;
    }

    private void insertTestEventsInEventStore(String streamName, List<DomainEvent> events)
            throws InterruptedException, ExecutionException {
        List<EventData> eventDataList = new ArrayList<>();
        events.forEach(event -> {
            EventData eventData = EventData
                    .builderAsJson(event.getType().toString(), event)
                    .build();
            eventDataList.add(eventData);
        });

        client.appendToStream(streamName, eventDataList.iterator()).get();
    }

}
