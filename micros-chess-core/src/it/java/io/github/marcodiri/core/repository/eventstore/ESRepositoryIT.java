package io.github.marcodiri.core.repository.eventstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
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

import io.github.marcodiri.core.domain.Aggregate;
import io.github.marcodiri.core.domain.AggregateFactory;
import io.github.marcodiri.core.domain.event.DomainEvent;
import io.github.marcodiri.core.domain.event.DomainEventType;

@ExtendWith(MockitoExtension.class)
public class ESRepositoryIT {

    private static final String CONNECTION_STRING = "esdb://localhost:2113?tls=false";

    private static EventStoreDBClientSettings setts;
    private static EventStoreDBClient client;

    private static enum TestEventType implements DomainEventType {
        TEST
    }

    @SuppressWarnings("unused")
    private static class TestEvent implements DomainEvent {

        private UUID id;
        private TestEventType type;

        public TestEvent() {
        }

        public TestEvent(UUID id, TestEventType type) {
            this.id = id;
            this.type = type;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        @Override
        public DomainEventType getType() {
            return type;
        }

        public void setType(TestEventType type) {
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            TestEvent event = (TestEvent) o;
            return Objects.equals(id, event.id)
                    && Objects.equals(type, event.type);
        }

    }

    private static class TestESRepository extends ESRepository {

        public TestESRepository(EventStoreDBClient client, AggregateFactory aggregateFactory) {
            super(client, aggregateFactory);
        }

        @Override
        protected String streamNameFromAggregateId(UUID aggregateId) {
            return String.format("Test_%s", aggregateId);
        }

        @Override
        protected List<DomainEvent> convertEvents(List<ResolvedEvent> resolvedEvents)
                throws StreamReadException, DatabindException, IOException {
            return null;
        }

    }

    private static class TestAggregate extends Aggregate {

        public void apply(TestEvent event) {
        }

    }

    @Mock
    TestAggregate aggregate;

    @Spy
    private TestESRepository repository = new TestESRepository(client, null);;

    @BeforeAll
    static void setupESConnection() {
        setts = EventStoreDBConnectionString.parseOrThrow(CONNECTION_STRING);
        client = EventStoreDBClient.create(setts);
    }

    @AfterAll
    static void teardownESConnection() throws ExecutionException, InterruptedException {
        client.shutdown();
    }

    @Test
    void applyAndWriteEventsThrowsIfAggregateIdIsNull() {
        when(aggregate.getId()).thenReturn(null);

        assertThatThrownBy(() -> {
            repository.applyAndWriteEvents(aggregate, Collections.emptyList());
        }).isInstanceOf(RuntimeException.class)
        .hasMessage("Aggregate id is null");
    }

    @Test
    void applyAndWriteEventsCallsApplyForMultipleEvents() throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, InterruptedException, ExecutionException {
        DomainEvent event1 = new TestEvent(UUID.randomUUID(), TestEventType.TEST);
        DomainEvent event2 = new TestEvent(UUID.randomUUID(), TestEventType.TEST);
        List<DomainEvent> events = Arrays.asList(event1, event2);
        UUID aggregateId = UUID.randomUUID();
        when(aggregate.getId()).thenReturn(aggregateId);

        repository.applyAndWriteEvents(aggregate, events);

        InOrder inOrder = inOrder(aggregate);
        inOrder.verify(aggregate).apply((TestEvent) event1);
        inOrder.verify(aggregate).apply((TestEvent) event2);
    }

    @Test
    void applyAndWriteEventsWritesEventsInEventStore()
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InterruptedException,
            ExecutionException, StreamReadException, DatabindException, IOException {
        DomainEvent event1 = new TestEvent(UUID.randomUUID(), TestEventType.TEST);
        DomainEvent event2 = new TestEvent(UUID.randomUUID(), TestEventType.TEST);
        List<DomainEvent> events = Arrays.asList(event1, event2);
        UUID aggregateId = UUID.randomUUID();
        when(aggregate.getId()).thenReturn(aggregateId);

        repository.applyAndWriteEvents(aggregate, events);

        String streamName = String.format("Test_%s", aggregateId);
        List<ResolvedEvent> resolvedEvents = readAllEventsFromStream(streamName);

        TestEvent firstWrittenEvent = new ObjectMapper().readValue(
                resolvedEvents.get(0).getOriginalEvent().getEventData(),
                TestEvent.class);
        TestEvent secondWrittenEvent = new ObjectMapper().readValue(
                resolvedEvents.get(1).getOriginalEvent().getEventData(),
                TestEvent.class);

        assertThat(firstWrittenEvent).isEqualTo(event1);
        assertThat(secondWrittenEvent).isEqualTo(event2);
    }

    @Test
    @SuppressWarnings("unused")
    void readEventsForGameProposalRetrievesAllPastEventsForAggregate()
            throws InterruptedException, ExecutionException, StreamReadException, DatabindException, IOException {
        UUID aggregateId = UUID.randomUUID();
        DomainEvent event1 = new TestEvent(UUID.randomUUID(), TestEventType.TEST);
        DomainEvent event2 = new TestEvent(UUID.randomUUID(), TestEventType.TEST);
        List<DomainEvent> events = Arrays.asList(event1, event2);
        String streamName = String.format("Test_%s", aggregateId);
        insertTestEventsInEventStore(streamName, events);

        List<DomainEvent> pastEvents = repository.readEventsForAggregate(aggregateId);

        List<ResolvedEvent> resolvedEvents = readAllEventsFromStream(streamName);
        verify(repository).convertEvents(resolvedEvents);
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
