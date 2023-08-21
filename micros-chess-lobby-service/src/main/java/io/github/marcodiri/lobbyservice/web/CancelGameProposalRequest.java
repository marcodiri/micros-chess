package io.github.marcodiri.lobbyservice.web;

import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class CancelGameProposalRequest {

    private UUID gameProposalId;

    private UUID creatorId;

    public CancelGameProposalRequest() {
    }

    public CancelGameProposalRequest(final UUID gameProposalId, final UUID creatorId) {
        this.gameProposalId = gameProposalId;
        this.creatorId = creatorId;
    }

    public UUID getGameProposalId() {
        return gameProposalId;
    }

    public void setGameProposalId(final UUID gameProposalId) {
        this.gameProposalId = gameProposalId;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(final UUID creatorId) {
        this.creatorId = creatorId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
