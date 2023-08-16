package io.github.marcodiri.lobbyservice.api.event;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class GameProposalAccepted extends GameProposalEvent {

    private static final long serialVersionUID = 1L;

    private final GameProposalEventType type = GameProposalEventType.ACCEPTED;

    private final UUID creatorId;
    private final UUID acceptorId;

    @JsonCreator
    public GameProposalAccepted(
            @JsonProperty("gameProposalId") final UUID gameProposalId,
            @JsonProperty("creatorId") final UUID creatorId,
            @JsonProperty("acceptorId") final UUID acceptorId) {
        super(gameProposalId);
        this.creatorId = creatorId;
        this.acceptorId = acceptorId;
    }

    @Override
    public GameProposalEventType getType() {
        return type;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public UUID getAcceptorId() {
        return acceptorId;
    }

}
