package io.github.marcodiri.lobby_service.domain.command;

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
