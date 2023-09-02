package io.github.marcodiri.webservice.web;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import io.github.marcodiri.gameservice.api.web.CreateGameRequest;
import io.github.marcodiri.gameservice.api.web.CreateGameResponse;
import io.github.marcodiri.lobbyservice.api.event.GameProposalAccepted;
import io.github.marcodiri.lobbyservice.api.event.GameProposalCreated;
import io.github.marcodiri.rest.InMemoryRestServer;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
public class WebServiceTest {

    @Mock
    EventController controller;

    private static WebClient httpClient;
    private WebService webService;

    private static InMemoryRestServer server;

    private static UUID gameId = UUID.randomUUID();

    @Path("/game")
    public static class MyResource {

        @POST
        @Path("/create-game")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response method(CreateGameRequest request) {
            CreateGameResponse response = new CreateGameResponse(gameId);
            return Response
                    .ok()
                    .entity(response)
                    .build();
        }

    }

    @Spy
    MyResource myResource = new MyResource();

    @BeforeEach
    void setup() throws IOException {
        server = InMemoryRestServer.create(myResource);
        httpClient = WebClient.create(server.target().getUri().toString());
        webService = new WebService(httpClient, controller);
    }

    @AfterEach
    void closeServer() {
        server.close();
    }

    @Test
    void sendCreateGameRequest() {
        UUID player1Id = UUID.randomUUID();
        UUID player2Id = UUID.randomUUID();
        GameProposalAccepted event = new GameProposalAccepted(UUID.randomUUID(), player1Id, player2Id);
        CreateGameRequest expectedRequest = new CreateGameRequest(player1Id, player2Id);

        CreateGameResponse response = webService.sendCreateGameRequest(event);

        await().atMost(2, SECONDS).untilAsserted(() -> verify(myResource).method(expectedRequest));
        assertThat(response).isInstanceOf(CreateGameResponse.class);
    }

    @Test
    void notifyClients() {
        UUID gameProposalId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        GameProposalCreated event = new GameProposalCreated(gameProposalId, creatorId);

        webService.notifyClients(event);

        verify(controller).notifyGameProposalCreated(event);
    }

}
