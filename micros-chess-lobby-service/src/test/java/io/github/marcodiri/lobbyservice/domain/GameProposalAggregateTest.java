package io.github.marcodiri.lobbyservice.domain;

import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.marcodiri.core.domain.event.DomainEvent;
import io.github.marcodiri.lobbyservice.api.event.GameProposalAccepted;
import io.github.marcodiri.lobbyservice.api.event.GameProposalCanceled;
import io.github.marcodiri.lobbyservice.api.event.GameProposalCreated;
import io.github.marcodiri.lobbyservice.domain.command.AcceptGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.command.CancelGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.command.CreateGameProposalCommand;

@ExtendWith(MockitoExtension.class)
public class GameProposalAggregateTest {

    @Nested
    class processCreateGameProposal {

        private UUID gameProposalId, creatorId;
        private CreateGameProposalCommand cmd;

        @Spy
        private GameProposalAggregate gameProposal;

        @BeforeEach
        void createCommand() {
            gameProposalId = UUID.randomUUID();
            creatorId = UUID.randomUUID();
            cmd = new CreateGameProposalCommand(creatorId);
        }

        @Test
        void processReturnsEventsForCreation() {
            GameProposalCreated expectedEvent = new GameProposalCreated(gameProposalId, creatorId);
            when(gameProposal.generateId()).thenReturn(gameProposalId);

            List<DomainEvent> events = gameProposal.process(cmd);

            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isEqualTo(expectedEvent);
        }

    }

    @Nested
    class processCancelGameProposal {

        private UUID gameProposalId, creatorId;
        private CancelGameProposalCommand cmd;

        @Spy
        private GameProposalAggregate gameProposal;

        @BeforeEach
        void createCommand() {
            gameProposalId = UUID.randomUUID();
            creatorId = UUID.randomUUID();
            cmd = new CancelGameProposalCommand(creatorId);
        }

        @Test
        void processReturnsEventsForCancellation() throws UnsupportedStateTransitionException {
            GameProposalCanceled expectedEvent = new GameProposalCanceled(gameProposalId);
            when(gameProposal.getId()).thenReturn(gameProposalId);
            when(gameProposal.getState()).thenReturn(GameProposalState.PENDING);

            List<DomainEvent> events = gameProposal.process(cmd);

            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isEqualTo(expectedEvent);
        }

        @ParameterizedTest
        @NullSource
        @EnumSource(
        value = GameProposalState.class,
        names = {"PENDING"},
        mode = EnumSource.Mode.EXCLUDE)
        void processThrowsIfGameProposalIsNotInCorrectState(GameProposalState state) {
            when(gameProposal.getState()).thenReturn(state);

            assertThatThrownBy(() -> gameProposal.process(cmd))
                .isInstanceOf(UnsupportedStateTransitionException.class)
                .hasMessage("Cannot transition from %s to %s", state, GameProposalState.CANCELED);
        }

    }

    @Nested
    class processAcceptGameProposal {

        private UUID gameProposalId, creatorId, acceptorId;
        private AcceptGameProposalCommand cmd;

        @Spy
        private GameProposalAggregate gameProposal;

        @BeforeEach
        void createCommand() {
            gameProposalId = UUID.randomUUID();
            creatorId = UUID.randomUUID();
            acceptorId = UUID.randomUUID();
            cmd = new AcceptGameProposalCommand(acceptorId);
        }

        @Test
        void processReturnsEventsForAcceptation() throws UnsupportedStateTransitionException {
            GameProposalAccepted expectedEvent = new GameProposalAccepted(gameProposalId, creatorId, acceptorId);
            when(gameProposal.getId()).thenReturn(gameProposalId);
            when(gameProposal.getCreatorId()).thenReturn(creatorId);
            when(gameProposal.getState()).thenReturn(GameProposalState.PENDING);

            List<DomainEvent> events = gameProposal.process(cmd);

            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isEqualTo(expectedEvent);
        }

        @ParameterizedTest
        @NullSource
        @EnumSource(
        value = GameProposalState.class,
        names = {"PENDING"},
        mode = EnumSource.Mode.EXCLUDE)
        void processThrowsIfGameProposalIsNotInCorrectState(GameProposalState state) {
            when(gameProposal.getState()).thenReturn(state);

            assertThatThrownBy(() -> gameProposal.process(cmd))
                .isInstanceOf(UnsupportedStateTransitionException.class)
                .hasMessage("Cannot transition from %s to %s", state, GameProposalState.ACCEPTED);
        }

    }

}
