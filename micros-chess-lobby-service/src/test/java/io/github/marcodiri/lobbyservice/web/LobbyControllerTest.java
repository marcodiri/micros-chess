package io.github.marcodiri.lobbyservice.web;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.with;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

import io.github.marcodiri.lobbyservice.domain.GameProposalAggregate;
import io.github.marcodiri.lobbyservice.domain.GameProposalState;
import io.github.marcodiri.lobbyservice.domain.LobbyService;
import io.github.marcodiri.lobbyservice.domain.UnsupportedStateTransitionException;
import io.github.marcodiri.rest.InMemoryRestServer;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;

@ExtendWith(MockitoExtension.class)
public class LobbyControllerTest {

    @Mock
    private LobbyService lobbyService;

    @InjectMocks
    private LobbyController lobbyController;

    @Mock
    GameProposalAggregate gameProposal;

    private static InMemoryRestServer server;

    private final UUID testPlayerId = UUID.fromString("73531011-830e-4cc9-860b-f0228735544e");

    @BeforeEach
    void setup() throws IOException {
        server = InMemoryRestServer.create(lobbyController);
    }

    @AfterEach
    void teardown() {
        server.close();
    }

    @Test
    void ping() throws IOException {
        get(server.target("/lobby/ping").getUri())
                .then()
                .assertThat()
                .statusCode(equalTo(200))
                .and()
                .body(equalTo("service is online"));
    }

    @Nested
    class CreateGameProposal {

        @Test
        void JSONResponse()
                throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
                NoSuchMethodException, SecurityException, InterruptedException, ExecutionException {
            UUID gameProposalId = UUID.randomUUID();
            when(gameProposal.getId()).thenReturn(gameProposalId);
            when(lobbyService.createGameProposal(testPlayerId)).thenReturn(gameProposal);
            RequestSpecification request = with()
                    .contentType(ContentType.JSON)
                    .body("{ \"creatorId\" : \"" + testPlayerId + "\" }");

            Response post = request.post(server.target("/lobby/create-game-proposal").getUri());
            ResponseBody<?> responseBody = post.getBody();

            verify(lobbyService).createGameProposal(testPlayerId);
            post.then()
                    .assertThat()
                    .statusCode(equalTo(200))
                    .and()
                    .contentType(ContentType.JSON);
            assertThat(responseBody.asString()).isEqualTo("{\"gameProposalId\":\"" + gameProposalId + "\"}");
        }

        @Test
        void internalErrorOnException() throws IllegalAccessException, IllegalArgumentException,
        InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException, ExecutionException {
            when(lobbyService.createGameProposal(any(UUID.class))).thenThrow(new IllegalArgumentException());

            given()
                    .contentType(ContentType.JSON)
                    .body("{ \"creatorId\" : \"" + testPlayerId + "\" }")
                    .when()
                    .post(server.target("/lobby/create-game-proposal").getUri())
                    .then()
                    .assertThat()
                    .statusCode(equalTo(500));
        }

    }

    @Nested
    class CancelGameProposal {

        @Test
        void OKResponse()
                throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
                NoSuchMethodException, SecurityException, InterruptedException, ExecutionException, StreamReadException,
                DatabindException, IOException, UnsupportedStateTransitionException {
            UUID gameProposalId = UUID.randomUUID();

            given()
                    .contentType(ContentType.JSON)
                    .body("{ \"gameProposalId\" : \"" + gameProposalId + "\", \"creatorId\" : \"" + testPlayerId
                            + "\" }")
                    .when()
                    .post(server.target("/lobby/cancel-game-proposal").getUri())
                    .then()
                    .assertThat()
                    .statusCode(equalTo(200));

            verify(lobbyService).cancelGameProposal(gameProposalId, testPlayerId);
        }

        @Test
        void badRequestOnUnsupportedStateTransitionException() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException, StreamReadException, DatabindException, IOException,
                UnsupportedStateTransitionException {
            UUID gameProposalId = UUID.randomUUID();
            when(lobbyService.cancelGameProposal(any(UUID.class), any(UUID.class)))
                    .thenThrow(new UnsupportedStateTransitionException(GameProposalState.ACCEPTED,
                            GameProposalState.CANCELED));

            given()
                    .contentType(ContentType.JSON)
                    .body("{ \"gameProposalId\" : \"" + gameProposalId + "\", \"creatorId\" : \"" + testPlayerId
                            + "\" }")
                    .when()
                    .post(server.target("/lobby/cancel-game-proposal").getUri())
                    .then()
                    .assertThat()
                    .statusCode(equalTo(400));
        }

        @Test
        void internalErrorOnException() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException, StreamReadException, DatabindException, IOException,
                UnsupportedStateTransitionException {
            UUID gameProposalId = UUID.randomUUID();
            when(lobbyService.cancelGameProposal(any(UUID.class), any(UUID.class)))
                    .thenThrow(new IllegalArgumentException());

            given()
                    .contentType(ContentType.JSON)
                    .body("{ \"gameProposalId\" : \"" + gameProposalId + "\", \"creatorId\" : \"" + testPlayerId
                            + "\" }")
                    .when()
                    .post(server.target("/lobby/cancel-game-proposal").getUri())
                    .then()
                    .assertThat()
                    .statusCode(equalTo(500));
        }
    }

    @Nested
    class AcceptGameProposal {

        @Test
        void OKResponse()
                throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
                NoSuchMethodException, SecurityException, InterruptedException, ExecutionException, StreamReadException,
                DatabindException, IOException, UnsupportedStateTransitionException {
            UUID gameProposalId = UUID.randomUUID();

            given()
                    .contentType(ContentType.JSON)
                    .body("{ \"gameProposalId\" : \"" + gameProposalId + "\", \"acceptorId\" : \"" + testPlayerId
                            + "\" }")
                    .when()
                    .post(server.target("/lobby/accept-game-proposal").getUri())
                    .then()
                    .assertThat()
                    .statusCode(equalTo(200));

            verify(lobbyService).acceptGameProposal(gameProposalId, testPlayerId);
        }

        @Test
        void badRequestOnUnsupportedStateTransitionException() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException, StreamReadException, DatabindException, IOException,
                UnsupportedStateTransitionException {
            UUID gameProposalId = UUID.randomUUID();
            when(lobbyService.acceptGameProposal(any(UUID.class), any(UUID.class)))
                    .thenThrow(new UnsupportedStateTransitionException(GameProposalState.CANCELED,
                            GameProposalState.ACCEPTED));

            given()
                    .contentType(ContentType.JSON)
                    .body("{ \"gameProposalId\" : \"" + gameProposalId + "\", \"acceptorId\" : \"" + testPlayerId
                            + "\" }")
                    .when()
                    .post(server.target("/lobby/accept-game-proposal").getUri())
                    .then()
                    .assertThat()
                    .statusCode(equalTo(400));
        }

        @Test
        void internalErrorOnException() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException, StreamReadException, DatabindException, IOException,
                UnsupportedStateTransitionException {
            UUID gameProposalId = UUID.randomUUID();
            when(lobbyService.acceptGameProposal(any(UUID.class), any(UUID.class)))
                    .thenThrow(new IllegalArgumentException());

            given()
                    .contentType(ContentType.JSON)
                    .body("{ \"gameProposalId\" : \"" + gameProposalId + "\", \"acceptorId\" : \"" + testPlayerId
                            + "\" }")
                    .when()
                    .post(server.target("/lobby/accept-game-proposal").getUri())
                    .then()
                    .assertThat()
                    .statusCode(equalTo(500));
        }

    }

}
