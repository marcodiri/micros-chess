package io.github.marcodiri.lobbyservice.api.web;

import java.util.Objects;
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

    public void setGameProposalId(final UUID gameProposalId) {
        this.gameProposalId = gameProposalId;
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
        CreateGameProposalResponse request = (CreateGameProposalResponse) o;
        return Objects.equals(gameProposalId, request.gameProposalId);
    }

}
