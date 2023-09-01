package io.github.marcodiri.gameservice.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.marcodiri.gameservice.api.web.CreateGameRequest;
import io.github.marcodiri.gameservice.api.web.CreateGameResponse;
import io.github.marcodiri.gameservice.api.web.PlayMoveRequest;
import io.github.marcodiri.gameservice.domain.GameAggregate;
import io.github.marcodiri.gameservice.domain.GameNotInProgressException;
import io.github.marcodiri.gameservice.domain.GameService;
import io.github.marcodiri.gameservice.domain.IllegalMoveException;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/game")
public class GameController {

    private static final Logger LOGGER = LogManager.getLogger(GameController.class);

    @Inject
    private GameService gameService;

    @GET
    @Path("/ping")
    public Response ping() {
        System.out.println("called ping");
        return Response
                .ok()
                .entity("service is online")
                .build();
    }

    @POST
    @Path("/create-game")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGame(CreateGameRequest request) {
        LOGGER.info("Received CreateGameRequest: {}", request);

        try {
            GameAggregate game = gameService.createGame(request.getPlayer1Id(), request.getPlayer2Id());
            CreateGameResponse response = new CreateGameResponse(game.getId());
            LOGGER.info("Sending CreateGameResponse: {}", response);
            return Response
                    .ok()
                    .entity(response)
                    .build();
        } catch (Exception e) {
            LOGGER.catching(e);
        }

        return Response
                .serverError()
                .build();
    }

    @POST
    @Path("/play-move")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response playMove(PlayMoveRequest request) {
        LOGGER.info("Received CancelGameProposalRequest: {}", request);

        try {
            gameService.playMove(request.getGameId(), request.getPlayerId(), request.getMove());
            LOGGER.info("Sending OK");
            return Response
                    .ok()
                    .build();
        } catch (GameNotInProgressException | IllegalMoveException e) {
            LOGGER.catching(e);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .build();
        } catch (Exception e) {
            LOGGER.catching(e);
        }

        return Response
                .serverError()
                .build();
    }

}
