package io.github.marcodiri.lobbyservice.repository.eventstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import io.github.marcodiri.lobbyservice.api.event.GameProposalCanceled;
import io.github.marcodiri.lobbyservice.api.event.GameProposalCreated;
import io.github.marcodiri.lobbyservice.domain.GameProposalAggregate;
import io.github.marcodiri.lobbyservice.domain.GameProposalFactory;
import io.github.marcodiri.lobbyservice.domain.UnsupportedStateTransitionException;
import io.github.marcodiri.lobbyservice.domain.command.AcceptGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.command.CancelGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.command.CreateGameProposalCommand;

@ExtendWith(MockitoExtension.class)
public class GameProposalESRepositoryIT {

    private static final String CONNECTION_STRING = "esdb://localhost:2113?tls=false";

    private static EventStoreDBClientSettings setts;
    private static EventStoreDBClient client;

    @Mock
    GameProposalAggregate gameProposal;

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

    @Test
    void writeEventsForAggregateThrowsIfAggregateIdIsNull() {
        when(gameProposal.getId()).thenReturn(null);

        assertThatThrownBy(() -> {
            gameProposalESRepository.writeEventsForAggregate(gameProposal, Collections.emptyList());
        }).isInstanceOf(RuntimeException.class)
        .hasMessage("GameProposal id is null");
    }

    @Nested
    class save {

        private CreateGameProposalCommand cmd;
        private UUID gameProposalId;

        @BeforeEach
        void setup() {
            gameProposalId = UUID.randomUUID();
            when(gameProposal.getId()).thenReturn(gameProposalId);
            when(gameProposalFactory.createAggregate()).thenReturn(gameProposal);
            cmd = new CreateGameProposalCommand(UUID.randomUUID());
        }

        @Test
        void saveCreatesNewGameProposal() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException {

            gameProposalESRepository.save(cmd);

            verify(gameProposalFactory).createAggregate();
        }

        @Test
        void saveCallsProcessAndApplyForASingleEvent() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException {
            List<DomainEvent> event = Collections
                    .singletonList(new GameProposalCreated(UUID.randomUUID(), UUID.randomUUID()));
            when(gameProposal.process(isA(CreateGameProposalCommand.class))).thenReturn(event);

            gameProposalESRepository.save(cmd);

            InOrder inOrder = inOrder(gameProposal);
            inOrder.verify(gameProposal).process(cmd);
            inOrder.verify(gameProposal).apply((GameProposalCreated) event.get(0));
        }

        @Test
        void saveCallsProcessAndApplyForMultipleEvents() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException {
            List<DomainEvent> events = Arrays.asList(new GameProposalCreated(UUID.randomUUID(), UUID.randomUUID()),
                    new GameProposalCanceled(UUID.randomUUID()));
            when(gameProposal.process(isA(CreateGameProposalCommand.class))).thenReturn(events);

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
            List<DomainEvent> events = Arrays.asList(new GameProposalCreated(gameProposalId, UUID.randomUUID()),
                    new GameProposalCanceled(gameProposalId));
            when(gameProposal.process(isA(CreateGameProposalCommand.class))).thenReturn(events);

            gameProposalESRepository.save(cmd);

            String streamName = String.format("GameProposal_%s", gameProposalId);
            List<ResolvedEvent> resolvedEvents = readAllEventsFromStream(streamName);

            GameProposalCreated firstWrittenEvent = new ObjectMapper().readValue(
                    resolvedEvents.get(0).getOriginalEvent().getEventData(),
                    GameProposalCreated.class);
            GameProposalCanceled secondWrittenEvent = new ObjectMapper().readValue(
                    resolvedEvents.get(1).getOriginalEvent().getEventData(),
                    GameProposalCanceled.class);

            assertThat(firstWrittenEvent).isEqualTo(events.get(0));
            assertThat(secondWrittenEvent).isEqualTo(events.get(1));
        }

        @Test
        void saveReturnsGameProposal() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException {
            GameProposalAggregate returnedGameProposal = gameProposalESRepository.save(cmd);

            assertThat(returnedGameProposal).isInstanceOf(GameProposalAggregate.class);
        }

    }

    @Test
    void readEventsForGameProposalRetrievesAllPastEventsForAggregate()
            throws InterruptedException, ExecutionException, StreamReadException, DatabindException, IOException {
        UUID gameProposalId = UUID.randomUUID();
        List<DomainEvent> events = Arrays.asList(new GameProposalCreated(gameProposalId, UUID.randomUUID()),
                new GameProposalCanceled(gameProposalId));
        String streamName = String.format("GameProposal_%s", gameProposalId);
        insertTestEventsInEventStore(streamName, events);

        List<DomainEvent> pastEvents = gameProposalESRepository.readEventsForAggregate(gameProposalId);

        assertThat(pastEvents).containsExactlyElementsOf(events);
    }

