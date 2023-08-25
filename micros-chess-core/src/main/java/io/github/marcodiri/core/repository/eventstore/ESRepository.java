package io.github.marcodiri.core.repository.eventstore;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.ReadResult;
import com.eventstore.dbclient.ReadStreamOptions;
import com.eventstore.dbclient.ResolvedEvent;
import com.eventstore.dbclient.WriteResult;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

import io.github.marcodiri.core.domain.Aggregate;
import io.github.marcodiri.core.domain.AggregateFactory;
import io.github.marcodiri.core.domain.event.DomainEvent;

public abstract class ESRepository {

    protected final EventStoreDBClient client;
    protected final AggregateFactory aggregateFactory;

    protected static final Logger LOGGER = LogManager.getLogger(ESRepository.class);

    public ESRepository(final EventStoreDBClient client, final AggregateFactory aggregateFactory) {
        this.client = client;
        this.aggregateFactory = aggregateFactory;
    }

    protected abstract String streamNameFromAggregateId(UUID aggregateId);

    protected abstract List<DomainEvent> convertEvents(List<ResolvedEvent> resolvedEvents)
            throws StreamReadException, DatabindException, IOException;

    /**
     * Read all events for given GameProposal id.
     *
     * @param gameProposalId
     * @return a list of {@link DomainEvent}.
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws StreamReadException
     * @throws DatabindException
     * @throws IOException
     */
    public List<DomainEvent> readEventsForAggregate(UUID gameProposalId)
            throws InterruptedException, ExecutionException, IOException {
        ReadStreamOptions readLastEvent = ReadStreamOptions.get()
                .forwards()
                .fromStart();

        ReadResult result = client.readStream(streamNameFromAggregateId(gameProposalId), readLastEvent)
                .get();

        List<ResolvedEvent> resolvedEvents = result.getEvents();
        List<DomainEvent> pastEvents = convertEvents(resolvedEvents);
        return pastEvents;
    }

    private WriteResult writeEventsForAggregate(Aggregate aggregate, List<EventData> appliedEventsData)
            throws InterruptedException, ExecutionException {
        UUID aggregateId = aggregate.getId();
        if (aggregateId == null) {
            throw new RuntimeException("Aggregate id is null");
        }
        WriteResult writeResult = client
                .appendToStream(streamNameFromAggregateId(aggregateId),
                        appliedEventsData.iterator())
                .get();
        return writeResult;
    }

    protected List<EventData> applyEventsToAggregate(Aggregate aggregate, List<DomainEvent> events)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        List<EventData> eventDataList = new ArrayList<>();
        for (DomainEvent event : events) {
            aggregate.getClass().getMethod("apply", event.getClass()).invoke(aggregate, event);
            EventData eventData = EventData
                    .builderAsJson(event.getType().toString(), event)
                    .build();
            eventDataList.add(eventData);
        }
        return eventDataList;
    }

    /**
     * Applies events to an Aggregate and writes events to EventStoreDB.
     *
     * @param aggregate the Aggregate to apply events to.
     * @param events    the events to be applied.
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws InterruptedException
     * @throws ExecutionException
     * @see Aggregate
     */
    protected void applyAndWriteEvents(Aggregate aggregate, List<DomainEvent> events)
            throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, InterruptedException, ExecutionException {
        List<EventData> appliedEventsData = applyEventsToAggregate(aggregate, events);
        WriteResult writeResult = writeEventsForAggregate(aggregate, appliedEventsData);
        LOGGER.info("Saved events to EventStore: {}", events);
        LOGGER.debug(writeResult);
    }

}
