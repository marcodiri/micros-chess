package io.github.marcodiri.core.domain.event;

import java.io.Serializable;

public interface DomainEvent extends Serializable {
    DomainEventType getType();
}