    @Nested
    class updateWithCancelGameProposalCommand {

        private UUID gameProposalId;
        private String streamName;
        private CancelGameProposalCommand cmd;

        @BeforeEach
        void setup() {
            gameProposalId = UUID.randomUUID();
            streamName = String.format("GameProposal_%s", gameProposalId);
            when(gameProposal.getId()).thenReturn(gameProposalId);
            when(gameProposalFactory.createAggregate()).thenReturn(gameProposal);
            cmd = new CancelGameProposalCommand(UUID.randomUUID());
        }

        @Test
        void updateCreatesNewGameProposalAndAppliesPastEvents() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException,
                InterruptedException,
                ExecutionException, StreamReadException, DatabindException, IOException,
                UnsupportedStateTransitionException {
            List<DomainEvent> events = Arrays.asList(new GameProposalCreated(gameProposalId, UUID.randomUUID()),
                    new GameProposalCanceled(gameProposalId));
            insertTestEventsInEventStore(streamName, events);

            gameProposalESRepository.update(gameProposalId, cmd);

            verify(gameProposalFactory).createAggregate();
            InOrder inOrder = inOrder(gameProposal);
            inOrder.verify(gameProposal).apply((GameProposalCreated) events.get(0));
            inOrder.verify(gameProposal).apply((GameProposalCanceled) events.get(1));
        }

        @Test
        void updateCallsProcessAndApplyForMultipleNewEvents()
                throws StreamReadException, DatabindException, IllegalAccessException, InvocationTargetException,
                NoSuchMethodException, InterruptedException, ExecutionException, IOException,
                UnsupportedStateTransitionException {
            List<DomainEvent> pastEvents = Arrays.asList(new GameProposalCreated(gameProposalId, UUID.randomUUID()),
                    new GameProposalCanceled(gameProposalId));
            List<DomainEvent> newEvents = Arrays.asList(new GameProposalCreated(gameProposalId, UUID.randomUUID()),
                    new GameProposalCreated(gameProposalId, UUID.randomUUID()));
            when(gameProposal.process(isA(CancelGameProposalCommand.class))).thenReturn(newEvents);
            insertTestEventsInEventStore(streamName, pastEvents);

            gameProposalESRepository.update(gameProposalId, cmd);

            InOrder inOrder = inOrder(gameProposal);
            inOrder.verify(gameProposal).process(cmd);
            inOrder.verify(gameProposal).apply((GameProposalCreated) newEvents.get(0));
            inOrder.verify(gameProposal).apply((GameProposalCreated) newEvents.get(1));
        }

        @Test
        void updatePersistsNewEventsInEventStore()
                throws StreamReadException, DatabindException, IllegalAccessException, InvocationTargetException,
                NoSuchMethodException, InterruptedException, ExecutionException, IOException,
                UnsupportedStateTransitionException {
            List<DomainEvent> pastEvents = Arrays.asList(new GameProposalCreated(gameProposalId, UUID.randomUUID()),
                    new GameProposalCanceled(gameProposalId));
            List<DomainEvent> newEvents = Arrays.asList(new GameProposalCreated(gameProposalId, UUID.randomUUID()),
                    new GameProposalCreated(gameProposalId, UUID.randomUUID()));
            when(gameProposal.process(isA(CancelGameProposalCommand.class))).thenReturn(newEvents);
            insertTestEventsInEventStore(streamName, pastEvents);

            gameProposalESRepository.update(gameProposalId, cmd);

            List<ResolvedEvent> resolvedEvents = readAllEventsFromStream(streamName);

            GameProposalCreated firstWrittenEvent = new ObjectMapper().readValue(
                    resolvedEvents.get(2).getOriginalEvent().getEventData(),
                    GameProposalCreated.class);
            GameProposalCreated secondWrittenEvent = new ObjectMapper().readValue(
                    resolvedEvents.get(3).getOriginalEvent().getEventData(),
                    GameProposalCreated.class);

            assertThat(firstWrittenEvent).isEqualTo(newEvents.get(0));
            assertThat(secondWrittenEvent).isEqualTo(newEvents.get(1));
        }

        @Test
        void updateReturnsGameProposal() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException, StreamReadException, DatabindException, IOException,
                UnsupportedStateTransitionException {
            List<DomainEvent> event = Collections
                    .singletonList(new GameProposalCreated(gameProposalId, UUID.randomUUID()));
            insertTestEventsInEventStore(streamName, event);

            GameProposalAggregate returnedGameProposal = gameProposalESRepository.update(gameProposalId, cmd);
            assertThat(returnedGameProposal).isInstanceOf(GameProposalAggregate.class);
        }

    }

