package io.github.marcodiri.webservice.web;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
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
import io.github.marcodiri.gameservice.api.event.MovePlayed;
import io.github.marcodiri.gameservice.api.web.CreateGameRequest;
import io.github.marcodiri.gameservice.api.web.CreateGameResponse;
import io.github.marcodiri.gameservice.api.web.PlayMoveRequest;
import io.github.marcodiri.lobbyservice.api.event.GameProposalAccepted;
import io.github.marcodiri.lobbyservice.api.event.GameProposalCreated;
import io.github.marcodiri.rest.InMemoryRestServer;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebControllerTest {

    @LocalServerPort
    private Integer port;

    private WebSocketStompClient webSocketStompClient;

    @Autowired
    WebController controller;

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
        controller.notifyClients(testEvent);

        assertThat(blockingQueue1.poll(5, SECONDS)).isEqualTo(testEvent);
        assertThat(blockingQueue2.poll(5, SECONDS)).isEqualTo(testEvent);
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
        controller.notifyClients(testEvent);

        assertThat(blockingQueue1.poll(5, SECONDS)).isEqualTo(testEvent);
        assertThat(blockingQueue2.poll(5, SECONDS)).isEqualTo(testEvent);
    }

    @Test
    void verifyMovePlayedIsReceivedByClients() throws Exception {

        UUID gameId = UUID.randomUUID();

        BlockingQueue<MovePlayed> blockingQueue1 = new ArrayBlockingQueue<>(1);
        BlockingQueue<MovePlayed> blockingQueue2 = new ArrayBlockingQueue<>(1);

        webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession session1 = webSocketStompClient
                .connectAsync(getWsPath(), new StompSessionHandlerAdapter() {
                })
                .get(1, SECONDS);
        StompSession session2 = webSocketStompClient
                .connectAsync(getWsPath(), new StompSessionHandlerAdapter() {
                })
                .get(1, SECONDS);
        session1.subscribe("/topic/game/" + gameId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MovePlayed.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue1.add((MovePlayed) payload);
            }
        });
        session2.subscribe("/topic/game/" + gameId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MovePlayed.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue2.add((MovePlayed) payload);
            }
        });

        UUID playerId = UUID.randomUUID();
        String move = "e4";
        MovePlayed testEvent = new MovePlayed(gameId, playerId, move);
        controller.notifyClients(testEvent);

        assertThat(blockingQueue1.poll(10, SECONDS)).isEqualTo(testEvent);
        assertThat(blockingQueue2.poll(10, SECONDS)).isEqualTo(testEvent);
    }

    @Nested
    class RESTRequests {

        private static InMemoryRestServer server;
        // private static WebClient httpClient;

        private static UUID gameId = UUID.randomUUID();

        @Path("/game")
        public static class MyResource {
            @POST
            @Path("/create-game")
            @Consumes(MediaType.APPLICATION_JSON)
            @Produces(MediaType.APPLICATION_JSON)
            public Response createGame(CreateGameRequest request) {
                CreateGameResponse response = new CreateGameResponse(gameId);
                return Response
                        .ok()
                        .entity(response)
                        .build();
            }

            @POST
            @Path("/play-move")
            @Consumes(MediaType.APPLICATION_JSON)
            @Produces(MediaType.APPLICATION_JSON)
            public Response playMove(PlayMoveRequest request) {
                return Response
                        .ok()
                        .build();
            }
        }

        @Spy
        MyResource myResource = new MyResource();

        @BeforeEach
        void setup() throws IOException {
            server = InMemoryRestServer.create(myResource);
            // httpClient = WebClient.create(server.target().getUri().toString());
            controller.setGameServiceBaseUri(server.target().getUri());
        }

        @AfterEach
        void closeServer() {
            server.close();
        }

        @Test
        void sendCreateGameRequest() throws Exception {
            UUID player1Id = UUID.randomUUID();
            UUID player2Id = UUID.randomUUID();
            GameProposalAccepted event = new GameProposalAccepted(UUID.randomUUID(), player1Id, player2Id);
            CreateGameRequest expectedRequest = new CreateGameRequest(player1Id, player2Id);

            CreateGameResponse response = controller.sendCreateGameRequest(event);

            await().atMost(5, SECONDS).untilAsserted(() -> verify(myResource).createGame(expectedRequest));
            assertThat(response).isInstanceOf(CreateGameResponse.class);
        }

        @Test
        void verifyPlayMoveEndpoint() throws Exception {
            UUID gameId = UUID.randomUUID();
            UUID playerId = UUID.randomUUID();
            String move = "e4";
            PlayMoveRequest expectedRequest = new PlayMoveRequest(gameId, playerId, move);

            webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());
            StompSession session = webSocketStompClient
                    .connectAsync(getWsPath(), new StompSessionHandlerAdapter() {
                    })
                    .get(1, SECONDS);

            session.send("/app/game/" + gameId + "/" + playerId, "e4");
            // controller.sendPlayMoveRequest(gameId.toString(), playerId.toString(), move);

            await().atMost(5, SECONDS).untilAsserted(() -> verify(myResource).playMove(expectedRequest));
        }

    }

    private String getWsPath() {
        return String.format("ws://localhost:%d/ws", port);
    }

}
