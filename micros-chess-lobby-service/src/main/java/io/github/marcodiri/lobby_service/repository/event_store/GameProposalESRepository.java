package io.github.marcodiri.lobby_service.repository.event_store;

import java.util.UUID;

import io.github.marcodiri.lobby_service.domain.GameProposal;
import io.github.marcodiri.lobby_service.domain.command.AcceptGameProposalCommand;
import io.github.marcodiri.lobby_service.domain.command.CancelGameProposalCommand;
import io.github.marcodiri.lobby_service.domain.command.CreateGameProposalCommand;

public class GameProposalESRepository {

    public GameProposal save(CreateGameProposalCommand cmd) {
        return null;
    }

    public GameProposal update(UUID gameProposalId, CancelGameProposalCommand cmd) {
        return null;
    }

    public GameProposal update(UUID gameProposalId, AcceptGameProposalCommand cmd) {
        return null;
    }

}
