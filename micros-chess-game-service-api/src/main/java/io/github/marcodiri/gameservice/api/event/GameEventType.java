package io.github.marcodiri.gameservice.api.event;

import io.github.marcodiri.core.domain.event.DomainEventType;

public enum GameEventType implements DomainEventType {

    CREATED(GameEventType.TYPE_CREATED, GameCreated.class),
    MOVE(GameEventType.TYPE_MOVE, MovePlayed.class);

    private static final String TYPE_CREATED = "game-created";
    private static final String TYPE_MOVE = "game-move-played";

    public static GameEventType fromString(final String type) {
        switch (type) {
            case TYPE_CREATED:
                return GameEventType.CREATED;
            case TYPE_MOVE:
                return GameEventType.MOVE;
            default:
                return null;
        }
    }

    private final String type;
    private final Class<? extends GameEvent> eventClass;

    GameEventType(final String type, final Class<? extends GameEvent> eventClass) {
        this.type = type;
        this.eventClass = eventClass;
    }

    @Override
    public String toString() {
        return type;
    }

    public Class<? extends GameEvent> getEventClass() {
        return eventClass;
    }

}