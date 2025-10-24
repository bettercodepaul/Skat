package com.skat.backend.api.controller;

import com.skat.backend.application.dto.ErrorResponseTO;
import com.skat.backend.application.dto.PlayerListResponseTO;
import com.skat.backend.application.dto.PlayerTO;
import com.skat.backend.domain.entities.GameEntity;
import com.skat.backend.domain.entities.PlayerEntity;
import com.skat.backend.domain.entities.PlayerScoreEntity;
import com.skat.backend.domain.repositories.GameRepository;
import com.skat.backend.domain.repositories.PlayerRepository;
import com.skat.backend.domain.repositories.PlayerScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for PlayersController following ADR-001 and ADR-008.
 * Uses Testcontainers for PostgreSQL 18 and TestRestTemplate for testing the full application context.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
    private TestRestTemplate restTemplate;

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
    void given_existingPlayers_when_listPlayersWithoutParameters_then_returns200WithDefaultPaging() {
        // Given
        var player1 = new PlayerEntity("Anna", "Schmidt");
        var player2 = new PlayerEntity("Max", "Mueller");
        playerRepository.save(player1);
        playerRepository.save(player2);

        // When
        var response = restTemplate.getForEntity("/api/players", PlayerListResponseTO.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().items()).hasSize(2);
        assertThat(response.getBody().paging().startIndex()).isEqualTo(0);
        assertThat(response.getBody().paging().pageSize()).isEqualTo(50);
        assertThat(response.getBody().paging().total()).isEqualTo(2);
        assertThat(response.getBody().sort().name()).isEqualTo("NAME");
    }

    // AC-2: List players — score_desc ordering
    @Test
    void given_playersWithDifferentScores_when_listPlayersSortedByScore_then_returnsOrderedByScoreDesc() {
        // Given
        var player1 = new PlayerEntity("Anna", "Schmidt");
        var player2 = new PlayerEntity("Max", "Mueller");
        var player3 = new PlayerEntity("Lisa", "Bauer");
        player1 = playerRepository.save(player1);
        player2 = playerRepository.save(player2);
        player3 = playerRepository.save(player3);

        var game = new GameEntity();
        game.setPlayedAt(OffsetDateTime.now());
        game = gameRepository.save(game);

        // Create scores for players
        var score1 = new PlayerScoreEntity();
        score1.setPlayer(player1);
        score1.setGame(game);
        score1.setSequenceIndex(0);
        score1.setTotalPoints(100);
        score1.setCreatedAt(OffsetDateTime.now());
        playerScoreRepository.save(score1);

        var score2 = new PlayerScoreEntity();
        score2.setPlayer(player2);
        score2.setGame(game);
        score2.setSequenceIndex(0);
        score2.setTotalPoints(200);
        score2.setCreatedAt(OffsetDateTime.now());
        playerScoreRepository.save(score2);

        var score3 = new PlayerScoreEntity();
        score3.setPlayer(player3);
        score3.setGame(game);
        score3.setSequenceIndex(0);
        score3.setTotalPoints(150);
        score3.setCreatedAt(OffsetDateTime.now());
        playerScoreRepository.save(score3);

        // When
        var response = restTemplate.getForEntity("/api/players?sort=SCORE_DESC", PlayerListResponseTO.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().items()).hasSize(3);
        assertThat(response.getBody().items().get(0).first_name()).isEqualTo("Max");
        assertThat(response.getBody().items().get(0).current_total_points()).isEqualTo(200);
        assertThat(response.getBody().items().get(1).first_name()).isEqualTo("Lisa");
        assertThat(response.getBody().items().get(1).current_total_points()).isEqualTo(150);
        assertThat(response.getBody().items().get(2).first_name()).isEqualTo("Anna");
        assertThat(response.getBody().items().get(2).current_total_points()).isEqualTo(100);
        assertThat(response.getBody().sort().name()).isEqualTo("SCORE_DESC");
    }

    // AC-3: List players — parameter validation
    @Test
    void given_invalidPageSize_when_listPlayers_then_returns400() {
        // When
        var response = restTemplate.getForEntity("/api/players?pageSize=0", ErrorResponseTO.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("bad_request");
        assertThat(response.getBody().field()).isEqualTo("pageSize");
    }

    @Test
    void given_negativeStartIndex_when_listPlayers_then_returns400() {
        // When
        var response = restTemplate.getForEntity("/api/players?startIndex=-1", ErrorResponseTO.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("bad_request");
        assertThat(response.getBody().field()).isEqualTo("startIndex");
    }

    @Test
    void given_pageSizeTooLarge_when_listPlayers_then_returns400() {
        // When
        var response = restTemplate.getForEntity("/api/players?pageSize=201", ErrorResponseTO.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("bad_request");
        assertThat(response.getBody().field()).isEqualTo("pageSize");
    }

    // AC-4: Create player — unique full name
    @Test
    void given_uniquePlayerName_when_createPlayer_then_returns201WithLocation() {
        // Given
        var requestBody = """
            {
                "first_name": "Anna",
                "last_name": "Schmidt"
            }
            """;
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(requestBody, headers);

        // When
        var response = restTemplate.postForEntity("/api/players", request, PlayerTO.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().first_name()).isEqualTo("Anna");
        assertThat(response.getBody().last_name()).isEqualTo("Schmidt");

        // Verify player was created
        assertThat(playerRepository.findAll()).hasSize(1);
    }

    // AC-5: Create player — conflict on duplicate full name
    @Test
    void given_existingPlayerName_when_createPlayerWithSameName_then_returns409() {
        // Given
        var existingPlayer = new PlayerEntity("Anna", "Schmidt");
        playerRepository.save(existingPlayer);

        var requestBody = """
            {
                "first_name": "Anna",
                "last_name": "Schmidt"
            }
            """;
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(requestBody, headers);

        // When
        var response = restTemplate.postForEntity("/api/players", request, ErrorResponseTO.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("conflict");
        assertThat(response.getBody().field()).isEqualTo("first_name,last_name");
    }

    @Test
    void given_missingFirstName_when_createPlayer_then_returns400() {
        // Given
        var requestBody = """
            {
                "last_name": "Schmidt"
            }
            """;
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(requestBody, headers);

        // When
        var response = restTemplate.postForEntity("/api/players", request, ErrorResponseTO.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("bad_request");
    }

    @Test
    void given_blankFirstName_when_createPlayer_then_returns400() {
        // Given
        var requestBody = """
            {
                "first_name": "   ",
                "last_name": "Schmidt"
            }
            """;
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(requestBody, headers);

        // When
        var response = restTemplate.postForEntity("/api/players", request, ErrorResponseTO.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("bad_request");
    }

    @Test
    void given_nameTooLong_when_createPlayer_then_returns400() {
        // Given
        var longName = "a".repeat(51);
        var requestBody = String.format("""
            {
                "first_name": "%s",
                "last_name": "Schmidt"
            }
            """, longName);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(requestBody, headers);

        // When
        var response = restTemplate.postForEntity("/api/players", request, ErrorResponseTO.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("bad_request");
    }

    // AC-6: Update player — id must exist
    @Test
    void given_nonExistentPlayerId_when_updatePlayer_then_returns404() {
        // Given
        var nonExistentId = UUID.randomUUID();
        var requestBody = """
            {
                "first_name": "Anna",
                "last_name": "Bauer"
            }
            """;
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(requestBody, headers);

        // When
        var response = restTemplate.exchange(
            "/api/players/" + nonExistentId,
            HttpMethod.PUT,
            request,
            ErrorResponseTO.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("not_found");
        assertThat(response.getBody().field()).isEqualTo("id");
    }

    // AC-7: Update player — uniqueness enforced
    @Test
    void given_twoPlayers_when_updatePlayerToExistingName_then_returns409() {
        // Given
        var player1 = new PlayerEntity("Anna", "Schmidt");
        var player2 = new PlayerEntity("Anna", "Mueller");
        player1 = playerRepository.save(player1);
        player2 = playerRepository.save(player2);

        var requestBody = """
            {
                "first_name": "Anna",
                "last_name": "Schmidt"
            }
            """;
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(requestBody, headers);

        // When
        var response = restTemplate.exchange(
            "/api/players/" + player2.getId(),
            HttpMethod.PUT,
            request,
            ErrorResponseTO.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("conflict");
        assertThat(response.getBody().field()).isEqualTo("first_name,last_name");
    }

    @Test
    void given_existingPlayer_when_updatePlayerToNewName_then_returns200() {
        // Given
        var player = new PlayerEntity("Anna", "Schmidt");
        player = playerRepository.save(player);

        var requestBody = """
            {
                "first_name": "Anna",
                "last_name": "Mueller"
            }
            """;
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(requestBody, headers);

        // When
        var response = restTemplate.exchange(
            "/api/players/" + player.getId(),
            HttpMethod.PUT,
            request,
            PlayerTO.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(player.getId());
        assertThat(response.getBody().first_name()).isEqualTo("Anna");
        assertThat(response.getBody().last_name()).isEqualTo("Mueller");

        // Verify player was updated
        var updatedPlayer = playerRepository.findById(player.getId()).orElseThrow();
        assertThat(updatedPlayer.getLastName()).isEqualTo("Mueller");
    }

    // AC-8: Delete player — safe delete without references
    @Test
    void given_playerWithoutReferences_when_deletePlayerWithoutForce_then_returns204() {
        // Given
        var player = new PlayerEntity("Anna", "Schmidt");
        player = playerRepository.save(player);

        // When
        var response = restTemplate.exchange(
            "/api/players/" + player.getId() + "?forceDeletion=false",
            HttpMethod.DELETE,
            null,
            Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify player was deleted
        assertThat(playerRepository.findById(player.getId())).isEmpty();
    }

    // AC-9: Delete player — conflict when referenced
    @Test
    void given_playerReferencedInGame_when_deletePlayerWithoutForce_then_returns409() {
        // Given
        var player = new PlayerEntity("Anna", "Schmidt");
        player = playerRepository.save(player);

        var game = new GameEntity();
        game.setPlayer1(player);
        game.setPlayedAt(OffsetDateTime.now());
        gameRepository.save(game);

        // When
        var response = restTemplate.exchange(
            "/api/players/" + player.getId() + "?forceDeletion=false",
            HttpMethod.DELETE,
            null,
            ErrorResponseTO.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("conflict");

        // Verify player was not deleted
        assertThat(playerRepository.findById(player.getId())).isPresent();
    }

    @Test
    void given_playerReferencedInScore_when_deletePlayerWithoutForce_then_returns409() {
        // Given
        var player = new PlayerEntity("Anna", "Schmidt");
        player = playerRepository.save(player);

        var game = new GameEntity();
        game.setPlayedAt(OffsetDateTime.now());
        game = gameRepository.save(game);

        var score = new PlayerScoreEntity();
        score.setPlayer(player);
        score.setGame(game);
        score.setSequenceIndex(0);
        score.setTotalPoints(100);
        score.setCreatedAt(OffsetDateTime.now());
        playerScoreRepository.save(score);

        // When
        var response = restTemplate.exchange(
            "/api/players/" + player.getId() + "?forceDeletion=false",
            HttpMethod.DELETE,
            null,
            ErrorResponseTO.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("conflict");

        // Verify player was not deleted
        assertThat(playerRepository.findById(player.getId())).isPresent();
    }

    // AC-10: Delete player — forced deletion nullifies references
    @Test
    void given_playerReferencedInGameAndScore_when_forceDeletePlayer_then_returns204AndNullifies() {
        // Given
        var player = new PlayerEntity("Anna", "Schmidt");
        player = playerRepository.save(player);

        var game = new GameEntity();
        game.setPlayer1(player);
        game.setPlayer2(player);
        game.setMainPlayer(player);
        game.setPlayedAt(OffsetDateTime.now());
        game = gameRepository.save(game);

        var score = new PlayerScoreEntity();
        score.setPlayer(player);
        score.setGame(game);
        score.setSequenceIndex(0);
        score.setTotalPoints(100);
        score.setCreatedAt(OffsetDateTime.now());
        score = playerScoreRepository.save(score);

        var gameId = game.getId();
        var scoreId = score.getId();

        // When
        var response = restTemplate.exchange(
            "/api/players/" + player.getId() + "?forceDeletion=true",
            HttpMethod.DELETE,
            null,
            Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify player was deleted
        assertThat(playerRepository.findById(player.getId())).isEmpty();

        // Verify references were nullified
        var updatedGame = gameRepository.findById(gameId).orElseThrow();
        assertThat(updatedGame.getPlayer1()).isNull();
        assertThat(updatedGame.getPlayer2()).isNull();
        assertThat(updatedGame.getMainPlayer()).isNull();

        var updatedScore = playerScoreRepository.findById(scoreId).orElseThrow();
        assertThat(updatedScore.getPlayer()).isNull();
    }

    @Test
    void given_nonExistentPlayer_when_deletePlayer_then_returns404() {
        // Given
        var nonExistentId = UUID.randomUUID();

        // When
        var response = restTemplate.exchange(
            "/api/players/" + nonExistentId + "?forceDeletion=false",
            HttpMethod.DELETE,
            null,
            ErrorResponseTO.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("not_found");
    }

    @Test
    void given_newPlayerWithoutScores_when_listPlayers_then_returnsPlayerWithZeroScore() {
        // Given
        var player = new PlayerEntity("Anna", "Schmidt");
        playerRepository.save(player);

        // When
        var response = restTemplate.getForEntity("/api/players", PlayerListResponseTO.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().items()).hasSize(1);
        assertThat(response.getBody().items().get(0).first_name()).isEqualTo("Anna");
        assertThat(response.getBody().items().get(0).current_total_points()).isEqualTo(0);
        assertThat(response.getBody().items().get(0).current_sequence_index()).isEqualTo(0);
    }
}
