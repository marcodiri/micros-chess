package io.github.marcodiri.gameservice.api.event;

import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;

import io.github.marcodiri.core.domain.event.DomainEvent;

public abstract class GameEvent implements DomainEvent {

    private static final long serialVersionUID = 1L;

    protected final UUID gameId;

    public GameEvent(final UUID gameId) {
        this.gameId = gameId;
    }

    public UUID getGameId() {
        return gameId;
    }

    @Override
    public abstract GameEventType getType();

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GameEvent event = (GameEvent) o;
        return Objects.equals(gameId, event.gameId);
    }

}
