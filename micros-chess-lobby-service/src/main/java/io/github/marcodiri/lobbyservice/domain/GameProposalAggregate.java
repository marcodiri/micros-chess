package io.github.marcodiri.lobbyservice.domain;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;

import io.github.marcodiri.core.domain.event.DomainEvent;
import io.github.marcodiri.lobbyservice.api.event.GameProposalCanceled;
import io.github.marcodiri.lobbyservice.api.event.GameProposalCreated;
import io.github.marcodiri.lobbyservice.domain.command.AcceptGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.command.CancelGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.command.CreateGameProposalCommand;

public class GameProposalAggregate {

    private UUID id;

    public GameProposalAggregate() {
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public List<DomainEvent> process(CreateGameProposalCommand command) {
        return null;
    }

    public List<DomainEvent> process(CancelGameProposalCommand command) {
        return null;
    }

    public List<DomainEvent> process(AcceptGameProposalCommand command) {
        return null;
    }

    public void apply(GameProposalCreated event) {
    }

    public void apply(GameProposalCanceled event) {
    }

}
