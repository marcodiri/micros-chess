package io.github.marcodiri.lobbyservice.web;

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
import io.github.marcodiri.lobbyservice.api.event.GameProposalCreated;
import io.github.marcodiri.lobbyservice.domain.GameProposalFactory;
import io.github.marcodiri.lobbyservice.repository.eventstore.EventStoreDBClientFactory;
import io.github.marcodiri.lobbyservice.repository.eventstore.GameProposalESRepository;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class LobbyControllerIT {

    private final UUID testPlayerId = UUID.fromString("73531011-830e-4cc9-860b-f0228735544e");

    private static GameProposalESRepository repository;

    @BeforeAll
    static void setupRepository() {
        repository = new GameProposalESRepository(new EventStoreDBClientFactory().createClient(),
                new GameProposalFactory());
    }

    @Test
    void ping() {
        get("micros-chess/rest/lobby/ping")
                .then()
                .assertThat()
                .statusCode(200)
                .body(equalTo("service is online"));
    }

    @Test
    void createGameProposal()
            throws StreamReadException, DatabindException, InterruptedException, ExecutionException, IOException {
        RequestSpecification request = with()
                .contentType(ContentType.JSON)
                .body("{ \"creatorId\" : \"" + testPlayerId + "\" }");

        Response post = request.post("micros-chess/rest/lobby/create-game-proposal");

        post.then().assertThat().statusCode(equalTo(200));
        CreateGameProposalResponse response = post.getBody().as(CreateGameProposalResponse.class);
        UUID gameProposalId = response.getGameProposalId();
        List<DomainEvent> writtenEvents = repository.readEventsForAggregate(gameProposalId);
        assertThat(writtenEvents).containsOnly(new GameProposalCreated(gameProposalId, testPlayerId));
    }

}
