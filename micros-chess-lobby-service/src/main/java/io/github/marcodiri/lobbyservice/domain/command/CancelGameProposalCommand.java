package io.github.marcodiri.lobbyservice.domain.command;

import java.util.Objects;
import java.util.UUID;

public class CancelGameProposalCommand extends GameProposalCommand {

    private final UUID creatorId;

    /**
     * @param creatorId the player who created the GameProposal.
     */
    public CancelGameProposalCommand(final UUID creatorId) {
        this.creatorId = creatorId;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CancelGameProposalCommand command = (CancelGameProposalCommand) o;
        return Objects.equals(creatorId, command.creatorId);
    }

}
