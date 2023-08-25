package io.github.marcodiri.lobbyservice.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.UUID;

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

    @Spy
    private GameProposalAggregate gameProposal;

    @Nested
    class processCreateGameProposal {

        private UUID gameProposalId, creatorId;
        private CreateGameProposalCommand cmd;

        @BeforeEach
        void createCommand() {
            gameProposalId = UUID.randomUUID();
            creatorId = UUID.randomUUID();
            cmd = new CreateGameProposalCommand(creatorId);
        }

        @Test
        void processReturnsEventsForCreation() {
            GameProposalCreated expectedEvent = new GameProposalCreated(gameProposalId, creatorId);
            doReturn(gameProposalId).when(gameProposal).generateId();

            List<DomainEvent> events = gameProposal.process(cmd);

            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isEqualTo(expectedEvent);
        }

    }

    @Nested
    class processCancelGameProposal {

        private UUID gameProposalId, creatorId;
        private CancelGameProposalCommand cmd;

        @BeforeEach
        void createCommand() {
            gameProposalId = UUID.randomUUID();
            creatorId = UUID.randomUUID();
            cmd = new CancelGameProposalCommand(creatorId);
        }

        @Test
        void processReturnsEventsForCancellation() throws UnsupportedStateTransitionException {
            GameProposalCanceled expectedEvent = new GameProposalCanceled(gameProposalId);
            doReturn(gameProposalId).when(gameProposal).getId();
            doReturn(GameProposalState.PENDING).when(gameProposal).getState();

            List<DomainEvent> events = gameProposal.process(cmd);

            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isEqualTo(expectedEvent);
        }

        @ParameterizedTest
        @NullSource
        @EnumSource(value = GameProposalState.class, names = { "PENDING" }, mode = EnumSource.Mode.EXCLUDE)
        void processThrowsIfGameProposalIsNotInCorrectState(GameProposalState state) {
            doReturn(state).when(gameProposal).getState();

            assertThatThrownBy(() -> gameProposal.process(cmd))
                    .isInstanceOf(UnsupportedStateTransitionException.class)
                    .hasMessage("Cannot transition from %s to %s", state, GameProposalState.CANCELED);
        }

    }

    @Nested
    class processAcceptGameProposal {

        private UUID gameProposalId, creatorId, acceptorId;
        private AcceptGameProposalCommand cmd;

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
            doReturn(gameProposalId).when(gameProposal).getId();
            doReturn(creatorId).when(gameProposal).getCreatorId();
            doReturn(GameProposalState.PENDING).when(gameProposal).getState();

            List<DomainEvent> events = gameProposal.process(cmd);

            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isEqualTo(expectedEvent);
        }

        @ParameterizedTest
        @NullSource
        @EnumSource(value = GameProposalState.class, names = { "PENDING" }, mode = EnumSource.Mode.EXCLUDE)
        void processThrowsIfGameProposalIsNotInCorrectState(GameProposalState state) {
            doReturn(state).when(gameProposal).getState();

            assertThatThrownBy(() -> gameProposal.process(cmd))
                    .isInstanceOf(UnsupportedStateTransitionException.class)
                    .hasMessage("Cannot transition from %s to %s", state, GameProposalState.ACCEPTED);
        }

    }

    @Nested
    class applyGameProposalCreated {

        private UUID gameProposalId, creatorId;
        private GameProposalCreated event;

        @BeforeEach
        void createCommand() {
            gameProposalId = UUID.randomUUID();
            creatorId = UUID.randomUUID();
            event = new GameProposalCreated(gameProposalId, creatorId);
        }

        @Test
        void applyChangesAggregateState() {
            gameProposal.apply(event);

            assertThat(gameProposal.getId()).isEqualTo(gameProposalId);
            assertThat(gameProposal.getCreatorId()).isEqualTo(creatorId);
            assertThat(gameProposal.getState()).isEqualTo(GameProposalState.PENDING);
        }

    }

    @Nested
    class applyGameProposalCanceled {

        private UUID gameProposalId;
        private GameProposalCanceled event;

        @BeforeEach
        void createCommand() {
            gameProposalId = UUID.randomUUID();
            event = new GameProposalCanceled(gameProposalId);
        }

        @Test
        void applyChangesAggregateState() {
            gameProposal.apply(event);

            assertThat(gameProposal.getState()).isEqualTo(GameProposalState.CANCELED);
        }

    }

    @Nested
    class applyGameProposalAccepted {

        private UUID gameProposalId, creatorId, acceptorId;
        private GameProposalAccepted event;

        @BeforeEach
        void createCommand() {
            gameProposalId = UUID.randomUUID();
            creatorId = UUID.randomUUID();
            acceptorId = UUID.randomUUID();
            event = new GameProposalAccepted(gameProposalId, creatorId, acceptorId);
        }

        @Test
        void applyChangesAggregateState() {
            gameProposal.apply(event);

            assertThat(gameProposal.getAcceptorId()).isEqualTo(acceptorId);
            assertThat(gameProposal.getState()).isEqualTo(GameProposalState.ACCEPTED);
        }

    }

}
