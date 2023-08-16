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
import io.github.marcodiri.lobbyservice.domain.GameProposal;
import io.github.marcodiri.lobbyservice.domain.GameProposalFactory;
import io.github.marcodiri.lobbyservice.domain.command.AcceptGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.command.CancelGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.command.CreateGameProposalCommand;

public class GameProposalESRepository {

    private EventStoreDBClient client;
    private GameProposalFactory gameProposalFactory;

    private static final Logger LOGGER = LogManager.getLogger(GameProposalESRepository.class);

    public GameProposalESRepository(EventStoreDBClient client, GameProposalFactory gameProposalFactory) {
        this.client = client;
        this.gameProposalFactory = gameProposalFactory;
    }

    public GameProposal save(CreateGameProposalCommand cmd) throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
            ExecutionException {
        GameProposal gameProposal = gameProposalFactory.createGameProposal();

        List<DomainEvent> events = gameProposal.process(cmd);
        List<EventData> appliedEventsData = applyEventsToGameProposal(gameProposal, events);
        WriteResult writeResult = writeEventsForGameProposal(gameProposal, appliedEventsData);

        LOGGER.info("Saved events to EventStore: {}", events);
        LOGGER.debug(writeResult);

        return gameProposal;
    }

    public GameProposal update(UUID gameProposalId, CancelGameProposalCommand cmd)
            throws StreamReadException, DatabindException, InterruptedException, ExecutionException, IOException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        GameProposal gameProposal = gameProposalFactory.createGameProposal();
        List<DomainEvent> pastEvents = readEventsForGameProposal(gameProposal);
        applyEventsToGameProposal(gameProposal, pastEvents);
        LOGGER.info("Restored GameProposal from events: {}, \n{}", pastEvents, gameProposal);

        List<DomainEvent> events = gameProposal.process(cmd);
        List<EventData> appliedEventsData = applyEventsToGameProposal(gameProposal, events);
        WriteResult writeResult = writeEventsForGameProposal(gameProposal, appliedEventsData);
        LOGGER.info("Saved events to EventStore: {}", events);
        LOGGER.debug(writeResult);

        return gameProposal;
    }

    public GameProposal update(UUID gameProposalId, AcceptGameProposalCommand cmd) {
        return null;
    }

    private String streamNameFromGameProposal(GameProposal gameProposal) {
        UUID gameProposalId = gameProposal.getId();
        return String.format("GameProposal_%s", gameProposalId);
    }

    List<DomainEvent> readEventsForGameProposal(GameProposal gameProposal)
            throws InterruptedException, ExecutionException, StreamReadException, DatabindException, IOException {
        ReadStreamOptions readLastEvent = ReadStreamOptions.get()
                .forwards()
                .fromStart();

        ReadResult result = client.readStream(streamNameFromGameProposal(gameProposal), readLastEvent)
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

    private WriteResult writeEventsForGameProposal(GameProposal gameProposal, List<EventData> appliedEventsData)
            throws InterruptedException, ExecutionException {
        WriteResult writeResult = client
                .appendToStream(streamNameFromGameProposal(gameProposal), appliedEventsData.iterator())
                .get();
        return writeResult;
    }

    /**
     * Applies events to a GameProposal and returns applied events ready for EventStoreDB.
     * @param gameProposal the GameProposal to apply events to.
     * @param events       the events to be applied.
     * @return a list of applied events ready to be sent to EventStoreDB.
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @see GameProposal
     * @see EventData
     */
    private List<EventData> applyEventsToGameProposal(GameProposal gameProposal, List<DomainEvent> events)
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

}
