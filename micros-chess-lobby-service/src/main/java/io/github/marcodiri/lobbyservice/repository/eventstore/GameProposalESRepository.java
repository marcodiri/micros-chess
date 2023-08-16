package io.github.marcodiri.lobbyservice.repository.eventstore;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.ReadResult;
import com.eventstore.dbclient.ReadStreamOptions;
import com.eventstore.dbclient.ResolvedEvent;
import com.eventstore.dbclient.WriteResult;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.marcodiri.core.domain.event.DomainEvent;
import io.github.marcodiri.lobbyservice.domain.GameProposal;
import io.github.marcodiri.lobbyservice.domain.GameProposalFactory;
import io.github.marcodiri.lobbyservice.domain.command.AcceptGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.command.CancelGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.command.CreateGameProposalCommand;

public class GameProposalESRepository {

    private EventStoreDBClient client;
    private GameProposalFactory gameProposalFactory;

    private static final Logger LOGGER = LogManager.getLogger(GameProposalESRepository.class);

    public GameProposalESRepository(EventStoreDBClient client, GameProposalFactory gameProposalFactory) {
        this.client = client;
        this.gameProposalFactory = gameProposalFactory;
    }

    public GameProposal save(CreateGameProposalCommand cmd) throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
            ExecutionException {
        GameProposal gameProposal = gameProposalFactory.createGameProposal();
        List<DomainEvent> events = gameProposal.process(cmd);

        List<EventData> eventDataList = new ArrayList<>();
        events.forEach(event -> {
            EventData eventData = EventData
                    .builderAsJson(event.getType().toString(), event)
                    .build();
            eventDataList.add(eventData);
        });

        WriteResult writeResult = client
                .appendToStream(streamNameFromGameProposal(gameProposal), eventDataList.iterator())
                .get();

        LOGGER.info("Saved events to EventStore: {}", events);
        LOGGER.debug(writeResult);

        return gameProposal;
    }

    public GameProposal update(UUID gameProposalId, CancelGameProposalCommand cmd) {
        return null;
    }

    public GameProposal update(UUID gameProposalId, AcceptGameProposalCommand cmd) {
        return null;
    }

    private String streamNameFromGameProposal(GameProposal gameProposal) {
        UUID gameProposalId = gameProposal.getId();
        return String.format("GameProposal_%s", gameProposalId);
    }

}
