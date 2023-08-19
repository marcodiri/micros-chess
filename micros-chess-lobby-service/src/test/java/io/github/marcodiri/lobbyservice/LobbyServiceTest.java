package io.github.marcodiri.lobbyservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

import io.github.marcodiri.lobbyservice.domain.GameProposalAggregate;
import io.github.marcodiri.lobbyservice.domain.UnsupportedStateTransitionException;
import io.github.marcodiri.lobbyservice.domain.command.AcceptGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.command.CancelGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.command.CreateGameProposalCommand;
import io.github.marcodiri.lobbyservice.repository.eventstore.GameProposalESRepository;

@ExtendWith(MockitoExtension.class)
class LobbyServiceTest {

    @Mock
    private GameProposalESRepository gameProposalESRepository;

    @InjectMocks
    private LobbyService lobbyService;

    @Nested
    class CreateGameProposal {

        @Test
        void createGameProposalCallsRepositorySaveWithCommand() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException {
            UUID creatorId = UUID.randomUUID();
            CreateGameProposalCommand expectedCommand = new CreateGameProposalCommand(creatorId);

            lobbyService.createGameProposal(creatorId);

            verify(gameProposalESRepository).save(expectedCommand);
        }

        @Test
        void createGameProposalReturnsAggregate() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException {
            GameProposalAggregate gameProposal = new GameProposalAggregate();
            when(gameProposalESRepository.save(any())).thenReturn(gameProposal);

            GameProposalAggregate returnedGameProposal = lobbyService.createGameProposal(UUID.randomUUID());

            assertThat(returnedGameProposal).isEqualTo(gameProposal);
        }

    }

    @Nested
    class CancelGameProposal {

        @Test
        void cancelGameProposalCallsRepositoryUpdateWithCommand()
                throws StreamReadException, DatabindException, InterruptedException, ExecutionException, IOException,
                IllegalAccessException, InvocationTargetException, NoSuchMethodException,
                UnsupportedStateTransitionException {
            UUID creatorId = UUID.randomUUID();
            UUID gameProposalId = UUID.randomUUID();
            CancelGameProposalCommand expectedCommand = new CancelGameProposalCommand(creatorId);

            lobbyService.cancelGameProposal(gameProposalId, creatorId);

            verify(gameProposalESRepository).update(gameProposalId, expectedCommand);
        }

        @Test
        void cancelGameProposalReturnsAggregate()
                throws StreamReadException, DatabindException, InterruptedException, ExecutionException, IOException,
                IllegalAccessException, InvocationTargetException, NoSuchMethodException,
                UnsupportedStateTransitionException {
            GameProposalAggregate gameProposal = new GameProposalAggregate();
            when(gameProposalESRepository.update(isA(UUID.class), isA(CancelGameProposalCommand.class)))
                    .thenReturn(gameProposal);

            GameProposalAggregate returnedGameProposal = lobbyService.cancelGameProposal(UUID.randomUUID(),
                    UUID.randomUUID());

            assertThat(returnedGameProposal).isEqualTo(gameProposal);
        }

    }

    @Nested
    class AcceptGameProposal {

        @Test
        void acceptGameProposalCallsRepositoryUpdateWithCommand()
                throws StreamReadException, DatabindException, IllegalAccessException, InvocationTargetException,
                NoSuchMethodException, InterruptedException, ExecutionException, IOException,
                UnsupportedStateTransitionException {
            UUID acceptorId = UUID.randomUUID();
            UUID gameProposalId = UUID.randomUUID();
            AcceptGameProposalCommand expectedCommand = new AcceptGameProposalCommand(acceptorId);

            lobbyService.acceptGameProposal(gameProposalId, acceptorId);

            verify(gameProposalESRepository).update(gameProposalId, expectedCommand);
        }

        @Test
        void acceptGameProposalReturnsAggregate()
                throws StreamReadException, DatabindException, IllegalAccessException, InvocationTargetException,
                NoSuchMethodException, InterruptedException, ExecutionException, IOException,
                UnsupportedStateTransitionException {
            GameProposalAggregate gameProposal = new GameProposalAggregate();
            when(gameProposalESRepository.update(isA(UUID.class), isA(AcceptGameProposalCommand.class)))
                    .thenReturn(gameProposal);

            GameProposalAggregate returnedGameProposal = lobbyService.acceptGameProposal(UUID.randomUUID(),
                    UUID.randomUUID());

            assertThat(returnedGameProposal).isEqualTo(gameProposal);
        }

    }

}
