package io.github.marcodiri.gameservice.eventhandler;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

import java.lang.reflect.InvocationTargetException;
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

import io.github.marcodiri.gameservice.domain.GameService;
import io.github.marcodiri.lobbyservice.api.event.GameProposalAccepted;

@ExtendWith(MockitoExtension.class)
public class GameESListenerIT {

    @Mock
    GameService gameService;

    GameESEventHandler gameESListener;

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
        readerClient.shutdown();
        writerClient.shutdown();
    }

    @BeforeEach
    void setupListener() {
        gameESListener = new GameESEventHandler(readerClient, gameService);
    }

    @Test
    void listenerCallsCreateGameOnGameProposalAcceptedEvents()
            throws InterruptedException, ExecutionException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
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

        await().atMost(2, SECONDS).untilAsserted(() -> verify(gameService).createGame(player1Id, player2Id));
    }

}