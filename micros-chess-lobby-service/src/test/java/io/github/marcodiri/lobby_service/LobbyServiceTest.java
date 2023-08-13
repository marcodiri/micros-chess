package io.github.marcodiri.lobby_service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.marcodiri.lobbyservice.LobbyService;
import io.github.marcodiri.lobbyservice.domain.GameProposal;
import io.github.marcodiri.lobbyservice.domain.command.AcceptGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.command.CancelGameProposalCommand;
import io.github.marcodiri.lobbyservice.domain.command.CreateGameProposalCommand;
import io.github.marcodiri.lobbyservice.repository.event_store.GameProposalESRepository;

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

            lobbyService.createGameProposal(creatorId);

            ArgumentCaptor<CreateGameProposalCommand> commandCaptor = ArgumentCaptor
                    .forClass(CreateGameProposalCommand.class);
            verify(gameProposalESRepository).save(commandCaptor.capture());

            assertThat(commandCaptor.getValue().getCreatorId()).isEqualTo(creatorId);
        }

        @Test
        void createGameProposalReturnsAggregate() throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException,
                ExecutionException {
            GameProposal gameProposal = new GameProposal();
            when(gameProposalESRepository.save(any())).thenReturn(gameProposal);

            GameProposal returnedGameProposal = lobbyService.createGameProposal(UUID.randomUUID());

            assertThat(returnedGameProposal).isEqualTo(gameProposal);
        }

    }

    @Nested
    class CancelGameProposal {

        @Test
        void cancelGameProposalCallsRepositoryUpdateWithCommand() {
            UUID creatorId = UUID.randomUUID();
            UUID gameProposalId = UUID.randomUUID();

            lobbyService.cancelGameProposal(gameProposalId, creatorId);

            ArgumentCaptor<UUID> gameProposalIdCaptor = ArgumentCaptor
                    .forClass(UUID.class);
            ArgumentCaptor<CancelGameProposalCommand> commandCaptor = ArgumentCaptor
                    .forClass(CancelGameProposalCommand.class);
            verify(gameProposalESRepository).update(gameProposalIdCaptor.capture(), commandCaptor.capture());

            assertThat(gameProposalIdCaptor.getValue()).isEqualTo(gameProposalId);
            assertThat(commandCaptor.getValue().getCreatorId()).isEqualTo(creatorId);
        }

        @Test
        void cancelGameProposalReturnsAggregate() {
            GameProposal gameProposal = new GameProposal();
            when(gameProposalESRepository.update(isA(UUID.class), isA(CancelGameProposalCommand.class)))
                    .thenReturn(gameProposal);

            GameProposal returnedGameProposal = lobbyService.cancelGameProposal(UUID.randomUUID(), UUID.randomUUID());

            assertThat(returnedGameProposal).isEqualTo(gameProposal);
        }

    }

    @Nested
    class AcceptGameProposal {

        @Test
        void acceptGameProposalCallsRepositoryUpdateWithCommand() {
            UUID acceptorId = UUID.randomUUID();
            UUID gameProposalId = UUID.randomUUID();

            lobbyService.acceptGameProposal(gameProposalId, acceptorId);

            ArgumentCaptor<UUID> gameProposalIdCaptor = ArgumentCaptor
                    .forClass(UUID.class);
            ArgumentCaptor<AcceptGameProposalCommand> commandCaptor = ArgumentCaptor
                    .forClass(AcceptGameProposalCommand.class);
            verify(gameProposalESRepository).update(gameProposalIdCaptor.capture(), commandCaptor.capture());

            assertThat(gameProposalIdCaptor.getValue()).isEqualTo(gameProposalId);
            assertThat(commandCaptor.getValue().getAcceptorId()).isEqualTo(acceptorId);
        }

        @Test
        void acceptGameProposalReturnsAggregate() {
            GameProposal gameProposal = new GameProposal();
            when(gameProposalESRepository.update(isA(UUID.class), isA(AcceptGameProposalCommand.class)))
                    .thenReturn(gameProposal);

            GameProposal returnedGameProposal = lobbyService.acceptGameProposal(UUID.randomUUID(), UUID.randomUUID());

            assertThat(returnedGameProposal).isEqualTo(gameProposal);
        }

    }

}
