package io.github.marcodiri.lobbyservice.api.web;

import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CreateGameProposalRequest request = (CreateGameProposalRequest) o;
        return Objects.equals(creatorId, request.creatorId);
    }

}
