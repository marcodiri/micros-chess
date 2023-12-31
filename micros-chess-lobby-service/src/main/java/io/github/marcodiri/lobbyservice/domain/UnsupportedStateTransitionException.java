package io.github.marcodiri.lobbyservice.domain;

public class UnsupportedStateTransitionException extends Exception {

    public UnsupportedStateTransitionException(GameProposalState currentState, GameProposalState nextState) {
        super(String.format("Cannot transition from %s to %s", currentState, nextState));
    }

}
