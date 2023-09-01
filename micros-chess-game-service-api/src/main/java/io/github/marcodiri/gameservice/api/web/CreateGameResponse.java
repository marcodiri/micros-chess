package io.github.marcodiri.gameservice.api.web;

import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class CreateGameResponse {

    private UUID gameId;

    public CreateGameResponse() {
    }

    public CreateGameResponse(final UUID gameId) {
        this.gameId = gameId;
    }

    public UUID getGameId() {
        return gameId;
    }

    public void setGameId(final UUID creatorId) {
        this.gameId = creatorId;
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
        CreateGameResponse response = (CreateGameResponse) o;
        return Objects.equals(gameId, response.gameId);
    }

}
