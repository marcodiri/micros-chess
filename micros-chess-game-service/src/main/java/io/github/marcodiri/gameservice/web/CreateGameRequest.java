package io.github.marcodiri.gameservice.web;

import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class CreateGameRequest {

    private UUID player1Id;
    private UUID player2Id;

    public CreateGameRequest() {
    }

    public CreateGameRequest(final UUID player1Id, final UUID player2Id) {
        this.player1Id = player1Id;
        this.player2Id = player2Id;
    }

    public UUID getPlayer1Id() {
        return player1Id;
    }

    public void setPlayer1Id(final UUID player1Id) {
        this.player1Id = player1Id;
    }

    public UUID getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(final UUID player2Id) {
        this.player2Id = player2Id;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
