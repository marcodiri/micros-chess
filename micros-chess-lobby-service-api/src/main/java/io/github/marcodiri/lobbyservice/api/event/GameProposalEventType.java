package io.github.marcodiri.lobbyservice.api.event;

import io.github.marcodiri.core.domain.event.DomainEventType;

public enum GameProposalEventType implements DomainEventType {

    CREATED(GameProposalEventType.TYPE_CREATED, GameProposalCreated.class),
    CANCELED(GameProposalEventType.TYPE_CANCELED, GameProposalCanceled.class),
    ACCEPTED(GameProposalEventType.TYPE_ACCEPTED, GameProposalAccepted.class);

    private static final String TYPE_CREATED = "game-proposal-created";
    private static final String TYPE_CANCELED = "game-proposal-canceled";
    private static final String TYPE_ACCEPTED = "game-proposal-accepted";

    public static GameProposalEventType fromString(final String type) {
        switch (type) {
            case TYPE_CREATED:
                return GameProposalEventType.CREATED;
            case TYPE_CANCELED:
                return GameProposalEventType.CANCELED;
            case TYPE_ACCEPTED:
                return GameProposalEventType.ACCEPTED;
            default:
                return null;
        }
    }

    private final String type;
    private final Class<? extends GameProposalEvent> eventClass;

    GameProposalEventType(final String type, final Class<? extends GameProposalEvent> eventClass) {
        this.type = type;
        this.eventClass = eventClass;
    }

    @Override
    public String toString() {
        return type;
    }

    public Class<? extends GameProposalEvent> getEventClass() {
        return eventClass;
    }

}
