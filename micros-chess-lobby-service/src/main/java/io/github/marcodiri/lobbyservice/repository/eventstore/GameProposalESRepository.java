package io.github.marcodiri.lobbyservice.repository.eventstore;

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
import com.eventstore.dbclient.RecordedEvent;
import com.eventstore.dbclient.ResolvedEvent;
import com.eventstore.dbclient.WriteResult;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.marcodiri.core.domain.event.DomainEvent;
import io.github.marcodiri.lobbyservice.api.event.GameProposalEventType;
import io.github.marcodiri.lobbyservice.domain.GameProposalAggregate;
import io.github.marcodiri.lobbyservice.domain.GameProposalFactory;
import io.github.marcodiri.lobbyservice.domain.UnsupportedStateTransitionException;
import io.github.marcodiri.lobbyservice.domain.command.AcceptGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.command.CancelGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.command.CreateGameProposalCommand;
import jakarta.inject.Inject;

public class GameProposalESRepository {

    private final EventStoreDBClient client;
    private final GameProposalFactory gameProposalFactory;

    private static final Logger LOGGER = LogManager.getLogger(GameProposalESRepository.class);

    @Inject
    public GameProposalESRepository(final EventStoreDBClient client, final GameProposalFactory gameProposalFactory) {
        this.client = client;
        this.gameProposalFactory = gameProposalFactory;
    }

    public GameProposalAggregate save(CreateGameProposalCommand cmd)
            throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
            ExecutionException {
        GameProposalAggregate gameProposal = gameProposalFactory.createAggregate();

        List<DomainEvent> events = gameProposal.process(cmd);
        applyAndWriteEvents(gameProposal, events);

        return gameProposal;
    }

    public GameProposalAggregate update(UUID gameProposalId, CancelGameProposalCommand cmd)
            throws StreamReadException, DatabindException, InterruptedException, ExecutionException, IOException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            UnsupportedStateTransitionException {
        GameProposalAggregate gameProposal = restoreAggregate(gameProposalId);

        List<DomainEvent> events = gameProposal.process(cmd);
        applyAndWriteEvents(gameProposal, events);

        return gameProposal;
    }

    public GameProposalAggregate update(UUID gameProposalId, AcceptGameProposalCommand cmd)
            throws StreamReadException, DatabindException, InterruptedException, ExecutionException, IOException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            UnsupportedStateTransitionException {
        GameProposalAggregate gameProposal = restoreAggregate(gameProposalId);

        List<DomainEvent> events = gameProposal.process(cmd);
        applyAndWriteEvents(gameProposal, events);

        return gameProposal;
    }

    private GameProposalAggregate restoreAggregate(UUID gameProposalId)
            throws InterruptedException, ExecutionException, StreamReadException,
            DatabindException, IOException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        GameProposalAggregate gameProposal = gameProposalFactory.createAggregate();
        List<DomainEvent> pastEvents = readEventsForAggregate(gameProposalId);
        applyEventsToAggregate(gameProposal, pastEvents);
        LOGGER.info("Restored GameProposal from events: {}, \n{}", pastEvents, gameProposal);
        return gameProposal;
    }

    private String streamNameFromAggregateId(UUID gameProposalId) {
        return String.format("GameProposal_%s", gameProposalId);
    }

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
            throws InterruptedException, ExecutionException, StreamReadException, DatabindException, IOException {
        ReadStreamOptions readLastEvent = ReadStreamOptions.get()
                .forwards()
                .fromStart();

        ReadResult result = client.readStream(streamNameFromAggregateId(gameProposalId), readLastEvent)
                .get();

        List<ResolvedEvent> resolvedEvents = result.getEvents();

        List<DomainEvent> pastEvents = new ArrayList<>();
        for (ResolvedEvent resolvedEvent : resolvedEvents) {
            RecordedEvent originalEvent = resolvedEvent.getOriginalEvent();
            DomainEvent event = new ObjectMapper().readValue(
                    originalEvent.getEventData(),
                    GameProposalEventType.fromString(originalEvent.getEventType()).getEventClass());
            pastEvents.add(event);
        }
        return pastEvents;
    }

    WriteResult writeEventsForAggregate(GameProposalAggregate gameProposal, List<EventData> appliedEventsData)
            throws InterruptedException, ExecutionException {
        UUID gameProposalId = gameProposal.getId();
        if (gameProposalId == null) {
            throw new RuntimeException("GameProposal id is null");
        }
        WriteResult writeResult = client
                .appendToStream(streamNameFromAggregateId(gameProposalId), appliedEventsData.iterator())
                .get();
        return writeResult;
    }

    /**
     * Applies events to a GameProposal and returns applied events ready for
     * EventStoreDB.
     *
     * @param gameProposal the GameProposal to apply events to.
     * @param events       the events to be applied.
     * @return a list of applied events ready to be sent to EventStoreDB.
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @see GameProposalAggregate
     * @see EventData
     */
    private List<EventData> applyEventsToAggregate(GameProposalAggregate gameProposal, List<DomainEvent> events)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        List<EventData> eventDataList = new ArrayList<>();
        for (DomainEvent event : events) {
            gameProposal.getClass().getMethod("apply", event.getClass()).invoke(gameProposal, event);
            EventData eventData = EventData
                    .builderAsJson(event.getType().toString(), event)
                    .build();
            eventDataList.add(eventData);
        }
        return eventDataList;
    }

    /**
     * Applies events to a GameProposal and writes events to EventStoreDB.
     *
     * @param gameProposal the GameProposal to apply events to.
     * @param events       the events to be applied.
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws InterruptedException
     * @throws ExecutionException
     * @see GameProposalAggregate
     */
    private void applyAndWriteEvents(GameProposalAggregate gameProposal, List<DomainEvent> events)
            throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, InterruptedException, ExecutionException {
        List<EventData> appliedEventsData = applyEventsToAggregate(gameProposal, events);
        WriteResult writeResult = writeEventsForAggregate(gameProposal, appliedEventsData);
        LOGGER.info("Saved events to EventStore: {}", events);
        LOGGER.debug(writeResult);
    }

}
