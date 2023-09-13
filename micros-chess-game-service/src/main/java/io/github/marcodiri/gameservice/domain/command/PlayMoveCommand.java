package io.github.marcodiri.gameservice.domain.command;

import java.util.Objects;
import java.util.UUID;

import io.github.marcodiri.gameservice.api.web.Move;

public class PlayMoveCommand extends GameCommand {

    private final UUID playerId;
    private final Move move;


    public PlayMoveCommand(final UUID playerId, final Move move) {
        this.playerId = playerId;
        this.move = move;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Move getMove() {
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
