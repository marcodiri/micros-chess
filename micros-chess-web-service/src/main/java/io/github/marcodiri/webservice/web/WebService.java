package io.github.marcodiri.webservice.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import io.github.marcodiri.gameservice.api.event.GameCreated;
import io.github.marcodiri.gameservice.api.event.MovePlayed;
import io.github.marcodiri.gameservice.api.web.CreateGameRequest;
import io.github.marcodiri.gameservice.api.web.CreateGameResponse;
import io.github.marcodiri.lobbyservice.api.event.GameProposalAccepted;
import io.github.marcodiri.lobbyservice.api.event.GameProposalCreated;

public class WebService {

    private final EventController controller;
    private final WebClient httpClient;

    private static final Logger LOGGER = LogManager.getLogger(WebService.class);

    public WebService(final WebClient httpClient, final EventController controller) {
        this.httpClient = httpClient;
        this.controller = controller;
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

    public void notifyClients(GameProposalCreated event) {
        controller.notifyGameProposalCreated(event);
    }

    public void notifyClients(GameCreated event) {
        controller.notifyGameCreated(event);
    }

    public void notifyClients(MovePlayed event) {
        controller.notifyMovePlayed(event);
    }

}
