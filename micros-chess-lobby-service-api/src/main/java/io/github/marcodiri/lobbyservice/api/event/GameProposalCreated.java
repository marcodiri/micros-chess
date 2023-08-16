package io.github.marcodiri.lobbyservice.api.event;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class GameProposalCreated extends GameProposalEvent {

    private static final long serialVersionUID = 1L;

    private final GameProposalEventType type = GameProposalEventType.CREATED;

    private final UUID creatorId;

    @JsonCreator
    public GameProposalCreated(
            @JsonProperty("gameProposalId") final UUID gameProposalId,
            @JsonProperty("creatorId") final UUID creatorId) {
        super(gameProposalId);
        this.creatorId = creatorId;
    }

    @Override
    public GameProposalEventType getType() {
        return type;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GameProposalCreated event = (GameProposalCreated) o;
        return super.equals(o)
                && Objects.equals(creatorId, event.creatorId);
    }

}
