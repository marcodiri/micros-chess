package io.github.marcodiri.lobby_service;

import java.util.UUID;

import io.github.marcodiri.lobby_service.domain.GameProposal;
import io.github.marcodiri.lobby_service.domain.command.AcceptGameProposalCommand;
import io.github.marcodiri.lobby_service.domain.command.CancelGameProposalCommand;
import io.github.marcodiri.lobby_service.domain.command.CreateGameProposalCommand;
import io.github.marcodiri.lobby_service.repository.event_store.GameProposalESRepository;

public class LobbyService {

    private GameProposalESRepository gameProposalESRepository;

    public LobbyService(GameProposalESRepository gameProposalESRepository) {
        this.gameProposalESRepository = gameProposalESRepository;
    }

    public GameProposal createGameProposal(UUID creatorId) {
        GameProposal gameProposal = gameProposalESRepository.save(new CreateGameProposalCommand(creatorId));
        return gameProposal;
    }

    public GameProposal cancelGameProposal(UUID gameProposalId, UUID creatorId) {
        GameProposal gameProposal = gameProposalESRepository
                .update(gameProposalId, new CancelGameProposalCommand(creatorId));
        return gameProposal;
    }

    public GameProposal acceptGameProposal(UUID gameProposalId, UUID acceptorId) {
        GameProposal gameProposal = gameProposalESRepository
                .update(gameProposalId, new AcceptGameProposalCommand(acceptorId));
        return gameProposal;
    }

}
