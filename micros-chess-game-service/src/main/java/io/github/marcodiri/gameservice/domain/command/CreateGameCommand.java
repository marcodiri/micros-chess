package io.github.marcodiri.gameservice.domain.command;

import java.util.Objects;
import java.util.UUID;

public class CreateGameCommand extends GameCommand {

    private final UUID player1Id;
    private final UUID player2Id;

    public CreateGameCommand(final UUID player1Id, final UUID player2Id) {
        this.player1Id = player1Id;
        this.player2Id = player2Id;
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
        CreateGameCommand command = (CreateGameCommand) o;
        return Objects.equals(player1Id, command.player1Id)
                && Objects.equals(player2Id, command.player2Id);
    }

}
