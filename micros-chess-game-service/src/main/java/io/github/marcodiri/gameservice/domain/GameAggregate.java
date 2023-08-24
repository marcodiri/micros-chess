package io.github.marcodiri.gameservice.domain;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.marcodiri.core.domain.event.DomainEvent;
import io.github.marcodiri.gameservice.api.event.GameCreated;
import io.github.marcodiri.gameservice.domain.command.CreateGameCommand;

public class GameAggregate {
    private static final Logger LOGGER = LogManager.getLogger(GameAggregate.class);

    public UUID generateId() {
        return UUID.randomUUID();
    }

    public List<DomainEvent> process(CreateGameCommand command) {
        LOGGER.info("Calling process for CreateGameCommand: {}", command);
        UUID gameId = generateId();
        return Collections.singletonList(
                new GameCreated(gameId, command.getPlayer1Id(), command.getPlayer2Id()));
    }

}
