package io.github.marcodiri.gameservice.domain;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.marcodiri.core.domain.event.DomainEvent;
import io.github.marcodiri.gameservice.api.event.GameCreated;
import io.github.marcodiri.gameservice.api.event.MovePlayed;
import io.github.marcodiri.gameservice.domain.command.CreateGameCommand;
import io.github.marcodiri.gameservice.domain.command.PlayMoveCommand;

public class GameAggregate {

    private UUID id;
    private GameState state;

    private static final Logger LOGGER = LogManager.getLogger(GameAggregate.class);

    public GameAggregate() {
    }

    public UUID getId() {
        return id;
    }

    public GameState getState() {
        return state;
    }

    public UUID generateId() {
        return UUID.randomUUID();
    }

    public List<DomainEvent> process(CreateGameCommand command) {
        LOGGER.info("Calling process for CreateGameCommand: {}", command);
        UUID gameId = generateId();
        return Collections.singletonList(
                new GameCreated(gameId, command.getPlayer1Id(), command.getPlayer2Id()));
    }

    public List<DomainEvent> process(PlayMoveCommand command) throws GameNotInProgressException, IllegalMoveException {
        LOGGER.info("Calling process for PlayMoveCommand: {}", command);
        if (getState() != GameState.IN_PROGRESS) {
            throw new GameNotInProgressException(getState());
        }
        if (!moveIsLegal()) {
            throw new IllegalMoveException(command.getMove());
        }
        return Collections.singletonList(
                new MovePlayed(getId(), command.getPlayerId(), command.getMove()));
    }

    boolean moveIsLegal() {
        // TODO
        return true;
    }

}
