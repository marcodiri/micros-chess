package io.github.marcodiri.gameservice.web;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.with;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.marcodiri.gameservice.api.web.Move;
import io.github.marcodiri.gameservice.domain.GameAggregate;
import io.github.marcodiri.gameservice.domain.GameNotInProgressException;
import io.github.marcodiri.gameservice.domain.GameService;
import io.github.marcodiri.gameservice.domain.GameState;
import io.github.marcodiri.gameservice.domain.IllegalMoveException;
import io.github.marcodiri.rest.InMemoryRestServer;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;

@ExtendWith(MockitoExtension.class)
public class GameControllerTest {

    @Mock
    private GameService gameService;

    @InjectMocks
    private GameController gameController;

    @Mock
    GameAggregate game;

    private static InMemoryRestServer server;

    private final UUID testPlayer1Id = UUID.fromString("73531011-830e-4cc9-860b-f0228735544e");
    private final UUID testPlayer2Id = UUID.fromString("12345678-830e-4cc9-860b-f0228735544e");

    @BeforeEach
    void setup() throws IOException {
        server = InMemoryRestServer.create(gameController);
    }

    @AfterEach
    void teardown() {
        server.close();
    }

    @Test
    void ping() throws IOException {
        get(server.target("/game/ping").getUri())
                .then()
                .assertThat()
                .statusCode(equalTo(200))
                .and()
                .body(equalTo("service is online"));
    }

    @Nested
    class CreateGame {

        @Test
        void JSONResponse() throws Exception {
            UUID gameId = UUID.randomUUID();
            when(game.getId()).thenReturn(gameId);
            when(gameService.createGame(testPlayer1Id, testPlayer2Id)).thenReturn(game);
            RequestSpecification request = with()
                    .contentType(ContentType.JSON)
                    .body("{ \"player1Id\" : \"" + testPlayer1Id + "\", \"player2Id\" : \"" + testPlayer2Id + "\" }");

            Response post = request.post(server.target("/game/create-game").getUri());
            ResponseBody<?> responseBody = post.getBody();

            verify(gameService).createGame(testPlayer1Id, testPlayer2Id);
            post.then()
                    .assertThat()
                    .statusCode(equalTo(200))
                    .and()
                    .contentType(ContentType.JSON);
            assertThat(responseBody.asString()).isEqualTo("{\"gameId\":\"" + gameId + "\"}");
        }

        @Test
        void internalErrorOnException() throws Exception {
            when(gameService.createGame(any(UUID.class), any(UUID.class))).thenThrow(new IllegalAccessException());

            given()
                    .contentType(ContentType.JSON)
                    .body("{ \"player1Id\" : \"" + testPlayer1Id + "\", \"player2Id\" : \"" + testPlayer2Id + "\" }")
                    .when()
                    .post(server.target("/game/create-game").getUri())
                    .then()
                    .assertThat()
                    .statusCode(equalTo(500));
        }

    }

    @Nested
    class PlayMove {

        private String moveString = "{\"from\":\"e2\", \"to\":\"e4\"}";
        private Move move = new Move("e2", "e4");

        @Test
        void OKResponse() throws Exception {
            UUID gameId = UUID.randomUUID();

            given()
                    .contentType(ContentType.JSON)
                    .body("{ \"gameId\" : \"" + gameId + "\", \"playerId\" : \"" + testPlayer1Id
                            + "\", \"move\" : " + moveString + " }")
                    .when()
                    .post(server.target("/game/play-move").getUri())
                    .then()
                    .assertThat()
                    .statusCode(equalTo(200));

            verify(gameService).playMove(gameId, testPlayer1Id, move);
        }

        @Test
        void badRequestOnGameNotInProgressException() throws Exception {
            UUID gameId = UUID.randomUUID();
            when(gameService.playMove(any(UUID.class), any(UUID.class), any(Move.class)))
                    .thenThrow(new GameNotInProgressException(GameState.ENDED));

            given()
                    .contentType(ContentType.JSON)
                    .body("{ \"gameId\" : \"" + gameId + "\", \"playerId\" : \"" + testPlayer1Id
                            + "\", \"move\" : " + moveString + " }")
                    .when()
                    .post(server.target("/game/play-move").getUri())
                    .then()
                    .assertThat()
                    .statusCode(equalTo(400));
        }

        @Test
        void badRequestOnIllegalMoveException() throws Exception {
            UUID gameId = UUID.randomUUID();
            when(gameService.playMove(any(UUID.class), any(UUID.class), any(Move.class)))
                    .thenThrow(new IllegalMoveException(move));

            given()
                    .contentType(ContentType.JSON)
                    .body("{ \"gameId\" : \"" + gameId + "\", \"playerId\" : \"" + testPlayer1Id
                            + "\", \"move\" : " + moveString + " }")
                    .when()
                    .post(server.target("/game/play-move").getUri())
                    .then()
                    .assertThat()
                    .statusCode(equalTo(400));
        }

        @Test
        void internalErrorOnException() throws Exception {
            UUID gameId = UUID.randomUUID();
            when(gameService.playMove(any(UUID.class), any(UUID.class), any(Move.class)))
                    .thenThrow(new IllegalArgumentException());

            given()
                    .contentType(ContentType.JSON)
                    .body("{ \"gameId\" : \"" + gameId + "\", \"playerId\" : \"" + testPlayer1Id
                            + "\", \"move\" : " + moveString + " }")
                    .when()
                    .post(server.target("/game/play-move").getUri())
                    .then()
                    .assertThat()
                    .statusCode(equalTo(500));
        }

    }

}
