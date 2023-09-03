package io.github.marcodiri.webservice.web;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import io.github.marcodiri.gameservice.api.event.GameCreated;
import io.github.marcodiri.lobbyservice.api.event.GameProposalCreated;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EventControllerTest {

    @LocalServerPort
    private Integer port;

    private WebSocketStompClient webSocketStompClient;

    @Autowired
    // private SimpMessagingTemplate simpMessagingTemplate;
    EventController controller;

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
        controller.notifyGameProposalCreated(testEvent);

        await()
                .atMost(10, SECONDS)
                .untilAsserted(() -> assertThat(blockingQueue1.poll()).isEqualTo(testEvent));
        await()
                .atMost(2, SECONDS)
                .untilAsserted(() -> assertThat(blockingQueue2.poll()).isEqualTo(testEvent));
    }

    @Test
    void verifyGameCreatedIsReceivedByClients() throws Exception {

        UUID player1Id = UUID.randomUUID();
        UUID player2Id = UUID.randomUUID();

        BlockingQueue<GameCreated> blockingQueue1 = new ArrayBlockingQueue<>(1);
        BlockingQueue<GameCreated> blockingQueue2 = new ArrayBlockingQueue<>(1);

        webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession session1 = webSocketStompClient
                .connectAsync(getWsPath(), new StompSessionHandlerAdapter() {
                })
                .get(1, SECONDS);
        StompSession session2 = webSocketStompClient
                .connectAsync(getWsPath(), new StompSessionHandlerAdapter() {
                })
                .get(1, SECONDS);
        session1.subscribe("/topic/player/" + player1Id, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return GameCreated.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue1.add((GameCreated) payload);
            }
        });
        session2.subscribe("/topic/player/" + player2Id, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return GameCreated.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue2.add((GameCreated) payload);
            }
        });

        UUID gameId = UUID.randomUUID();
        GameCreated testEvent = new GameCreated(gameId, player1Id, player2Id);
        controller.notifyGameCreated(testEvent);

        await()
                .atMost(2, SECONDS)
                .untilAsserted(() -> assertThat(blockingQueue1.peek()).isEqualTo(testEvent));
        await()
                .atMost(2, SECONDS)
                .untilAsserted(() -> assertThat(blockingQueue2.peek()).isEqualTo(testEvent));
    }

    private String getWsPath() {
        return String.format("ws://localhost:%d/ws", port);
    }

}
