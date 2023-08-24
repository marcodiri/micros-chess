package io.github.marcodiri.gameservice.api.event;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MovePlayed extends GameEvent {

    private static final long serialVersionUID = 1L;

    private final GameEventType type = GameEventType.MOVE;

    private final UUID playerId;
    private final String move;

    @JsonCreator
    public MovePlayed(
            @JsonProperty("gameId") final UUID gameId,
            @JsonProperty("playerId") final UUID playerId,
            @JsonProperty("move") final String move) {
        super(gameId);
        this.playerId = playerId;
        this.move = move;
    }

    @Override
    public GameEventType getType() {
        return type;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getMove() {
        return move;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MovePlayed event = (MovePlayed) o;
        return super.equals(o)
                && Objects.equals(playerId, event.playerId)
                && Objects.equals(move, event.move);
    }

}
