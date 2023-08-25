package io.github.marcodiri.gameservice.repository.eventstore;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.WriteResult;

import io.github.marcodiri.core.domain.event.DomainEvent;
import io.github.marcodiri.gameservice.domain.GameAggregate;
import io.github.marcodiri.gameservice.domain.GameFactory;
import io.github.marcodiri.gameservice.domain.command.CreateGameCommand;

public class GameESRepository {

    private final EventStoreDBClient client;
    private final GameFactory gameFactory;

    private static final Logger LOGGER = LogManager.getLogger(GameESRepository.class);

    public GameESRepository(final EventStoreDBClient client, final GameFactory gameFactory) {
        this.client = client;
        this.gameFactory = gameFactory;
    }

    public GameAggregate save(CreateGameCommand cmd) throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, InterruptedException, ExecutionException {
        GameAggregate game = gameFactory.createAggregate();

        List<DomainEvent> events = game.process(cmd);
        applyAndWriteEvents(game, events);

        return game;
    }

    private String streamNameFromAggregateId(UUID gameId) {
        return String.format("Game_%s", gameId);
    }

    WriteResult writeEventsForAggregate(GameAggregate game, List<EventData> appliedEventsData)
            throws InterruptedException, ExecutionException {
        UUID gameId = game.getId();
        if (gameId == null) {
            throw new RuntimeException("Game id is null");
        }
        WriteResult writeResult = client
        .appendToStream(streamNameFromAggregateId(gameId),
        appliedEventsData.iterator())
        .get();
        return writeResult;
    }

    /**
     * Applies events to a Game and returns applied events ready for
     * EventStoreDB.
     *
     * @param game   the Game to apply events to.
     * @param events the events to be applied.
     * @return a list of applied events ready to be sent to EventStoreDB.
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @see GameAggregate
     * @see EventData
     */
    private List<EventData> applyEventsToAggregate(GameAggregate game, List<DomainEvent> events)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        List<EventData> eventDataList = new ArrayList<>();
        for (DomainEvent event : events) {
            game.getClass().getMethod("apply", event.getClass()).invoke(game, event);
            EventData eventData = EventData
                    .builderAsJson(event.getType().toString(), event)
                    .build();
            eventDataList.add(eventData);
        }
        return eventDataList;
    }

    /**
     * Applies events to a Game and writes events to EventStoreDB.
     *
     * @param game   the Game to apply events to.
     * @param events the events to be applied.
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws InterruptedException
     * @throws ExecutionException
     * @see GameAggregate
     */
    private void applyAndWriteEvents(GameAggregate game, List<DomainEvent> events)
            throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, InterruptedException, ExecutionException {
        List<EventData> appliedEventsData = applyEventsToAggregate(game, events);
        WriteResult writeResult = writeEventsForAggregate(game, appliedEventsData);
        LOGGER.info("Saved events to EventStore: {}", events);
        LOGGER.debug(writeResult);
    }

}
