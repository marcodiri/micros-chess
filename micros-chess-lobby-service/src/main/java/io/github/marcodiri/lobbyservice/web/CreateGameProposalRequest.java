package io.github.marcodiri.lobbyservice.web;

import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class CreateGameProposalRequest {

    private UUID creatorId;

    public CreateGameProposalRequest() {
    }

    public CreateGameProposalRequest(final UUID creatorId) {
        this.creatorId = creatorId;
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
