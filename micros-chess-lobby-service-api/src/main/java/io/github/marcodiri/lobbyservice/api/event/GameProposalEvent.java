package io.github.marcodiri.lobbyservice.api.event;

import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;

import io.github.marcodiri.core.domain.event.DomainEvent;

public abstract class GameProposalEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    protected final UUID gameProposalId;

    public GameProposalEvent(final UUID gameProposalId) {
        this.gameProposalId = gameProposalId;
    }

    public UUID getGameProposalId() {
        return gameProposalId;
    }

    @Override
    public abstract GameProposalEventType getType();

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

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
