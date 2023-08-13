package io.github.marcodiri.lobbyservice.domain.command;

import java.util.UUID;

public class CancelGameProposalCommand {

    private UUID creatorId;

    public CancelGameProposalCommand(UUID creatorId) {
        this.creatorId = creatorId;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

}
