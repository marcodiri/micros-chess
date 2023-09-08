package io.github.marcodiri.lobbyservice.api.web;

import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CancelGameProposalRequest request = (CancelGameProposalRequest) o;
        return Objects.equals(gameProposalId, request.gameProposalId)
                && Objects.equals(creatorId, request.creatorId);
    }

}
