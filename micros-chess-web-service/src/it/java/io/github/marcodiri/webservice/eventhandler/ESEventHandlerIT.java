package io.github.marcodiri.webservice.eventhandler;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.EventStoreDBClientSettings;
import com.eventstore.dbclient.EventStoreDBConnectionString;

import io.github.marcodiri.gameservice.api.web.CreateGameRequest;
import io.github.marcodiri.gameservice.api.web.CreateGameResponse;
import io.github.marcodiri.lobbyservice.api.event.GameProposalAccepted;
import io.github.marcodiri.rest.InMemoryRestServer;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
public class ESEventHandlerIT {

    ESEventHandler eventHandler;

    private static EventStoreDBClientSettings setts;
    private static EventStoreDBClient readerClient, writerClient;
    private static WebClient httpClient;

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

    @BeforeAll
    static void setupClient() {
        setts = EventStoreDBConnectionString.parseOrThrow("esdb://localhost:2113?tls=false");
        readerClient = EventStoreDBClient.create(setts);
        writerClient = EventStoreDBClient.create(setts);
    }

    @AfterAll
    static void teardownClient() throws ExecutionException, InterruptedException {
        writerClient.shutdown();
        readerClient.shutdown();
    }

    @BeforeEach
    void setup() throws IOException {
        server = InMemoryRestServer.create(myResource);
        httpClient = WebClient.create(server.target().getUri().toString());
        eventHandler = new ESEventHandler(readerClient, httpClient);
    }

    @AfterEach
    void closeServer() {
        server.close();
    }

    @Test
    void listenerRequestsCreateGameOnGameProposalAcceptedEvents() throws Exception {
        UUID gameProposalId = UUID.randomUUID();
        UUID player1Id = UUID.randomUUID();
        UUID player2Id = UUID.randomUUID();
        CreateGameRequest expectedRequest = new CreateGameRequest(player1Id, player2Id);
        GameProposalAccepted event = new GameProposalAccepted(gameProposalId, player1Id, player2Id);
        EventData eventData = EventData
                .builderAsJson(event.getType().toString(), event)
                .build();
        String streamName = String.format("Test_%s", gameProposalId);

        writerClient
                .appendToStream(streamName, eventData)
                .get();

        await().atMost(2, SECONDS).untilAsserted(() -> verify(myResource).method(expectedRequest));
    }

}
