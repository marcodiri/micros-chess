package io.github.marcodiri.lobby_service;

import java.util.UUID;

import io.github.marcodiri.lobby_service.domain.GameProposal;
import io.github.marcodiri.lobby_service.domain.command.CreateGameProposalCommand;
import io.github.marcodiri.lobby_service.repository.event_store.GameProposalESRepository;

public class LobbyService {

    private GameProposalESRepository gameProposalESRepository;

    public LobbyService(GameProposalESRepository gameProposalESRepository) {
        this.gameProposalESRepository = gameProposalESRepository;
    }

    public GameProposal createGameProposal(UUID playerId) {
        GameProposal gameProposal = gameProposalESRepository.save(new CreateGameProposalCommand(playerId));
        return gameProposal;
    }

}
