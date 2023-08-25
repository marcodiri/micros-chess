package io.github.marcodiri.gameservice.repository.eventstore;

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
import io.github.marcodiri.gameservice.api.event.GameEventType;
import io.github.marcodiri.gameservice.domain.GameAggregate;
import io.github.marcodiri.gameservice.domain.GameFactory;
import io.github.marcodiri.gameservice.domain.GameNotInProgressException;
import io.github.marcodiri.gameservice.domain.IllegalMoveException;
import io.github.marcodiri.gameservice.domain.command.CreateGameCommand;
import io.github.marcodiri.gameservice.domain.command.PlayMoveCommand;

public class GameESRepository extends ESRepository {

    public GameESRepository(final EventStoreDBClient client, final GameFactory gameFactory) {
        super(client, gameFactory);
    }

    public GameAggregate save(CreateGameCommand cmd) throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, InterruptedException, ExecutionException {
        GameAggregate game = (GameAggregate) aggregateFactory.createAggregate();

        List<DomainEvent> events = game.process(cmd);
        applyAndWriteEvents(game, events);

        return game;
    }

    public GameAggregate update(UUID gameId, PlayMoveCommand cmd) throws StreamReadException, DatabindException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, InterruptedException,
            ExecutionException, IOException, GameNotInProgressException, IllegalMoveException {
        GameAggregate game = restoreAggregate(gameId);

        List<DomainEvent> events = game.process(cmd);
        applyAndWriteEvents(game, events);

        return game;
    }

    private GameAggregate restoreAggregate(UUID gameId)
            throws InterruptedException, ExecutionException, StreamReadException,
            DatabindException, IOException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        GameAggregate game = (GameAggregate) aggregateFactory.createAggregate();
        List<DomainEvent> pastEvents = readEventsForAggregate(gameId);
        applyEventsToAggregate(game, pastEvents);
        LOGGER.info("Restored Game from events: {}, \n{}", pastEvents, game);
        return game;
    }

    @Override
    protected String streamNameFromAggregateId(UUID gameId) {
        return String.format("Game_%s", gameId);
    }

    @Override
    protected List<DomainEvent> convertEvents(List<ResolvedEvent> resolvedEvents)
            throws StreamReadException, DatabindException, IOException {
        List<DomainEvent> domainEvents = new ArrayList<>();
        for (ResolvedEvent resolvedEvent : resolvedEvents) {
            RecordedEvent originalEvent = resolvedEvent.getOriginalEvent();
            DomainEvent event = new ObjectMapper().readValue(
                    originalEvent.getEventData(),
                    GameEventType.fromString(originalEvent.getEventType()).getEventClass());
            domainEvents.add(event);
        }
        return domainEvents;
    }

}
