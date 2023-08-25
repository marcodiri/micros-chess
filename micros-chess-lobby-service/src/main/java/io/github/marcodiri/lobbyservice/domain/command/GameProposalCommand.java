package io.github.marcodiri.lobbyservice.domain.command;

import org.apache.commons.lang3.builder.ToStringBuilder;

import io.github.marcodiri.core.domain.command.Command;

public abstract class GameProposalCommand implements Command {

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}