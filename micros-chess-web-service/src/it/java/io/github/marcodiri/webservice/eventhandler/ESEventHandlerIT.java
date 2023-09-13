package io.github.marcodiri.webservice.eventhandler;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.EventStoreDBClientSettings;
import com.eventstore.dbclient.EventStoreDBConnectionString;

import io.github.marcodiri.gameservice.api.event.GameCreated;
import io.github.marcodiri.gameservice.api.event.MovePlayed;
import io.github.marcodiri.gameservice.api.web.Move;
import io.github.marcodiri.lobbyservice.api.event.GameProposalAccepted;
import io.github.marcodiri.lobbyservice.api.event.GameProposalCreated;
import io.github.marcodiri.webservice.web.WebController;

@ExtendWith(MockitoExtension.class)
public class ESEventHandlerIT {

    @Mock
    WebController webController;

    ESEventHandler eventHandler;

    private static EventStoreDBClientSettings setts;
    private static EventStoreDBClient readerClient, writerClient;

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
        eventHandler = new ESEventHandler(readerClient, webController);
    }

    @Test
    void listenerCallsWebServiceOnGameProposalAcceptedEvents() throws Exception {
        UUID gameProposalId = UUID.randomUUID();
        UUID player1Id = UUID.randomUUID();
        UUID player2Id = UUID.randomUUID();
        GameProposalAccepted event = new GameProposalAccepted(gameProposalId, player1Id, player2Id);
        EventData eventData = EventData
                .builderAsJson(event.getType().toString(), event)
                .build();
        String streamName = String.format("Test_%s", gameProposalId);

        writerClient
                .appendToStream(streamName, eventData)
                .get();

        await().atMost(2, SECONDS).untilAsserted(() -> verify(webController).sendCreateGameRequest(event));
    }

    @Test
    void listenerCallsWebServiceOnGameProposalCreatedEvents() throws Exception {
        UUID gameProposalId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        GameProposalCreated event = new GameProposalCreated(gameProposalId, creatorId);
        EventData eventData = EventData
                .builderAsJson(event.getType().toString(), event)
                .build();
        String streamName = String.format("Test_%s", gameProposalId);

        writerClient
                .appendToStream(streamName, eventData)
                .get();

        await().atMost(2, SECONDS).untilAsserted(() -> verify(webController).notifyClients(event));
    }

    @Test
    void listenerCallsWebServiceOnGameCreatedEvents() throws Exception {
        UUID gameId = UUID.randomUUID();
        UUID player1Id = UUID.randomUUID();
        UUID player2Id = UUID.randomUUID();
        GameCreated event = new GameCreated(gameId, player1Id, player2Id);
        EventData eventData = EventData
                .builderAsJson(event.getType().toString(), event)
                .build();
        String streamName = String.format("Test_%s", gameId);

        writerClient
                .appendToStream(streamName, eventData)
                .get();

        await().atMost(2, SECONDS).untilAsserted(() -> verify(webController).notifyClients(event));
    }

    @Test
    void listenerCallsWebServiceOnMovePlayedEvents() throws Exception {
        UUID gameId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        Move move = new Move("e2", "e4");
        MovePlayed event = new MovePlayed(gameId, playerId, move);
        EventData eventData = EventData
                .builderAsJson(event.getType().toString(), event)
                .build();
        String streamName = String.format("Test_%s", gameId);

        writerClient
                .appendToStream(streamName, eventData)
                .get();

        await().atMost(2, SECONDS).untilAsserted(() -> verify(webController).notifyClients(event));
    }

}
