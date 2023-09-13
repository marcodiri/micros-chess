package io.github.marcodiri.gameservice.domain;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

import io.github.marcodiri.gameservice.api.web.Move;
import io.github.marcodiri.gameservice.domain.command.CreateGameCommand;
import io.github.marcodiri.gameservice.domain.command.PlayMoveCommand;
import io.github.marcodiri.gameservice.repository.eventstore.GameESRepository;
import jakarta.inject.Inject;

public class GameService {

    private final GameESRepository gameESRepository;

    private static final Logger LOGGER = LogManager.getLogger(GameService.class);

    @Inject
    public GameService(final GameESRepository gameESRepository) {
        this.gameESRepository = gameESRepository;
    }

    public GameAggregate createGame(UUID player1Id, UUID player2Id) throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, InterruptedException, ExecutionException {
        GameAggregate game = gameESRepository.save(new CreateGameCommand(player1Id, player2Id));
        LOGGER.info("Created: {}", game);
        return game;
    }

    public GameAggregate playMove(UUID gameId, UUID playerId, Move move) throws StreamReadException,
            DatabindException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            InterruptedException, ExecutionException, IOException, GameNotInProgressException, IllegalMoveException {
        GameAggregate game = gameESRepository.update(gameId, new PlayMoveCommand(playerId, move));
        LOGGER.info("Played move in: {}", game);
        return game;
    }

}
