package io.github.marcodiri.gameservice.domain;

import io.github.marcodiri.core.domain.AggregateFactory;

public class GameFactory implements AggregateFactory {

    public GameAggregate createAggregate() {
        return new GameAggregate();
    }

}
