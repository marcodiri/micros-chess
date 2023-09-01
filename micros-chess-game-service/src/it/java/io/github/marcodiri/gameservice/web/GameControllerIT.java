package io.github.marcodiri.gameservice.web;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.with;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

import io.github.marcodiri.core.domain.event.DomainEvent;
import io.github.marcodiri.gameservice.api.event.GameCreated;
import io.github.marcodiri.gameservice.api.web.CreateGameResponse;
import io.github.marcodiri.gameservice.api.web.PlayMoveRequest;
import io.github.marcodiri.gameservice.domain.GameFactory;
import io.github.marcodiri.gameservice.repository.eventstore.EventStoreDBClientFactory;
import io.github.marcodiri.gameservice.repository.eventstore.GameESRepository;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class GameControllerIT {

    private final UUID testPlayer1Id = UUID.fromString("73531011-830e-4cc9-860b-f0228735544e");
    private final UUID testPlayer2Id = UUID.fromString("12345678-830e-4cc9-860b-f0228735544e");
    private final String move = "e4";

    private static GameESRepository repository;

    @BeforeAll
    static void setupRepository() {
        repository = new GameESRepository(new EventStoreDBClientFactory().createClient(),
                new GameFactory());
    }

    @Test
    void ping() {
        get("micros-chess/rest/game/ping")
                .then()
                .assertThat()
                .statusCode(200)
                .body(equalTo("service is online"));
    }

    @Test
    void createGame()
            throws StreamReadException, DatabindException, InterruptedException, ExecutionException,
            IOException {
        RequestSpecification request = with()
                .contentType(ContentType.JSON)
                .body("{ \"player1Id\" : \"" + testPlayer1Id + "\", \"player2Id\" : \"" + testPlayer2Id + "\" }");

        Response post = request.post("micros-chess/rest/game/create-game");

        post.then().assertThat().statusCode(equalTo(200));
        CreateGameResponse response = post.getBody().as(CreateGameResponse.class);
        UUID gameId = response.getGameId();
        List<DomainEvent> writtenEvents = repository.readEventsForAggregate(gameId);
        assertThat(writtenEvents).containsOnly(new GameCreated(gameId, testPlayer1Id, testPlayer2Id));
    }

    @Test
    void playMove()
            throws StreamReadException, DatabindException, InterruptedException,
            ExecutionException,
            IOException {
        RequestSpecification createRequest = with()
                .contentType(ContentType.JSON)
                .body("{ \"player1Id\" : \"" + testPlayer1Id + "\", \"player2Id\" : \"" + testPlayer2Id + "\" }");

        Response cratePost = createRequest.post("micros-chess/rest/game/create-game");

        CreateGameResponse response = cratePost.getBody().as(CreateGameResponse.class);
        UUID gameId = response.getGameId();
        RequestSpecification playMoveRequest = with()
                .contentType(ContentType.JSON)
                .body("{ \"gameId\" : \"" + gameId + "\", \"playerId\" : \"" + testPlayer1Id
                        + "\", \"move\" : \"" + move + "\" }");

        Response cancelPost = playMoveRequest.post("micros-chess/rest/game/play-move");

        cancelPost.then().assertThat().statusCode(equalTo(200));
        List<DomainEvent> writtenEvents = repository.readEventsForAggregate(gameId);
        assertThat(writtenEvents.get(1)).isEqualTo(new PlayMoveRequest(gameId, testPlayer1Id, move));
    }

}
