package io.github.marcodiri.gameservice.domain.command;

import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class GameCommand {

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
