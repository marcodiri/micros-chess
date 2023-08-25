package io.github.marcodiri.core.domain;

import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Aggregate {

    protected UUID id;

    protected static final Logger LOGGER = LogManager.getLogger(Aggregate.class);

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public UUID generateId() {
        return UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

}
