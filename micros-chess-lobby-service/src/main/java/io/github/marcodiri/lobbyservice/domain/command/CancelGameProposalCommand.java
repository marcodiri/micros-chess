package io.github.marcodiri.lobbyservice.domain.command;

import java.util.UUID;

public class CancelGameProposalCommand {

    private final UUID creatorId;

    /**
     * @param creatorId the player who created the GameProposal
     */
    public CancelGameProposalCommand(final UUID creatorId) {
        this.creatorId = creatorId;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

}
