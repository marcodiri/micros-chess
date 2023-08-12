package io.github.marcodiri.lobby_service.domain.command;

import java.util.UUID;

public class CreateGameProposalCommand {

    private UUID creatorId;

    public CreateGameProposalCommand(UUID creatorId) {
        this.creatorId = creatorId;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

}