    @Nested
    class updateWithAcceptGameProposalCommand {

        private UUID gameProposalId;
        private String streamName;
        private AcceptGameProposalCommand cmd;

        @BeforeEach
        void setup() {
            gameProposalId = UUID.randomUUID();
            streamName = String.format("GameProposal_%s", gameProposalId);
            when(gameProposal.getId()).thenReturn(gameProposalId);
            when(gameProposalFactory.createAggregate()).thenReturn(gameProposal);
            cmd = new AcceptGameProposalCommand(UUID.randomUUID());
        }

        @Test
        void updateCreatesNewGameProposalAndAppliesPastEvents() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException,
                InterruptedException,
                ExecutionException, StreamReadException, DatabindException, IOException,
                UnsupportedStateTransitionException {
            List<DomainEvent> events = Arrays.asList(new GameProposalCreated(gameProposalId, UUID.randomUUID()),
                    new GameProposalCanceled(gameProposalId));
            insertTestEventsInEventStore(streamName, events);

            gameProposalESRepository.update(gameProposalId, cmd);

            verify(gameProposalFactory).createAggregate();
            InOrder inOrder = inOrder(gameProposal);
            inOrder.verify(gameProposal).apply((GameProposalCreated) events.get(0));
            inOrder.verify(gameProposal).apply((GameProposalCanceled) events.get(1));
        }

        @Test
        void updateCallsProcessAndApplyForMultipleNewEvents()
                throws StreamReadException, DatabindException, IllegalAccessException, InvocationTargetException,
                NoSuchMethodException, InterruptedException, ExecutionException, IOException,
                UnsupportedStateTransitionException {
            List<DomainEvent> pastEvents = Arrays.asList(new GameProposalCreated(gameProposalId, UUID.randomUUID()),
                    new GameProposalCanceled(gameProposalId));
            List<DomainEvent> newEvents = Arrays.asList(new GameProposalCreated(gameProposalId, UUID.randomUUID()),
                    new GameProposalCreated(gameProposalId, UUID.randomUUID()));
            when(gameProposal.process(isA(AcceptGameProposalCommand.class))).thenReturn(newEvents);
            insertTestEventsInEventStore(streamName, pastEvents);

            gameProposalESRepository.update(gameProposalId, cmd);

            InOrder inOrder = inOrder(gameProposal);
            inOrder.verify(gameProposal).process(cmd);
            inOrder.verify(gameProposal).apply((GameProposalCreated) newEvents.get(0));
            inOrder.verify(gameProposal).apply((GameProposalCreated) newEvents.get(1));
        }

        @Test
        void updatePersistsNewEventsInEventStore()
                throws StreamReadException, DatabindException, IllegalAccessException, InvocationTargetException,
                NoSuchMethodException, InterruptedException, ExecutionException, IOException,
                UnsupportedStateTransitionException {
            List<DomainEvent> pastEvents = Arrays.asList(new GameProposalCreated(gameProposalId, UUID.randomUUID()),
                    new GameProposalCanceled(gameProposalId));
            List<DomainEvent> newEvents = Arrays.asList(new GameProposalCreated(gameProposalId, UUID.randomUUID()),
                    new GameProposalCreated(gameProposalId, UUID.randomUUID()));
            when(gameProposal.process(isA(AcceptGameProposalCommand.class))).thenReturn(newEvents);
            insertTestEventsInEventStore(streamName, pastEvents);

            gameProposalESRepository.update(gameProposalId, cmd);

            List<ResolvedEvent> resolvedEvents = readAllEventsFromStream(streamName);

            GameProposalCreated firstWrittenEvent = new ObjectMapper().readValue(
                    resolvedEvents.get(2).getOriginalEvent().getEventData(),
                    GameProposalCreated.class);
            GameProposalCreated secondWrittenEvent = new ObjectMapper().readValue(
                    resolvedEvents.get(3).getOriginalEvent().getEventData(),
                    GameProposalCreated.class);

            assertThat(firstWrittenEvent).isEqualTo(newEvents.get(0));
            assertThat(secondWrittenEvent).isEqualTo(newEvents.get(1));
        }

        @Test
        void updateReturnsGameProposal() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException, StreamReadException, DatabindException, IOException,
                UnsupportedStateTransitionException {
            List<DomainEvent> event = Collections
                    .singletonList(new GameProposalCreated(gameProposalId, UUID.randomUUID()));
            insertTestEventsInEventStore(streamName, event);

            GameProposalAggregate returnedGameProposal = gameProposalESRepository.update(gameProposalId, cmd);
            assertThat(returnedGameProposal).isInstanceOf(GameProposalAggregate.class);
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
