package io.github.marcodiri.webservice.web;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import io.github.marcodiri.gameservice.api.web.CreateGameRequest;
import io.github.marcodiri.gameservice.api.web.CreateGameResponse;

public class WebService {

    private final WebClient httpClient;

    private static final Logger LOGGER = LogManager.getLogger(WebService.class);

    public WebService(final WebClient httpClient) {
        this.httpClient = httpClient;
    }

    public CreateGameResponse sendCreateGameRequest(UUID player1Id, UUID player2Id) {

        CreateGameRequest createGameRequest = new CreateGameRequest(player1Id, player2Id);

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

}
