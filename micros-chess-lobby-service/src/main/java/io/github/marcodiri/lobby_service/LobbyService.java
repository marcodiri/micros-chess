package io.github.marcodiri.lobby_service;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.marcodiri.lobby_service.domain.GameProposal;
import io.github.marcodiri.lobby_service.domain.command.AcceptGameProposalCommand;
import io.github.marcodiri.lobby_service.domain.command.CancelGameProposalCommand;
import io.github.marcodiri.lobby_service.domain.command.CreateGameProposalCommand;
import io.github.marcodiri.lobby_service.repository.event_store.GameProposalESRepository;

public class LobbyService {

    private GameProposalESRepository gameProposalESRepository;

	private static final Logger LOGGER = LogManager.getLogger(LobbyService.class);

    public LobbyService(GameProposalESRepository gameProposalESRepository) {
        this.gameProposalESRepository = gameProposalESRepository;
    }

    public GameProposal createGameProposal(UUID creatorId) {
        GameProposal gameProposal = gameProposalESRepository.save(new CreateGameProposalCommand(creatorId));
        LOGGER.info("Created: {}", gameProposal);
        return gameProposal;
    }

    public GameProposal cancelGameProposal(UUID gameProposalId, UUID creatorId) {
        GameProposal gameProposal = gameProposalESRepository
                .update(gameProposalId, new CancelGameProposalCommand(creatorId));
        LOGGER.info("Canceled: {}", gameProposal);
        return gameProposal;
    }

    public GameProposal acceptGameProposal(UUID gameProposalId, UUID acceptorId) {
        GameProposal gameProposal = gameProposalESRepository
                .update(gameProposalId, new AcceptGameProposalCommand(acceptorId));
        LOGGER.info("Accepted: {}", gameProposal);
        return gameProposal;
    }

}
