package io.github.marcodiri.lobby_service.domain.command;

import java.util.UUID;

public class CreateGameProposalCommand {

    private UUID playerId;

    public CreateGameProposalCommand(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

}
