package io.github.marcodiri.lobbyservice.domain;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.marcodiri.core.domain.event.DomainEvent;
import io.github.marcodiri.lobbyservice.api.event.GameProposalAccepted;
import io.github.marcodiri.lobbyservice.api.event.GameProposalCanceled;
import io.github.marcodiri.lobbyservice.api.event.GameProposalCreated;
import io.github.marcodiri.lobbyservice.domain.command.AcceptGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.command.CancelGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.command.CreateGameProposalCommand;

public class GameProposalAggregate {

    private UUID id;
    private UUID creatorId;
    private GameProposalState state;

    private static final Logger LOGGER = LogManager.getLogger(GameProposalAggregate.class);

    public GameProposalAggregate() {
    }

    public UUID getId() {
        return id;
    }

    UUID generateId() {
        return UUID.randomUUID();
    }

    UUID getCreatorId() {
        return creatorId;
    }

    GameProposalState getState() {
        return state;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public List<DomainEvent> process(CreateGameProposalCommand command) {
        LOGGER.info("Calling process for CreateGameProposalCommand: {}", command);
        UUID gameProposalId = generateId();
        return Collections.singletonList(
                new GameProposalCreated(gameProposalId, command.getCreatorId()));
    }

    public List<DomainEvent> process(CancelGameProposalCommand command) throws UnsupportedStateTransitionException {
        LOGGER.info("Calling process for CancelGameProposalCommand: {}", command);
        if (getState() != GameProposalState.PENDING) {
            throw new UnsupportedStateTransitionException(getState(), GameProposalState.CANCELED);
        }
        return Collections.singletonList(
                new GameProposalCanceled(getId()));
    }

    public List<DomainEvent> process(AcceptGameProposalCommand command) throws UnsupportedStateTransitionException {
        LOGGER.info("Calling process for AcceptGameProposalCommand: {}", command);
        if (getState() != GameProposalState.PENDING) {
            throw new UnsupportedStateTransitionException(getState(), GameProposalState.ACCEPTED);
        }
        return Collections.singletonList(
                new GameProposalAccepted(getId(), getCreatorId(), command.getAcceptorId()));
    }

    public void apply(GameProposalCreated event) {
    }

    public void apply(GameProposalCanceled event) {
    }

}
