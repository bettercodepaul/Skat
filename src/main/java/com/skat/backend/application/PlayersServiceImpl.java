package com.skat.backend.application;

import com.skat.backend.api.exception.ConflictException;
import com.skat.backend.api.exception.NotFoundException;
import com.skat.backend.application.dto.*;
import com.skat.backend.domain.entities.PlayerEntity;
import com.skat.backend.domain.entities.PlayerScoreEntity;
import com.skat.backend.domain.repositories.GameRepository;
import com.skat.backend.domain.repositories.PlayerRepository;
import com.skat.backend.domain.repositories.PlayerScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlayersServiceImpl implements PlayersService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerScoreRepository playerScoreRepository;

    @Override
    @Transactional(readOnly = true)
    public PlayerListResponseTO listPlayers(PlayersQuery query) {
        Pageable pageable = PageRequest.of(query.startIndex() / query.pageSize(), query.pageSize());
        
        // Fetch players based on sort
        List<PlayerEntity> players;
        if (query.sort() == PlayersSort.NAME) {
            players = playerRepository.findAllOrderedByName(pageable);
        } else {
            players = playerRepository.findAllPlayers(pageable);
        }
        
        // Extract player IDs
        List<UUID> playerIds = players.stream()
            .map(PlayerEntity::getId)
            .collect(Collectors.toList());
        
        // Fetch latest scores for these players
        Map<UUID, PlayerScoreEntity> latestScores = new HashMap<>();
        if (!playerIds.isEmpty()) {
            List<PlayerScoreEntity> scores = playerScoreRepository.findLatestScoresForPlayers(playerIds);
            for (PlayerScoreEntity score : scores) {
                if (score.getPlayer() != null) {
                    latestScores.put(score.getPlayer().getId(), score);
                }
            }
        }
        
        // Map to DTOs
        List<PlayerWithScoreTO> items = players.stream()
            .map(player -> {
                PlayerScoreEntity score = latestScores.get(player.getId());
                int totalPoints = score != null ? score.getTotalPoints() : 0;
                int sequenceIndex = score != null ? score.getSequenceIndex() : 0;
                OffsetDateTime updatedAt = score != null ? score.getCreatedAt() : OffsetDateTime.now();
                
                return new PlayerWithScoreTO(
                    player.getId(),
                    player.getFirstName(),
                    player.getLastName(),
                    totalPoints,
                    sequenceIndex,
                    updatedAt
                );
            })
            .collect(Collectors.toList());
        
        // Sort by score if needed
        if (query.sort() == PlayersSort.SCORE_DESC) {
            items.sort((a, b) -> {
                int scoreCompare = Integer.compare(b.current_total_points(), a.current_total_points());
                if (scoreCompare != 0) return scoreCompare;
                int lastNameCompare = a.last_name().compareTo(b.last_name());
                if (lastNameCompare != 0) return lastNameCompare;
                return a.first_name().compareTo(b.first_name());
            });
        }
        
        long total = playerRepository.count();
        PagingTO paging = new PagingTO(query.startIndex(), query.pageSize(), total);
        
        return new PlayerListResponseTO(items, paging, query.sort());
    }

    @Override
    @Transactional
    public PlayerTO createPlayer(UpsertPlayerRequest request) {
        String firstName = request.first_name().trim();
        String lastName = request.last_name().trim();
        
        if (playerRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCase(firstName, lastName)) {
            throw new ConflictException(
                "Player with first_name+last_name already exists",
                "first_name,last_name"
            );
        }
        
        PlayerEntity player = new PlayerEntity(firstName, lastName);
        player = playerRepository.save(player);
        
        return new PlayerTO(player.getId(), player.getFirstName(), player.getLastName());
    }

    @Override
    @Transactional
    public PlayerTO updatePlayer(UUID id, UpsertPlayerRequest request) {
        PlayerEntity player = playerRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Player not found", "id"));
        
        String firstName = request.first_name().trim();
        String lastName = request.last_name().trim();
        
        // Check if the new name conflicts with another player
        if (playerRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndIdNot(firstName, lastName, id)) {
            throw new ConflictException(
                "Player with first_name+last_name already exists",
                "first_name,last_name"
            );
        }
        
        player.setFirstName(firstName);
        player.setLastName(lastName);
        player = playerRepository.save(player);
        
        return new PlayerTO(player.getId(), player.getFirstName(), player.getLastName());
    }

    @Override
    @Transactional
    public void deletePlayer(UUID id, boolean forceDeletion) {
        PlayerEntity player = playerRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Player not found", "id"));
        
        if (!forceDeletion) {
            boolean hasGames = gameRepository.existsByPlayerId(id);
            boolean hasScores = playerScoreRepository.existsByPlayerId(id);
            
            if (hasGames || hasScores) {
                throw new ConflictException("Player is referenced in games or scores");
            }
            
            playerRepository.delete(player);
        } else {
            // Force deletion: nullify references first
            gameRepository.nullifyPlayer1References(id);
            gameRepository.nullifyPlayer2References(id);
            gameRepository.nullifyPlayer3References(id);
            gameRepository.nullifyMainPlayerReferences(id);
            playerScoreRepository.nullifyPlayerReferences(id);
            
            playerRepository.delete(player);
        }
    }
}
