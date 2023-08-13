package io.github.marcodiri.lobbyservice.api.event;

import io.github.marcodiri.core.domain.event.DomainEventType;

public enum GameProposalEventType implements DomainEventType {

    CREATED("game-proposal-created"),
    CANCELED("game-proposal-canceled"),
    ACCEPTED("game-proposal-accepted");

    private final String type;

    GameProposalEventType(final String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

}
