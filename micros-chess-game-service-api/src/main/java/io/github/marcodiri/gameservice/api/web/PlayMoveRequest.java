package io.github.marcodiri.gameservice.api.web;

import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class PlayMoveRequest {

    private UUID gameId;
    private UUID playerId;
    private Move move;

    public PlayMoveRequest() {
    }

    public PlayMoveRequest(
            final UUID gameId,
            final UUID playerId,
            final Move move) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.move = move;
    }

    public UUID getGameId() {
        return gameId;
    }

    public void setGameId(final UUID gameId) {
        this.gameId = gameId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(final UUID playerId) {
        this.playerId = playerId;
    }

    public Move getMove() {
        return move;
    }

    public void setMove(final Move move) {
        this.move = move;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PlayMoveRequest request = (PlayMoveRequest) o;
        return Objects.equals(gameId, request.gameId)
                && Objects.equals(playerId, request.playerId)
                && Objects.equals(move, request.move);
    }

}
