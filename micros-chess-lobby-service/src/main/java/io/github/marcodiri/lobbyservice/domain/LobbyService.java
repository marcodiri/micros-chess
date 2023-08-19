package io.github.marcodiri.lobbyservice.domain;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

import io.github.marcodiri.lobbyservice.domain.command.AcceptGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.command.CancelGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.command.CreateGameProposalCommand;
import io.github.marcodiri.lobbyservice.repository.eventstore.GameProposalESRepository;

public class LobbyService {

    private GameProposalESRepository gameProposalESRepository;

    private static final Logger LOGGER = LogManager.getLogger(LobbyService.class);

    public LobbyService(GameProposalESRepository gameProposalESRepository) {
        this.gameProposalESRepository = gameProposalESRepository;
    }

    public GameProposalAggregate createGameProposal(UUID creatorId)
            throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
            ExecutionException {
        GameProposalAggregate gameProposal = gameProposalESRepository.save(new CreateGameProposalCommand(creatorId));
        LOGGER.info("Created: {}", gameProposal);
        return gameProposal;
    }

    public GameProposalAggregate cancelGameProposal(UUID gameProposalId, UUID creatorId)
            throws StreamReadException, DatabindException, InterruptedException, ExecutionException, IOException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            UnsupportedStateTransitionException {
        GameProposalAggregate gameProposal = gameProposalESRepository
                .update(gameProposalId, new CancelGameProposalCommand(creatorId));
        LOGGER.info("Canceled: {}", gameProposal);
        return gameProposal;
    }

    public GameProposalAggregate acceptGameProposal(UUID gameProposalId, UUID acceptorId)
            throws StreamReadException, DatabindException, InterruptedException, ExecutionException, IOException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            UnsupportedStateTransitionException {
        GameProposalAggregate gameProposal = gameProposalESRepository
                .update(gameProposalId, new AcceptGameProposalCommand(acceptorId));
        LOGGER.info("Accepted: {}", gameProposal);
        return gameProposal;
    }

}
