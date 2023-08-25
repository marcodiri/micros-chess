package io.github.marcodiri.lobbyservice.repository.eventstore;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.RecordedEvent;
import com.eventstore.dbclient.ResolvedEvent;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.marcodiri.core.domain.event.DomainEvent;
import io.github.marcodiri.core.repository.eventstore.ESRepository;
import io.github.marcodiri.lobbyservice.api.event.GameProposalEventType;
import io.github.marcodiri.lobbyservice.domain.GameProposalAggregate;
import io.github.marcodiri.lobbyservice.domain.GameProposalFactory;
import io.github.marcodiri.lobbyservice.domain.UnsupportedStateTransitionException;
import io.github.marcodiri.lobbyservice.domain.command.AcceptGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.command.CancelGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.command.CreateGameProposalCommand;
import jakarta.inject.Inject;

public class GameProposalESRepository extends ESRepository {

    @Inject
    public GameProposalESRepository(final EventStoreDBClient client, final GameProposalFactory gameProposalFactory) {
        super(client, gameProposalFactory);
    }

    public GameProposalAggregate save(CreateGameProposalCommand cmd)
            throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
            ExecutionException {
        GameProposalAggregate gameProposal = (GameProposalAggregate) aggregateFactory.createAggregate();

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
        GameProposalAggregate gameProposal = (GameProposalAggregate) aggregateFactory.createAggregate();
        List<DomainEvent> pastEvents = readEventsForAggregate(gameProposalId);
        applyEventsToAggregate(gameProposal, pastEvents);
        LOGGER.info("Restored GameProposal from events: {}, \n{}", pastEvents, gameProposal);
        return gameProposal;
    }

    @Override
    protected String streamNameFromAggregateId(UUID gameProposalId) {
        return String.format("GameProposal_%s", gameProposalId);
    }

    @Override
    protected List<DomainEvent> convertEvents(List<ResolvedEvent> resolvedEvents)
            throws StreamReadException, DatabindException, IOException {
        List<DomainEvent> domainEvents = new ArrayList<>();
        for (ResolvedEvent resolvedEvent : resolvedEvents) {
            RecordedEvent originalEvent = resolvedEvent.getOriginalEvent();
            DomainEvent event = new ObjectMapper().readValue(
                    originalEvent.getEventData(),
                    GameProposalEventType.fromString(originalEvent.getEventType()).getEventClass());
            domainEvents.add(event);
        }
        return domainEvents;
    }

}
