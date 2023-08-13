package io.github.marcodiri.lobbyservice.domain;

import java.util.List;
import java.util.UUID;

import io.github.marcodiri.core.domain.event.DomainEvent;
import io.github.marcodiri.lobbyservice.domain.command.CreateGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.event.GameProposalCanceled;
import io.github.marcodiri.lobbyservice.domain.event.GameProposalCreated;

public class GameProposal {

    private UUID id;

    public GameProposal() {
    }

    public UUID getId() {
        return id;
    }

    public List<DomainEvent> process(CreateGameProposalCommand cmd) {
        return null;
    }

    public void apply(GameProposalCreated event) {
    }

    public void apply(GameProposalCanceled event) {
    }


}
