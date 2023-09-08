package io.github.marcodiri.webservice.web;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.marcodiri.gameservice.api.event.GameCreated;
import io.github.marcodiri.gameservice.api.event.MovePlayed;
import io.github.marcodiri.gameservice.api.web.CreateGameRequest;
import io.github.marcodiri.gameservice.api.web.CreateGameResponse;
import io.github.marcodiri.gameservice.api.web.PlayMoveRequest;
import io.github.marcodiri.lobbyservice.api.event.GameProposalAccepted;
import io.github.marcodiri.lobbyservice.api.event.GameProposalCreated;
import io.github.marcodiri.lobbyservice.api.web.AcceptGameProposalRequest;
import io.github.marcodiri.lobbyservice.api.web.CreateGameProposalRequest;
import io.github.marcodiri.lobbyservice.api.web.CreateGameProposalResponse;

@Controller
public class WebController {

    private URI lobbyServiceBaseUri;
    private URI gameServiceBaseUri;

    @Autowired
    public void setLobbyServiceBaseUri(URI lobbyServiceBaseUri) {
        this.lobbyServiceBaseUri = lobbyServiceBaseUri;
    }

    @Autowired
    public void setGameServiceBaseUri(URI gameServiceBaseUri) {
        this.gameServiceBaseUri = gameServiceBaseUri;
    }

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    private static final Logger LOGGER = LogManager.getLogger(WebController.class);

    public void notifyClients(GameProposalCreated event) {
        // FIXME: should not send back creatorId
        LOGGER.info("Sending websocket message: " + event);
        this.simpMessagingTemplate.convertAndSend("/topic/game-proposals", event);
    }

    public void notifyClients(GameCreated event) {
        // FIXME: should not send back players Id
        LOGGER.info("Sending websocket message: " + event);
        this.simpMessagingTemplate.convertAndSend("/topic/player/" + event.getPlayer1Id(), event);
        this.simpMessagingTemplate.convertAndSend("/topic/player/" + event.getPlayer2Id(), event);
    }

    public void notifyClients(MovePlayed event) {
        // FIXME: should not send back players Id
        LOGGER.info("Sending websocket message: " + event);
        this.simpMessagingTemplate.convertAndSend("/topic/game/" + event.getGameId(), event);
    }

    @MessageMapping("/create-game-proposal")
    public CreateGameProposalResponse sendCreateGameProposalRequest(String playerId)
            throws ClientProtocolException, IOException {
        UUID playerUuid = UUID.fromString(playerId);
        CreateGameProposalRequest createGameProposalRequest = new CreateGameProposalRequest(playerUuid);

        ObjectMapper mapper = new ObjectMapper();

        String uri = lobbyServiceBaseUri.toString() + "/lobby/create-game-proposal";
        LOGGER.info("POSTing to endpoint {} {}", uri, createGameProposalRequest);
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");

            StringEntity entity = new StringEntity(mapper.writeValueAsString(createGameProposalRequest));
            entity.setContentType(ContentType.APPLICATION_JSON.getMimeType());

            request.setEntity(entity);

            CreateGameProposalResponse response = client.execute(request,
                    httpResponse -> mapper.readValue(httpResponse.getEntity().getContent(),
                            CreateGameProposalResponse.class));

            LOGGER.info("Received response " + response);
            return response;
        }
    }

    @MessageMapping("/accept-game-proposal/{gameProposalId}")
    public void sendCreateGameProposalRequest(
            @DestinationVariable String gameProposalId,
            String playerId) throws IOException {
        UUID gameProposalUuid = UUID.fromString(gameProposalId);
        UUID playerUuid = UUID.fromString(playerId);
        AcceptGameProposalRequest acceptGameProposalRequest = new AcceptGameProposalRequest(gameProposalUuid,
                playerUuid);

        ObjectMapper mapper = new ObjectMapper();

        String uri = lobbyServiceBaseUri.toString() + "/lobby/accept-game-proposal";
        LOGGER.info("POSTing to endpoint {} {}", uri, acceptGameProposalRequest);
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");

            StringEntity entity = new StringEntity(mapper.writeValueAsString(acceptGameProposalRequest));
            entity.setContentType(ContentType.APPLICATION_JSON.getMimeType());

            request.setEntity(entity);

            HttpResponse response = client.execute(request);

            LOGGER.info("Received response " + response);
        }
    }

    public CreateGameResponse sendCreateGameRequest(GameProposalAccepted event)
            throws ClientProtocolException, IOException {
        CreateGameRequest createGameRequest = new CreateGameRequest(
                event.getCreatorId(),
                event.getAcceptorId());

        ObjectMapper mapper = new ObjectMapper();

        String uri = gameServiceBaseUri.toString() + "/game/create-game";
        LOGGER.info("POSTing to endpoint {} {}", uri, createGameRequest);
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");

            StringEntity entity = new StringEntity(mapper.writeValueAsString(createGameRequest));
            entity.setContentType(ContentType.APPLICATION_JSON.getMimeType());

            request.setEntity(entity);

            CreateGameResponse response = client.execute(request,
                    httpResponse -> mapper.readValue(httpResponse.getEntity().getContent(), CreateGameResponse.class));

            LOGGER.info("Received response " + response);
            return response;
        }
    }

    @MessageMapping("/game/{gameId}/{playerId}")
    public void sendPlayMoveRequest(@DestinationVariable String gameId, @DestinationVariable String playerId,
            String move)
            throws IllegalArgumentException, ClientProtocolException, IOException {
        UUID gameUuid = UUID.fromString(gameId);
        UUID playerUuid = UUID.fromString(playerId);
        PlayMoveRequest playMoveRequest = new PlayMoveRequest(gameUuid, playerUuid, move);

        ObjectMapper mapper = new ObjectMapper();

        String uri = gameServiceBaseUri.toString() + "/game/play-move";
        LOGGER.info("POSTing to endpoint {} {}", uri, playMoveRequest);
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");

            StringEntity entity = new StringEntity(mapper.writeValueAsString(playMoveRequest));
            entity.setContentType(ContentType.APPLICATION_JSON.getMimeType());

            request.setEntity(entity);

            HttpResponse response = client.execute(request);

            LOGGER.info("Received response " + response);
        }
    }

}
