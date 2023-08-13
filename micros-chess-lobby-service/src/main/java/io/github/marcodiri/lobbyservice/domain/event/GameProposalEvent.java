package io.github.marcodiri.lobbyservice.domain.event;

import java.util.Objects;
import java.util.UUID;

import io.github.marcodiri.core.domain.event.DomainEvent;
import io.github.marcodiri.lobbyservice.api.event.GameProposalEventType;

public abstract class GameProposalEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    private final UUID gameProposalId;

    public GameProposalEvent(final UUID gameProposalId) {
        this.gameProposalId = gameProposalId;
    }

    public UUID getGameProposalId() {
        return gameProposalId;
    }

    @Override
    public abstract GameProposalEventType getType();

    @Override
    public int hashCode() {
        return Objects.hash(gameProposalId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GameProposalEvent event = (GameProposalEvent) o;
        return Objects.equals(gameProposalId, event.gameProposalId);
    }

}
