package io.github.marcodiri.lobbyservice.domain.command;

import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class GameProposalCommand {

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}