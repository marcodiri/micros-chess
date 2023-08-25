package io.github.marcodiri.core.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class Aggregate {

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
