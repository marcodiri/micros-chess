package io.github.marcodiri.gameservice.api.web;

import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class PlayMoveRequest {

    private UUID gameId;
    private UUID playerId;
    private String move;

    public PlayMoveRequest() {
    }

    public PlayMoveRequest(final UUID gameId, final UUID playerId, final String move) {
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

    public String getMove() {
        return move;
    }

    public void setMove(final String move) {
        this.move = move;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
