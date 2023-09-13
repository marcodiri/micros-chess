package io.github.marcodiri.gameservice.domain;

import io.github.marcodiri.gameservice.api.web.Move;

public class IllegalMoveException extends Exception {

    public IllegalMoveException(Move move) {
        super(String.format("Move %s is illegal for the current position", move));
    }

}
