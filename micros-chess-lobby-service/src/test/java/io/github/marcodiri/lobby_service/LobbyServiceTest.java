package io.github.marcodiri.lobby_service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.marcodiri.lobby_service.domain.GameProposal;
import io.github.marcodiri.lobby_service.domain.command.CreateGameProposalCommand;
import io.github.marcodiri.lobby_service.repository.event_store.GameProposalESRepository;

@ExtendWith(MockitoExtension.class)
class LobbyServiceTest {

    @Mock
    private GameProposalESRepository gameProposalESRepository;

    @InjectMocks
    private LobbyService lobbyService;

    @Nested
    class CreateGameProposal {

        @Test
        void createGameProposalCallsRepositorySaveWithCommand() {
            UUID playerId = UUID.randomUUID();

            lobbyService.createGameProposal(playerId);

            ArgumentCaptor<CreateGameProposalCommand> commandCaptor = ArgumentCaptor
                    .forClass(CreateGameProposalCommand.class);
            verify(gameProposalESRepository).save(commandCaptor.capture());

            assertThat(commandCaptor.getValue().getPlayerId()).isEqualTo(playerId);
        }

        @Test
        void createGameProposalReturnsAggregate() {
            GameProposal gameProposal = new GameProposal();
            when(gameProposalESRepository.save(any())).thenReturn(gameProposal);

            GameProposal returnedGameProposal = lobbyService.createGameProposal(UUID.randomUUID());

            assertThat(returnedGameProposal).isEqualTo(gameProposal);
        }

    }

}
