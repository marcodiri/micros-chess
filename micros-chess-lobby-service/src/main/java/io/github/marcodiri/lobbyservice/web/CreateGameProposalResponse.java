package io.github.marcodiri.lobbyservice.web;

import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class CreateGameProposalResponse {

    private UUID gameProposalId;

    public CreateGameProposalResponse() {
    }

    public CreateGameProposalResponse(final UUID gameProposalId) {
        this.gameProposalId = gameProposalId;
    }

    public UUID getGameProposalId() {
        return gameProposalId;
    }

    public void setGameProposalId(final UUID creatorId) {
        this.gameProposalId = creatorId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
