package io.github.marcodiri.lobbyservice.domain;

import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.marcodiri.core.domain.event.DomainEvent;
import io.github.marcodiri.lobbyservice.api.event.GameProposalCreated;
import io.github.marcodiri.lobbyservice.domain.command.CreateGameProposalCommand;

@ExtendWith(MockitoExtension.class)
public class GameProposalAggregateTest {

    @Nested
    class processCreateGameProposal {

        private UUID creatorId;
        private CreateGameProposalCommand cmd;

        @Spy
        private GameProposalAggregate gameProposal;

        @BeforeEach
        void createCommand() {
            creatorId = UUID.randomUUID();
            cmd = new CreateGameProposalCommand(creatorId);
        }

        @Test
        void processReturnsEventsForCreation() {
            UUID gameProposalId = UUID.randomUUID();
            GameProposalCreated expectedEvent = new GameProposalCreated(gameProposalId, creatorId);
            when(gameProposal.generateId()).thenReturn(gameProposalId);

            List<DomainEvent> events = gameProposal.process(cmd);

            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isEqualTo(expectedEvent);
        }

    }

}
