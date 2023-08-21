package io.github.marcodiri.lobbyservice.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.marcodiri.lobbyservice.domain.GameProposalAggregate;
import io.github.marcodiri.lobbyservice.domain.LobbyService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/lobby")
public class LobbyController {

    private static final Logger LOGGER = LogManager.getLogger(LobbyController.class);

    @Inject
    private LobbyService lobbyService;

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
    @Path("/create-game-proposal")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGameProposal(CreateGameProposalRequest request) {
        LOGGER.info("Received CreateGameProposalRequest: {}", request);

        try {
            GameProposalAggregate gameProposal = lobbyService.createGameProposal(request.getCreatorId());
            CreateGameProposalResponse response = new CreateGameProposalResponse(gameProposal.getId());
            LOGGER.info("Sending CreateGameProposalResponse: {}", response);
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

}
