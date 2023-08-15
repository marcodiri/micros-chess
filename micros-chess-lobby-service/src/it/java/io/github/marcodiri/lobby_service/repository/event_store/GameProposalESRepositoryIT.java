package io.github.marcodiri.lobby_service.repository.event_store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
import io.github.marcodiri.lobbyservice.domain.GameProposal;
import io.github.marcodiri.lobbyservice.domain.GameProposalFactory;
import io.github.marcodiri.lobbyservice.domain.command.CreateGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.event.GameProposalCanceled;
import io.github.marcodiri.lobbyservice.domain.event.GameProposalCreated;
import io.github.marcodiri.lobbyservice.repository.eventstore.GameProposalESRepository;

@ExtendWith(MockitoExtension.class)
public class GameProposalESRepositoryIT {

    private static final String CONNECTION_STRING = "esdb://localhost:2113?tls=false";

    private static EventStoreDBClientSettings setts;
    private static EventStoreDBClient client;

    @Mock
    private GameProposalFactory gameProposalFactory;

    private GameProposalESRepository gameProposalESRepository;

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
        gameProposalESRepository = new GameProposalESRepository(client, gameProposalFactory);
    }

    @Nested
    class save {

        @Test
        void saveCreatesNewGameProposal() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException {
            GameProposal gameProposal = mock(GameProposal.class);
            when(gameProposalFactory.createGameProposal()).thenReturn(gameProposal);

            CreateGameProposalCommand cmd = new CreateGameProposalCommand(UUID.randomUUID());
            gameProposalESRepository.save(cmd);

            verify(gameProposalFactory).createGameProposal();
        }

        @Test
        void saveCallsProcessAndApplyForASingleEvent() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException {
            GameProposal gameProposal = mock(GameProposal.class);
            List<DomainEvent> event = Collections.singletonList(new GameProposalCreated(UUID.randomUUID()));

            when(gameProposal.process(isA(CreateGameProposalCommand.class))).thenReturn(event);
            when(gameProposalFactory.createGameProposal()).thenReturn(gameProposal);

            CreateGameProposalCommand cmd = new CreateGameProposalCommand(UUID.randomUUID());
            gameProposalESRepository.save(cmd);

            InOrder inOrder = inOrder(gameProposal);

            inOrder.verify(gameProposal).process(cmd);
            inOrder.verify(gameProposal).apply((GameProposalCreated) event.get(0));
        }

        @Test
        void saveCallsProcessAndApplyForMultipleEvents() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException {
            GameProposal gameProposal = mock(GameProposal.class);
            List<DomainEvent> events = Arrays.asList(new GameProposalCreated(UUID.randomUUID()),
                    new GameProposalCanceled(UUID.randomUUID()));

            when(gameProposal.process(isA(CreateGameProposalCommand.class))).thenReturn(events);
            when(gameProposalFactory.createGameProposal()).thenReturn(gameProposal);

            CreateGameProposalCommand cmd = new CreateGameProposalCommand(UUID.randomUUID());
            gameProposalESRepository.save(cmd);

            InOrder inOrder = inOrder(gameProposal);

            inOrder.verify(gameProposal).process(cmd);
            inOrder.verify(gameProposal).apply((GameProposalCreated) events.get(0));
            inOrder.verify(gameProposal).apply((GameProposalCanceled) events.get(1));
        }

        @Test
        void savePersistsEventsInEventStore() throws InterruptedException, ExecutionException, IllegalAccessException,
                IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException,
                StreamReadException, DatabindException, IOException {
            GameProposal gameProposal = mock(GameProposal.class);
            UUID gameProposalId = UUID.randomUUID();
            when(gameProposal.getId()).thenReturn(gameProposalId);
            List<DomainEvent> events = Arrays.asList(new GameProposalCreated(gameProposalId),
                    new GameProposalCanceled(gameProposalId));

            when(gameProposal.process(isA(CreateGameProposalCommand.class))).thenReturn(events);
            when(gameProposalFactory.createGameProposal()).thenReturn(gameProposal);

            CreateGameProposalCommand cmd = new CreateGameProposalCommand(UUID.randomUUID());
            gameProposalESRepository.save(cmd);

            String streamName = String.format("GameProposal_%s", gameProposalId);

            ReadStreamOptions readLastEvent = ReadStreamOptions.get()
                    .backwards()
                    .fromEnd();

            ReadResult result = client.readStream(streamName, readLastEvent)
                    .get();

            List<ResolvedEvent> resolvedEvents = result.getEvents();

            GameProposalCreated firstWrittenEvent = new ObjectMapper().readValue(
                    resolvedEvents.get(1).getOriginalEvent().getEventData(),
                    GameProposalCreated.class);
            GameProposalCanceled secondWrittenEvent = new ObjectMapper().readValue(
                    resolvedEvents.get(0).getOriginalEvent().getEventData(),
                    GameProposalCanceled.class);

            assertThat(firstWrittenEvent).isEqualTo(events.get(0));
            assertThat(secondWrittenEvent).isEqualTo(events.get(1));
        }

        @Test
        void saveReturnsGameProposal() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException {
            GameProposal gameProposal = mock(GameProposal.class);
            when(gameProposalFactory.createGameProposal()).thenReturn(gameProposal);

            CreateGameProposalCommand cmd = new CreateGameProposalCommand(UUID.randomUUID());
            GameProposal returnedGameProposal = gameProposalESRepository.save(cmd);
            assertThat(returnedGameProposal).isInstanceOf(GameProposal.class);
        }

    }

}
