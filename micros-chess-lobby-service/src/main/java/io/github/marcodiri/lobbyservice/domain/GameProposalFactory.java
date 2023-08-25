package io.github.marcodiri.lobbyservice.domain;

import io.github.marcodiri.core.domain.AggregateFactory;

public class GameProposalFactory implements AggregateFactory {

    public GameProposalAggregate createAggregate() {
        return new GameProposalAggregate();
    }

}
