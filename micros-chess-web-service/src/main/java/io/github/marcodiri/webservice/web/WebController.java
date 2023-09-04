package io.github.marcodiri.webservice.web;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;

import io.github.marcodiri.gameservice.api.event.GameCreated;
import io.github.marcodiri.gameservice.api.event.MovePlayed;
import io.github.marcodiri.gameservice.api.web.CreateGameRequest;
import io.github.marcodiri.gameservice.api.web.CreateGameResponse;
import io.github.marcodiri.gameservice.api.web.PlayMoveRequest;
import io.github.marcodiri.lobbyservice.api.event.GameProposalAccepted;
import io.github.marcodiri.lobbyservice.api.event.GameProposalCreated;

@Controller
public class WebController {

    private WebClient httpClient;

    public void setHttpClient(WebClient httpClient) {
        this.httpClient = httpClient;
    }

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    private static final Logger LOGGER = LogManager.getLogger(WebController.class);

    public void notifyClients(GameProposalCreated event) {
        // FIXME: should not send back creatorId
        this.simpMessagingTemplate.convertAndSend("/topic/game-proposals", event);
    }

    public void notifyClients(GameCreated event) {
        // FIXME: should not send back players Id
        this.simpMessagingTemplate.convertAndSend("/topic/player/" + event.getPlayer1Id(), event);
        this.simpMessagingTemplate.convertAndSend("/topic/player/" + event.getPlayer2Id(), event);
    }

    public void notifyClients(MovePlayed event) {
        // FIXME: should not send back players Id
        this.simpMessagingTemplate.convertAndSend("/topic/game/" + event.getGameId(), event);
    }

    public CreateGameResponse sendCreateGameRequest(GameProposalAccepted event) {
        CreateGameRequest createGameRequest = new CreateGameRequest(
                event.getCreatorId(),
                event.getAcceptorId());

        LOGGER.info("POSTing to endpoint /game/create-game " + createGameRequest);
        CreateGameResponse response = httpClient.post()
                .uri("/game/create-game")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(createGameRequest)
                .retrieve()
                .bodyToMono(CreateGameResponse.class)
                .block();
        LOGGER.info("Received response " + response);
        return response;
    }

    @MessageMapping("/game/{gameId}/{playerId}")
    public void sendPlayMoveRequest(@DestinationVariable String gameId, @DestinationVariable String playerId, String move)
            throws IllegalArgumentException {
        UUID gameUuid = UUID.fromString(gameId);
        UUID playerUuid = UUID.fromString(playerId);
        PlayMoveRequest playMoveRequest = new PlayMoveRequest(gameUuid, playerUuid, move);

        LOGGER.info("POSTing to endpoint /game/play-move " + playMoveRequest);
        var response = httpClient.post()
                .uri("/game/play-move")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(playMoveRequest)
                .retrieve()
                .toBodilessEntity()
                .block();
        LOGGER.info("Received response " + response);
        // TODO: send confirmation to client based on response
    }

}
