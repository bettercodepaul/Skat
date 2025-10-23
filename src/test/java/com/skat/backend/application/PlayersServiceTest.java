package com.skat.backend.application;

import com.skat.backend.api.exception.ConflictException;
import com.skat.backend.api.exception.NotFoundException;
import com.skat.backend.application.dto.*;
import com.skat.backend.domain.entities.PlayerEntity;
import com.skat.backend.domain.repositories.GameRepository;
import com.skat.backend.domain.repositories.PlayerRepository;
import com.skat.backend.domain.repositories.PlayerScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pure unit test for PlayersServiceImpl following ADR-002 (Unit Testing Strategy with Maven Surefire).
 * Uses Mockito for mocking dependencies and AssertJ for assertions.
 * No Spring context is loaded, making this a fast unit test.
 */
class PlayersServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private PlayerScoreRepository playerScoreRepository;

    @InjectMocks
    private PlayersServiceImpl playersService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void given_validRequest_when_createPlayer_then_playerIsCreatedAndReturned() {
        // Given
        UpsertPlayerRequest request = new UpsertPlayerRequest("Anna", "Schmidt");
        PlayerEntity savedPlayer = new PlayerEntity("Anna", "Schmidt");
        UUID playerId = UUID.randomUUID();
        savedPlayer.setId(playerId);

        when(playerRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCase("Anna", "Schmidt"))
            .thenReturn(false);
        when(playerRepository.save(any(PlayerEntity.class))).thenReturn(savedPlayer);

        // When
        PlayerTO result = playersService.createPlayer(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(playerId);
        assertThat(result.first_name()).isEqualTo("Anna");
        assertThat(result.last_name()).isEqualTo("Schmidt");
        
        verify(playerRepository).existsByFirstNameIgnoreCaseAndLastNameIgnoreCase("Anna", "Schmidt");
        verify(playerRepository).save(any(PlayerEntity.class));
    }

    @Test
    void given_duplicateName_when_createPlayer_then_throwsConflictException() {
        // Given
        UpsertPlayerRequest request = new UpsertPlayerRequest("Anna", "Schmidt");
        when(playerRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCase("Anna", "Schmidt"))
            .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> playersService.createPlayer(request))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("Player with first_name+last_name already exists");
        
        verify(playerRepository).existsByFirstNameIgnoreCaseAndLastNameIgnoreCase("Anna", "Schmidt");
        verify(playerRepository, never()).save(any());
    }

    @Test
    void given_nameWithWhitespace_when_createPlayer_then_nameIsTrimmed() {
        // Given
        UpsertPlayerRequest request = new UpsertPlayerRequest("  Anna  ", "  Schmidt  ");
        PlayerEntity savedPlayer = new PlayerEntity("Anna", "Schmidt");
        UUID playerId = UUID.randomUUID();
        savedPlayer.setId(playerId);

        when(playerRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCase("Anna", "Schmidt"))
            .thenReturn(false);
        when(playerRepository.save(any(PlayerEntity.class))).thenReturn(savedPlayer);

        // When
        PlayerTO result = playersService.createPlayer(request);

        // Then
        ArgumentCaptor<PlayerEntity> playerCaptor = ArgumentCaptor.forClass(PlayerEntity.class);
        verify(playerRepository).save(playerCaptor.capture());
        PlayerEntity capturedPlayer = playerCaptor.getValue();
        assertThat(capturedPlayer.getFirstName()).isEqualTo("Anna");
        assertThat(capturedPlayer.getLastName()).isEqualTo("Schmidt");
    }

    @Test
    void given_existingPlayer_when_updatePlayer_then_playerIsUpdatedAndReturned() {
        // Given
        UUID playerId = UUID.randomUUID();
        UpsertPlayerRequest request = new UpsertPlayerRequest("Anna", "Mueller");
        PlayerEntity existingPlayer = new PlayerEntity("Anna", "Schmidt");
        existingPlayer.setId(playerId);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(existingPlayer));
        when(playerRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndIdNot("Anna", "Mueller", playerId))
            .thenReturn(false);
        when(playerRepository.save(any(PlayerEntity.class))).thenReturn(existingPlayer);

        // When
        PlayerTO result = playersService.updatePlayer(playerId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(playerId);
        assertThat(result.first_name()).isEqualTo("Anna");
        assertThat(result.last_name()).isEqualTo("Mueller");
        
        verify(playerRepository).findById(playerId);
        verify(playerRepository).existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndIdNot("Anna", "Mueller", playerId);
        verify(playerRepository).save(existingPlayer);
        assertThat(existingPlayer.getLastName()).isEqualTo("Mueller");
    }

    @Test
    void given_nonExistentPlayer_when_updatePlayer_then_throwsNotFoundException() {
        // Given
        UUID playerId = UUID.randomUUID();
        UpsertPlayerRequest request = new UpsertPlayerRequest("Anna", "Mueller");
        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> playersService.updatePlayer(playerId, request))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Player not found");
        
        verify(playerRepository).findById(playerId);
        verify(playerRepository, never()).save(any());
    }

    @Test
    void given_duplicateNameOnUpdate_when_updatePlayer_then_throwsConflictException() {
        // Given
        UUID playerId = UUID.randomUUID();
        UpsertPlayerRequest request = new UpsertPlayerRequest("Anna", "Mueller");
        PlayerEntity existingPlayer = new PlayerEntity("Anna", "Schmidt");
        existingPlayer.setId(playerId);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(existingPlayer));
        when(playerRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndIdNot("Anna", "Mueller", playerId))
            .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> playersService.updatePlayer(playerId, request))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("Player with first_name+last_name already exists");
        
        verify(playerRepository).findById(playerId);
        verify(playerRepository).existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndIdNot("Anna", "Mueller", playerId);
        verify(playerRepository, never()).save(any());
    }

    @Test
    void given_playerWithoutReferences_when_deletePlayerWithoutForce_then_playerIsDeleted() {
        // Given
        UUID playerId = UUID.randomUUID();
        PlayerEntity player = new PlayerEntity("Anna", "Schmidt");
        player.setId(playerId);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(gameRepository.existsByPlayerId(playerId)).thenReturn(false);
        when(playerScoreRepository.existsByPlayerId(playerId)).thenReturn(false);

        // When
        playersService.deletePlayer(playerId, false);

        // Then
        verify(playerRepository).findById(playerId);
        verify(gameRepository).existsByPlayerId(playerId);
        verify(playerScoreRepository).existsByPlayerId(playerId);
        verify(playerRepository).delete(player);
        verify(gameRepository, never()).nullifyPlayer1References(any());
    }

    @Test
    void given_playerWithGameReferences_when_deletePlayerWithoutForce_then_throwsConflictException() {
        // Given
        UUID playerId = UUID.randomUUID();
        PlayerEntity player = new PlayerEntity("Anna", "Schmidt");
        player.setId(playerId);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(gameRepository.existsByPlayerId(playerId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> playersService.deletePlayer(playerId, false))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("Player is referenced in games or scores");
        
        verify(playerRepository).findById(playerId);
        verify(gameRepository).existsByPlayerId(playerId);
        verify(playerRepository, never()).delete(any());
    }

    @Test
    void given_playerWithScoreReferences_when_deletePlayerWithoutForce_then_throwsConflictException() {
        // Given
        UUID playerId = UUID.randomUUID();
        PlayerEntity player = new PlayerEntity("Anna", "Schmidt");
        player.setId(playerId);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(gameRepository.existsByPlayerId(playerId)).thenReturn(false);
        when(playerScoreRepository.existsByPlayerId(playerId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> playersService.deletePlayer(playerId, false))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("Player is referenced in games or scores");
        
        verify(playerRepository).findById(playerId);
        verify(gameRepository).existsByPlayerId(playerId);
        verify(playerScoreRepository).existsByPlayerId(playerId);
        verify(playerRepository, never()).delete(any());
    }

    @Test
    void given_playerWithReferences_when_forceDeletePlayer_then_referencesNullifiedAndPlayerDeleted() {
        // Given
        UUID playerId = UUID.randomUUID();
        PlayerEntity player = new PlayerEntity("Anna", "Schmidt");
        player.setId(playerId);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        // When
        playersService.deletePlayer(playerId, true);

        // Then
        verify(playerRepository).findById(playerId);
        verify(gameRepository).nullifyPlayer1References(playerId);
        verify(gameRepository).nullifyPlayer2References(playerId);
        verify(gameRepository).nullifyPlayer3References(playerId);
        verify(gameRepository).nullifyMainPlayerReferences(playerId);
        verify(playerScoreRepository).nullifyPlayerReferences(playerId);
        verify(playerRepository).delete(player);
    }

    @Test
    void given_nonExistentPlayer_when_deletePlayer_then_throwsNotFoundException() {
        // Given
        UUID playerId = UUID.randomUUID();
        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> playersService.deletePlayer(playerId, false))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Player not found");
        
        verify(playerRepository).findById(playerId);
        verify(playerRepository, never()).delete(any());
    }

    @Test
    void given_validQuery_when_listPlayers_then_returnsPlayerListResponse() {
        // Given
        PlayersQuery query = new PlayersQuery(0, 50, PlayersSort.NAME);
        
        List<Object[]> mockResults = new ArrayList<>();
        Object[] row1 = new Object[]{
            UUID.randomUUID(),
            "Anna",
            "Schmidt",
            100,
            5,
            java.sql.Timestamp.valueOf("2024-01-01 10:00:00")
        };
        mockResults.add(row1);

        when(playerRepository.findPlayersWithLatestScore("NAME", 0, 50)).thenReturn(mockResults);
        when(playerRepository.count()).thenReturn(1L);

        // When
        PlayerListResponseTO result = playersService.listPlayers(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).first_name()).isEqualTo("Anna");
        assertThat(result.items().get(0).last_name()).isEqualTo("Schmidt");
        assertThat(result.items().get(0).current_total_points()).isEqualTo(100);
        assertThat(result.items().get(0).current_sequence_index()).isEqualTo(5);
        assertThat(result.paging().startIndex()).isEqualTo(0);
        assertThat(result.paging().pageSize()).isEqualTo(50);
        assertThat(result.paging().total()).isEqualTo(1L);
        assertThat(result.sort()).isEqualTo(PlayersSort.NAME);
        
        verify(playerRepository).findPlayersWithLatestScore("NAME", 0, 50);
        verify(playerRepository).count();
    }
}
