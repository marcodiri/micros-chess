package io.github.marcodiri.gameservice.domain;

public class IllegalMoveException extends Exception {

    public IllegalMoveException(String move) {
        super(String.format("Move %s is illegal for the current position", move));
    }

}
