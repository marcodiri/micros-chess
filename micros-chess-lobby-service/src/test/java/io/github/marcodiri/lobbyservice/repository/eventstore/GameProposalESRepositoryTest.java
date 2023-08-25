package io.github.marcodiri.lobbyservice.repository.eventstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eventstore.dbclient.EventData;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

import io.github.marcodiri.core.domain.Aggregate;
import io.github.marcodiri.core.domain.event.DomainEvent;
import io.github.marcodiri.lobbyservice.api.event.GameProposalCanceled;
import io.github.marcodiri.lobbyservice.api.event.GameProposalCreated;
import io.github.marcodiri.lobbyservice.domain.GameProposalAggregate;
import io.github.marcodiri.lobbyservice.domain.GameProposalFactory;
import io.github.marcodiri.lobbyservice.domain.UnsupportedStateTransitionException;
import io.github.marcodiri.lobbyservice.domain.command.AcceptGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.command.CancelGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.command.CreateGameProposalCommand;

@ExtendWith(MockitoExtension.class)
public class GameProposalESRepositoryTest {

    @Mock
    GameProposalAggregate gameProposal;

    @Mock
    private GameProposalFactory gameProposalFactory;

    private class TestGameProposalESRepository extends GameProposalESRepository {

        public TestGameProposalESRepository() {
            super(null, gameProposalFactory);
        }

        @Override
        public List<DomainEvent> readEventsForAggregate(UUID gameProposalId)
                throws InterruptedException, ExecutionException, IOException {
            return null;
        }

        @Override
        protected List<EventData> applyEventsToAggregate(Aggregate aggregate, List<DomainEvent> events)
                throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
            return null;
        }

