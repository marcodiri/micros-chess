package io.github.marcodiri.lobbyservice.web;

import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class AcceptGameProposalRequest {

    private UUID gameProposalId;

    private UUID acceptorId;

    public AcceptGameProposalRequest() {
    }

    public AcceptGameProposalRequest(final UUID gameProposalId, final UUID acceptorId) {
        this.gameProposalId = gameProposalId;
        this.acceptorId = acceptorId;
    }

    public UUID getGameProposalId() {
        return gameProposalId;
    }

    public void setGameProposalId(final UUID gameProposalId) {
        this.gameProposalId = gameProposalId;
    }

    public UUID getAcceptorId() {
        return acceptorId;
    }

    public void setAcceptorId(final UUID acceptorId) {
        this.acceptorId = acceptorId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
