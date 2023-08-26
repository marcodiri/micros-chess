package io.github.marcodiri.gameservice.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

import io.github.marcodiri.gameservice.domain.command.CreateGameCommand;
import io.github.marcodiri.gameservice.domain.command.PlayMoveCommand;
import io.github.marcodiri.gameservice.repository.eventstore.GameESRepository;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class GameServiceTest {

    @Mock
    private GameESRepository gameESRepository;

    @InjectMocks
    private GameService gameService;

    @Nested
    class CreateGame {

        @Test
        void createGameCallsRepositorySaveWithCommand() throws IllegalAccessException, InvocationTargetException,
                NoSuchMethodException, InterruptedException, ExecutionException {
            UUID player1Id = UUID.randomUUID();
            UUID player2Id = UUID.randomUUID();
            CreateGameCommand expectedCommand = new CreateGameCommand(player1Id, player2Id);

            gameService.createGame(player1Id, player2Id);

            verify(gameESRepository).save(expectedCommand);
        }

        @Test
        void createGameReturnsAggregate() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException {
            GameAggregate game = new GameAggregate();
            when(gameESRepository.save(any())).thenReturn(game);

            GameAggregate returnedGame = gameService.createGame(UUID.randomUUID(), UUID.randomUUID());

            assertThat(returnedGame).isEqualTo(game);
        }

    }

    @Nested
    class PlayMove {

        @Test
        void playMoveCallsRepositoryUpdateWithCommand() throws StreamReadException, DatabindException,
                IllegalAccessException, InvocationTargetException, NoSuchMethodException, InterruptedException,
                ExecutionException, IOException, GameNotInProgressException, IllegalMoveException {
            UUID gameId = UUID.randomUUID();
            UUID playerId = UUID.randomUUID();
            String move = "e4";
            PlayMoveCommand expectedCommand = new PlayMoveCommand(playerId, move);

            gameService.playMove(gameId, playerId, move);

            verify(gameESRepository).update(gameId, expectedCommand);
        }

        @Test
        void playMoveReturnsAggregate() throws StreamReadException, DatabindException, IllegalAccessException,
                InvocationTargetException, NoSuchMethodException, InterruptedException, ExecutionException, IOException,
                GameNotInProgressException, IllegalMoveException {
            GameAggregate game = new GameAggregate();
            when(gameESRepository.update(isA(UUID.class), isA(PlayMoveCommand.class))).thenReturn(game);

            GameAggregate returnedGame = gameService.playMove(UUID.randomUUID(), UUID.randomUUID(), "");

            assertThat(returnedGame).isEqualTo(game);
        }

    }

}
