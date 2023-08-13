package io.github.marcodiri.lobbyservice.domain.event;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.marcodiri.lobbyservice.api.event.GameProposalEventType;

public final class GameProposalCreated extends GameProposalEvent {

    private static final long serialVersionUID = 1L;

    private final GameProposalEventType type = GameProposalEventType.CREATED;

    @JsonCreator
    public GameProposalCreated(@JsonProperty("gameProposalId") final UUID gameProposalId) {
        super(gameProposalId);
    }

    @Override
    public GameProposalEventType getType() {
        return type;
    }

}