        @Override
        protected void applyAndWriteEvents(Aggregate aggregate, List<DomainEvent> events) {
        }

    }

    @Spy
    private TestGameProposalESRepository gameProposalESRepository;

    @Nested
    class save {

        private CreateGameProposalCommand cmd;

        @BeforeEach
        void setup() {
            when(gameProposalFactory.createAggregate()).thenReturn(gameProposal);
            cmd = new CreateGameProposalCommand(UUID.randomUUID());
        }

        @Test
        void saveCreatesNewGameProposal() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException {
            gameProposalESRepository.save(cmd);

            verify(gameProposalFactory).createAggregate();
        }

        @Test
        void saveCallsProcessAndApplyAndWriteEvents() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException {
            List<DomainEvent> events = Arrays.asList(
                    new GameProposalCreated(UUID.randomUUID(), UUID.randomUUID()),
                    new GameProposalCanceled(UUID.randomUUID()));
            when(gameProposal.process(isA(CreateGameProposalCommand.class))).thenReturn(events);

            gameProposalESRepository.save(cmd);

            verify(gameProposal).process(cmd);
            verify(gameProposalESRepository).applyAndWriteEvents(gameProposal, events);
        }

        @Test
        void saveReturnsGameProposal() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException {
            GameProposalAggregate returnedGameProposal = gameProposalESRepository.save(cmd);

            assertThat(returnedGameProposal).isInstanceOf(GameProposalAggregate.class);
        }

    }

    @Nested
    class updateWithCancelGameProposalCommand {

        private UUID gameProposalId;
        private CancelGameProposalCommand cmd;

        @BeforeEach
        void setup() {
            gameProposalId = UUID.randomUUID();
            when(gameProposalFactory.createAggregate()).thenReturn(gameProposal);
            cmd = new CancelGameProposalCommand(UUID.randomUUID());
        }

        @Test
        void updateCreatesNewGameProposalAndAppliesPastEvents() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException,
                InterruptedException,
                ExecutionException, StreamReadException, DatabindException, IOException,
                UnsupportedStateTransitionException {
            List<DomainEvent> events = Arrays.asList(
                    new GameProposalCreated(gameProposalId, UUID.randomUUID()),
                    new GameProposalCanceled(gameProposalId));
            doReturn(events).when(gameProposalESRepository).readEventsForAggregate(gameProposalId);

            gameProposalESRepository.update(gameProposalId, cmd);

            verify(gameProposalFactory).createAggregate();
            InOrder inOrder = inOrder(gameProposalESRepository);
            inOrder.verify(gameProposalESRepository).readEventsForAggregate(gameProposalId);
            inOrder.verify(gameProposalESRepository).applyEventsToAggregate(gameProposal, events);
        }

        @Test
        void updateCallsProcessAndApplyAndWriteEvents()
                throws StreamReadException, DatabindException, IllegalAccessException, InvocationTargetException,
                NoSuchMethodException, InterruptedException, ExecutionException, IOException,
                UnsupportedStateTransitionException {
            List<DomainEvent> newEvents = Arrays.asList(
                    new GameProposalCreated(gameProposalId, UUID.randomUUID()),
                    new GameProposalCreated(gameProposalId, UUID.randomUUID()));
            when(gameProposal.process(isA(CancelGameProposalCommand.class))).thenReturn(newEvents);

            gameProposalESRepository.update(gameProposalId, cmd);

            verify(gameProposal).process(cmd);
            verify(gameProposalESRepository).applyAndWriteEvents(gameProposal, newEvents);
        }

        @Test
        void updateReturnsGameProposal() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException, StreamReadException, DatabindException, IOException,
                UnsupportedStateTransitionException {
            GameProposalAggregate returnedGameProposal = gameProposalESRepository.update(gameProposalId, cmd);
            assertThat(returnedGameProposal).isInstanceOf(GameProposalAggregate.class);
        }

    }

    @Nested
    class updateWithAcceptGameProposalCommand {

        private UUID gameProposalId;
        private AcceptGameProposalCommand cmd;

        @BeforeEach
        void setup() {
            gameProposalId = UUID.randomUUID();
            when(gameProposalFactory.createAggregate()).thenReturn(gameProposal);
            cmd = new AcceptGameProposalCommand(UUID.randomUUID());
        }

        @Test
        void updateCreatesNewGameProposalAndAppliesPastEvents() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException,
                InterruptedException,
                ExecutionException, StreamReadException, DatabindException, IOException,
                UnsupportedStateTransitionException {
            List<DomainEvent> events = Arrays.asList(
                    new GameProposalCreated(gameProposalId, UUID.randomUUID()),
                    new GameProposalCanceled(gameProposalId));
            doReturn(events).when(gameProposalESRepository).readEventsForAggregate(gameProposalId);

            gameProposalESRepository.update(gameProposalId, cmd);

            verify(gameProposalFactory).createAggregate();
            InOrder inOrder = inOrder(gameProposalESRepository);
            inOrder.verify(gameProposalESRepository).readEventsForAggregate(gameProposalId);
            inOrder.verify(gameProposalESRepository).applyEventsToAggregate(gameProposal, events);
        }

        @Test
        void updateCallsProcessAndApplyAndWriteEvents()
                throws StreamReadException, DatabindException, IllegalAccessException, InvocationTargetException,
                NoSuchMethodException, InterruptedException, ExecutionException, IOException,
                UnsupportedStateTransitionException {
            List<DomainEvent> newEvents = Arrays.asList(
                    new GameProposalCreated(gameProposalId, UUID.randomUUID()),
                    new GameProposalCreated(gameProposalId, UUID.randomUUID()));
            when(gameProposal.process(isA(AcceptGameProposalCommand.class))).thenReturn(newEvents);

            gameProposalESRepository.update(gameProposalId, cmd);

            verify(gameProposal).process(cmd);
            verify(gameProposalESRepository).applyAndWriteEvents(gameProposal, newEvents);
        }

        @Test
        void updateReturnsGameProposal() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException, StreamReadException, DatabindException, IOException,
                UnsupportedStateTransitionException {
            GameProposalAggregate returnedGameProposal = gameProposalESRepository.update(gameProposalId, cmd);
            assertThat(returnedGameProposal).isInstanceOf(GameProposalAggregate.class);
        }

    }

}
