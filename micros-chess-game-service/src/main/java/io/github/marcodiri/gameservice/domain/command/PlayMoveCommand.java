package io.github.marcodiri.gameservice.domain.command;

import java.util.Objects;
import java.util.UUID;

public class PlayMoveCommand extends GameCommand {

    private final UUID playerId;
    private final String move;


    public PlayMoveCommand(final UUID playerId, final String move) {
        this.playerId = playerId;
        this.move = move;
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
        PlayMoveCommand command = (PlayMoveCommand) o;
        return Objects.equals(playerId, command.playerId)
                && Objects.equals(move, command.move);
    }

}
