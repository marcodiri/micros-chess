package io.github.marcodiri.gameservice.domain;

public class GameNotInProgressException extends Exception {

    public GameNotInProgressException(GameState state) {
        super(String.format("Move cannot be played because the game is in state %s", state));
    }

}
