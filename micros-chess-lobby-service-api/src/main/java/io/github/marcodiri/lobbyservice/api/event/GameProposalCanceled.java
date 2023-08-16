package io.github.marcodiri.lobbyservice.api.event;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class GameProposalCanceled extends GameProposalEvent {

    private static final long serialVersionUID = 1L;

    private final GameProposalEventType type = GameProposalEventType.CANCELED;

    @JsonCreator
    public GameProposalCanceled(@JsonProperty("gameProposalId") final UUID gameProposalId) {
        super(gameProposalId);
    }

    @Override
    public GameProposalEventType getType() {
        return type;
    }

}
