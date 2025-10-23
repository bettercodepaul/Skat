package com.skat.backend.api.controller;

import com.skat.backend.domain.entities.GameEntity;
import com.skat.backend.domain.entities.PlayerEntity;
import com.skat.backend.domain.entities.PlayerScoreEntity;
import com.skat.backend.domain.repositories.GameRepository;
import com.skat.backend.domain.repositories.PlayerRepository;
import com.skat.backend.domain.repositories.PlayerScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for PlayersController following ADR-001 (Integration Testing Strategy with Testcontainers).
 * Uses Testcontainers for PostgreSQL 18 and tests the full Spring Boot application.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PlayersControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18")
        .withDatabaseName("skatdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerScoreRepository playerScoreRepository;

    @BeforeEach
    void setUp() {
        playerScoreRepository.deleteAll();
        gameRepository.deleteAll();
        playerRepository.deleteAll();
    }

    // AC-1: List players — default sorting and paging
    @Test
    void given_existingPlayers_when_listPlayersWithoutParameters_then_returns200WithDefaultPaging() throws Exception {
        // Given
        PlayerEntity player1 = new PlayerEntity("Anna", "Schmidt");
        PlayerEntity player2 = new PlayerEntity("Max", "Mueller");
        playerRepository.save(player1);
        playerRepository.save(player2);

        // When & Then
        mockMvc.perform(get("/api/players"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(2)))
            .andExpect(jsonPath("$.paging.startIndex", is(0)))
            .andExpect(jsonPath("$.paging.pageSize", is(50)))
            .andExpect(jsonPath("$.paging.total", is(2)))
            .andExpect(jsonPath("$.sort", is("NAME")));
    }

    // AC-2: List players — score_desc ordering
    @Test
    void given_playersWithDifferentScores_when_listPlayersSortedByScore_then_returnsOrderedByScoreDesc() throws Exception {
        // Given
        PlayerEntity player1 = new PlayerEntity("Anna", "Schmidt");
        PlayerEntity player2 = new PlayerEntity("Max", "Mueller");
        PlayerEntity player3 = new PlayerEntity("Lisa", "Bauer");
        player1 = playerRepository.save(player1);
        player2 = playerRepository.save(player2);
        player3 = playerRepository.save(player3);

        GameEntity game = new GameEntity();
        game.setPlayedAt(OffsetDateTime.now());
        game = gameRepository.save(game);

        // Create scores for players
        PlayerScoreEntity score1 = new PlayerScoreEntity();
        score1.setPlayer(player1);
        score1.setGame(game);
        score1.setSequenceIndex(0);
        score1.setTotalPoints(100);
        score1.setCreatedAt(OffsetDateTime.now());
        playerScoreRepository.save(score1);

        PlayerScoreEntity score2 = new PlayerScoreEntity();
        score2.setPlayer(player2);
        score2.setGame(game);
        score2.setSequenceIndex(0);
        score2.setTotalPoints(200);
        score2.setCreatedAt(OffsetDateTime.now());
        playerScoreRepository.save(score2);

        PlayerScoreEntity score3 = new PlayerScoreEntity();
        score3.setPlayer(player3);
        score3.setGame(game);
        score3.setSequenceIndex(0);
        score3.setTotalPoints(150);
        score3.setCreatedAt(OffsetDateTime.now());
        playerScoreRepository.save(score3);

        // When & Then
        mockMvc.perform(get("/api/players")
                .param("sort", "SCORE_DESC"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(3)))
            .andExpect(jsonPath("$.items[0].first_name", is("Max")))
            .andExpect(jsonPath("$.items[0].current_total_points", is(200)))
            .andExpect(jsonPath("$.items[1].first_name", is("Lisa")))
            .andExpect(jsonPath("$.items[1].current_total_points", is(150)))
            .andExpect(jsonPath("$.items[2].first_name", is("Anna")))
            .andExpect(jsonPath("$.items[2].current_total_points", is(100)))
            .andExpect(jsonPath("$.sort", is("SCORE_DESC")));
    }

    // AC-3: List players — parameter validation
    @Test
    void given_invalidPageSize_when_listPlayers_then_returns400() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/players")
                .param("pageSize", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", is("bad_request")))
            .andExpect(jsonPath("$.field", is("pageSize")));
    }

    @Test
    void given_negativeStartIndex_when_listPlayers_then_returns400() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/players")
                .param("startIndex", "-1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", is("bad_request")))
            .andExpect(jsonPath("$.field", is("startIndex")));
    }

    @Test
    void given_pageSizeTooLarge_when_listPlayers_then_returns400() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/players")
                .param("pageSize", "201"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", is("bad_request")))
            .andExpect(jsonPath("$.field", is("pageSize")));
    }

    // AC-4: Create player — unique full name
    @Test
    void given_uniquePlayerName_when_createPlayer_then_returns201WithLocation() throws Exception {
        // Given
        String requestBody = """
            {
                "first_name": "Anna",
                "last_name": "Schmidt"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/players")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.first_name", is("Anna")))
            .andExpect(jsonPath("$.last_name", is("Schmidt")));

        // Verify player was created
        assertThat(playerRepository.findAll()).hasSize(1);
    }

    // AC-5: Create player — conflict on duplicate full name
    @Test
    void given_existingPlayerName_when_createPlayerWithSameName_then_returns409() throws Exception {
        // Given
        PlayerEntity existingPlayer = new PlayerEntity("Anna", "Schmidt");
        playerRepository.save(existingPlayer);

        String requestBody = """
            {
                "first_name": "Anna",
                "last_name": "Schmidt"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/players")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error", is("conflict")))
            .andExpect(jsonPath("$.field", is("first_name,last_name")));
    }

    @Test
    void given_missingFirstName_when_createPlayer_then_returns400() throws Exception {
        // Given
        String requestBody = """
            {
                "last_name": "Schmidt"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/players")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", is("bad_request")));
    }

    @Test
    void given_blankFirstName_when_createPlayer_then_returns400() throws Exception {
        // Given
        String requestBody = """
            {
                "first_name": "   ",
                "last_name": "Schmidt"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/players")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", is("bad_request")));
    }

    @Test
    void given_nameTooLong_when_createPlayer_then_returns400() throws Exception {
        // Given
        String longName = "a".repeat(51);
        String requestBody = String.format("""
            {
                "first_name": "%s",
                "last_name": "Schmidt"
            }
            """, longName);

        // When & Then
        mockMvc.perform(post("/api/players")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", is("bad_request")));
    }

    // AC-6: Update player — id must exist
    @Test
    void given_nonExistentPlayerId_when_updatePlayer_then_returns404() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        String requestBody = """
            {
                "first_name": "Anna",
                "last_name": "Bauer"
            }
            """;

        // When & Then
        mockMvc.perform(put("/api/players/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error", is("not_found")))
            .andExpect(jsonPath("$.field", is("id")));
    }

    // AC-7: Update player — uniqueness enforced
    @Test
    void given_twoPlayers_when_updatePlayerToExistingName_then_returns409() throws Exception {
        // Given
        PlayerEntity player1 = new PlayerEntity("Anna", "Schmidt");
        PlayerEntity player2 = new PlayerEntity("Anna", "Mueller");
        player1 = playerRepository.save(player1);
        player2 = playerRepository.save(player2);

        String requestBody = """
            {
                "first_name": "Anna",
                "last_name": "Schmidt"
            }
            """;

        // When & Then
        mockMvc.perform(put("/api/players/" + player2.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error", is("conflict")))
            .andExpect(jsonPath("$.field", is("first_name,last_name")));
    }

    @Test
    void given_existingPlayer_when_updatePlayerToNewName_then_returns200() throws Exception {
        // Given
        PlayerEntity player = new PlayerEntity("Anna", "Schmidt");
        player = playerRepository.save(player);

        String requestBody = """
            {
                "first_name": "Anna",
                "last_name": "Mueller"
            }
            """;

        // When & Then
        mockMvc.perform(put("/api/players/" + player.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(player.getId().toString())))
            .andExpect(jsonPath("$.first_name", is("Anna")))
            .andExpect(jsonPath("$.last_name", is("Mueller")));

        // Verify player was updated
        PlayerEntity updatedPlayer = playerRepository.findById(player.getId()).orElseThrow();
        assertThat(updatedPlayer.getLastName()).isEqualTo("Mueller");
    }

    // AC-8: Delete player — safe delete without references
    @Test
    void given_playerWithoutReferences_when_deletePlayerWithoutForce_then_returns204() throws Exception {
        // Given
        PlayerEntity player = new PlayerEntity("Anna", "Schmidt");
        player = playerRepository.save(player);

        // When & Then
        mockMvc.perform(delete("/api/players/" + player.getId())
                .param("forceDeletion", "false"))
            .andExpect(status().isNoContent());

        // Verify player was deleted
        assertThat(playerRepository.findById(player.getId())).isEmpty();
    }

    // AC-9: Delete player — conflict when referenced
    @Test
    void given_playerReferencedInGame_when_deletePlayerWithoutForce_then_returns409() throws Exception {
        // Given
        PlayerEntity player = new PlayerEntity("Anna", "Schmidt");
        player = playerRepository.save(player);

        GameEntity game = new GameEntity();
        game.setPlayer1(player);
        game.setPlayedAt(OffsetDateTime.now());
        gameRepository.save(game);

        // When & Then
        mockMvc.perform(delete("/api/players/" + player.getId())
                .param("forceDeletion", "false"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error", is("conflict")));

        // Verify player was not deleted
        assertThat(playerRepository.findById(player.getId())).isPresent();
    }

    @Test
    void given_playerReferencedInScore_when_deletePlayerWithoutForce_then_returns409() throws Exception {
        // Given
        PlayerEntity player = new PlayerEntity("Anna", "Schmidt");
        player = playerRepository.save(player);

        GameEntity game = new GameEntity();
        game.setPlayedAt(OffsetDateTime.now());
        game = gameRepository.save(game);

        PlayerScoreEntity score = new PlayerScoreEntity();
        score.setPlayer(player);
        score.setGame(game);
        score.setSequenceIndex(0);
        score.setTotalPoints(100);
        score.setCreatedAt(OffsetDateTime.now());
        playerScoreRepository.save(score);

        // When & Then
        mockMvc.perform(delete("/api/players/" + player.getId())
                .param("forceDeletion", "false"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error", is("conflict")));

        // Verify player was not deleted
        assertThat(playerRepository.findById(player.getId())).isPresent();
    }

    // AC-10: Delete player — forced deletion nullifies references
    @Test
    void given_playerReferencedInGameAndScore_when_forceDeletePlayer_then_returns204AndNullifies() throws Exception {
        // Given
        PlayerEntity player = new PlayerEntity("Anna", "Schmidt");
        player = playerRepository.save(player);

        GameEntity game = new GameEntity();
        game.setPlayer1(player);
        game.setPlayer2(player);
        game.setMainPlayer(player);
        game.setPlayedAt(OffsetDateTime.now());
        game = gameRepository.save(game);

        PlayerScoreEntity score = new PlayerScoreEntity();
        score.setPlayer(player);
        score.setGame(game);
        score.setSequenceIndex(0);
        score.setTotalPoints(100);
        score.setCreatedAt(OffsetDateTime.now());
        score = playerScoreRepository.save(score);

        UUID gameId = game.getId();
        UUID scoreId = score.getId();

        // When & Then
        mockMvc.perform(delete("/api/players/" + player.getId())
                .param("forceDeletion", "true"))
            .andExpect(status().isNoContent());

        // Verify player was deleted
        assertThat(playerRepository.findById(player.getId())).isEmpty();

        // Verify references were nullified
        GameEntity updatedGame = gameRepository.findById(gameId).orElseThrow();
        assertThat(updatedGame.getPlayer1()).isNull();
        assertThat(updatedGame.getPlayer2()).isNull();
        assertThat(updatedGame.getMainPlayer()).isNull();

        PlayerScoreEntity updatedScore = playerScoreRepository.findById(scoreId).orElseThrow();
        assertThat(updatedScore.getPlayer()).isNull();
    }

    @Test
    void given_nonExistentPlayer_when_deletePlayer_then_returns404() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(delete("/api/players/" + nonExistentId)
                .param("forceDeletion", "false"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error", is("not_found")));
    }

    @Test
    void given_newPlayerWithoutScores_when_listPlayers_then_returnsPlayerWithZeroScore() throws Exception {
        // Given
        PlayerEntity player = new PlayerEntity("Anna", "Schmidt");
        playerRepository.save(player);

        // When & Then
        mockMvc.perform(get("/api/players"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(1)))
            .andExpect(jsonPath("$.items[0].first_name", is("Anna")))
            .andExpect(jsonPath("$.items[0].current_total_points", is(0)))
            .andExpect(jsonPath("$.items[0].current_sequence_index", is(0)));
    }
}
