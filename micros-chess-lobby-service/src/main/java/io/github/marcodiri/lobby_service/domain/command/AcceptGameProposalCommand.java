package io.github.marcodiri.lobby_service.domain.command;

import java.util.UUID;

public class AcceptGameProposalCommand {

    private UUID acceptorId;

    public AcceptGameProposalCommand(UUID acceptorId) {
        this.acceptorId = acceptorId;
    }

    public UUID getAcceptorId() {
        return acceptorId;
    }

}
