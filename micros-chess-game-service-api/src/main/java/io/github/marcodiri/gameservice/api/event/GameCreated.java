package io.github.marcodiri.gameservice.api.event;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GameCreated extends GameEvent {

private static final long serialVersionUID = 1L;

    private final GameEventType type = GameEventType.CREATED;

    private final UUID player1Id;
    private final UUID player2Id;

    @JsonCreator
    public GameCreated(
            @JsonProperty("gameId") final UUID gameId,
            @JsonProperty("player1Id") final UUID player1Id,
            @JsonProperty("player2Id") final UUID player2Id) {
        super(gameId);
        this.player1Id = player1Id;
        this.player2Id = player2Id;
    }

    @Override
    public GameEventType getType() {
        return type;
    }

    public UUID getPlayer1Id() {
        return player1Id;
    }

    public UUID getPlayer2Id() {
        return player2Id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GameCreated event = (GameCreated) o;
        return super.equals(o)
                && Objects.equals(player1Id, event.player1Id)
                && Objects.equals(player2Id, event.player2Id);
    }

}
