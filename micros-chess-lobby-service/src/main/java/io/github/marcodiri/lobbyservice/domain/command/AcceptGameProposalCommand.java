package io.github.marcodiri.lobbyservice.domain.command;

import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AcceptGameProposalCommand command = (AcceptGameProposalCommand) o;
        return Objects.equals(acceptorId, command.acceptorId);
    }

}
