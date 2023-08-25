package io.github.marcodiri.gameservice.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.marcodiri.core.domain.event.DomainEvent;
import io.github.marcodiri.gameservice.api.event.GameCreated;
import io.github.marcodiri.gameservice.api.event.MovePlayed;
import io.github.marcodiri.gameservice.domain.command.CreateGameCommand;
import io.github.marcodiri.gameservice.domain.command.PlayMoveCommand;

public class GameAggregate {

    private UUID id;
    private UUID player1Id;
    private UUID player2Id;
    private List<ImmutablePair<UUID, String>> movesList;
    private GameState state;

    private static final Logger LOGGER = LogManager.getLogger(GameAggregate.class);

    public GameAggregate() {
    }

    public UUID getId() {
        return id;
    }

    UUID generateId() {
        return UUID.randomUUID();
    }

    UUID getPlayer1Id() {
        return player1Id;
    }

    UUID getPlayer2Id() {
        return player2Id;
    }

    GameState getState() {
        return state;
    }

    List<ImmutablePair<UUID, String>> getMovesList() {
        return movesList;
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

    public void apply(GameCreated event) {
        LOGGER.info("Calling apply for GameCreated: {}", event);
        this.id = event.getGameId();
        this.player1Id = event.getPlayer1Id();
        this.player2Id = event.getPlayer2Id();
        this.movesList = new ArrayList<>();
        this.state = GameState.IN_PROGRESS;
    }

    public void apply(MovePlayed event) {
        LOGGER.info("Calling apply for MovePlayed: {}", event);
        getMovesList().add(ImmutablePair.of(event.getPlayerId(), event.getMove()));
    }

    boolean moveIsLegal() {
        // TODO
        return true;
    }

}
