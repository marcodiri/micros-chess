package io.github.marcodiri.lobbyservice.domain.command;

import java.util.UUID;

public class CreateGameProposalCommand extends GameProposalCommand {

    private final UUID creatorId;

    /**
     * @param creatorId the player who requested the creation of the GameProposal.
     */
    public CreateGameProposalCommand(final UUID creatorId) {
        this.creatorId = creatorId;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

}
