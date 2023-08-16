package io.github.marcodiri.lobbyservice.domain.command;

import java.util.UUID;

public class CreateGameProposalCommand {

    private final UUID creatorId;

    public CreateGameProposalCommand(final UUID creatorId) {
        this.creatorId = creatorId;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

}
