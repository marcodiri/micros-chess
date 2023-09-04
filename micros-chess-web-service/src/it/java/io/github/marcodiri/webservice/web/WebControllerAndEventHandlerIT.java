package io.github.marcodiri.webservice.web;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.EventStoreDBClientSettings;
import com.eventstore.dbclient.EventStoreDBConnectionString;

import io.github.marcodiri.lobbyservice.api.event.GameProposalCreated;

public class WebControllerAndEventHandlerIT {

    private WebSocketStompClient webSocketStompClient;

    private static EventStoreDBClientSettings setts;
    private static EventStoreDBClient writerClient;

    @BeforeAll
    static void setupClient() {
        setts = EventStoreDBConnectionString.parseOrThrow("esdb://localhost:2113?tls=false");
        writerClient = EventStoreDBClient.create(setts);
    }

    @AfterAll
    static void teardownClient() throws ExecutionException, InterruptedException {
        writerClient.shutdown();
    }

    @BeforeEach
    void setup() throws Exception {
        this.webSocketStompClient = new WebSocketStompClient(new SockJsClient(
                List.of(new WebSocketTransport(new StandardWebSocketClient()))));
    }

    @Test
    void verifyGameProposalsCreatedIsReceivedByClients() throws Exception {

        BlockingQueue<GameProposalCreated> blockingQueue1 = new ArrayBlockingQueue<>(1);
        BlockingQueue<GameProposalCreated> blockingQueue2 = new ArrayBlockingQueue<>(1);

        webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession session1 = webSocketStompClient
                .connectAsync(getWsPath(), new StompSessionHandlerAdapter() {
                })
                .get(1, SECONDS);
        StompSession session2 = webSocketStompClient
                .connectAsync(getWsPath(), new StompSessionHandlerAdapter() {
                })
                .get(1, SECONDS);
        session1.subscribe("/topic/game-proposals", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return GameProposalCreated.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue1.add((GameProposalCreated) payload);
            }
        });
        session2.subscribe("/topic/game-proposals", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return GameProposalCreated.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue2.add((GameProposalCreated) payload);
            }
        });

        UUID gameProposalId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        GameProposalCreated testEvent = new GameProposalCreated(gameProposalId, creatorId);
        EventData eventData = EventData
                .builderAsJson(testEvent.getType().toString(), testEvent)
                .build();
        String streamName = String.format("Test_%s", gameProposalId);

        writerClient
                .appendToStream(streamName, eventData)
                .get();

        assertThat(blockingQueue1.poll(2, SECONDS)).isEqualTo(testEvent);
        assertThat(blockingQueue2.poll(2, SECONDS)).isEqualTo(testEvent);
    }

    private String getWsPath() {
        return "ws://localhost:8080/ws";
    }

}
