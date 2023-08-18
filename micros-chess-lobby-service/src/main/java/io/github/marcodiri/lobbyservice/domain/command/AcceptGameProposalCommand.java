package io.github.marcodiri.lobbyservice.domain.command;

import java.util.UUID;

public class AcceptGameProposalCommand extends GameProposalCommand {

    private UUID acceptorId;

    /**
     * @param acceptorId the player who accepted the GameProposal.
     */
    public AcceptGameProposalCommand(UUID acceptorId) {
        this.acceptorId = acceptorId;
    }

    public UUID getAcceptorId() {
        return acceptorId;
    }

}
