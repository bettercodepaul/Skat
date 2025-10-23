package com.skat.backend.application;

import com.skat.backend.api.exception.ConflictException;
import com.skat.backend.api.exception.NotFoundException;
import com.skat.backend.application.dto.*;
import com.skat.backend.domain.entities.PlayerEntity;
import com.skat.backend.domain.repositories.GameRepository;
import com.skat.backend.domain.repositories.PlayerRepository;
import com.skat.backend.domain.repositories.PlayerScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PlayersServiceImpl implements PlayersService {

    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;
    private final PlayerScoreRepository playerScoreRepository;

    public PlayersServiceImpl(PlayerRepository playerRepository,
                              GameRepository gameRepository,
                              PlayerScoreRepository playerScoreRepository) {
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
        this.playerScoreRepository = playerScoreRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PlayerListResponseTO listPlayers(PlayersQuery query) {
        String sortValue = query.sort().name();
        List<Object[]> results = playerRepository.findPlayersWithLatestScore(
            sortValue, 
            query.startIndex(), 
            query.pageSize()
        );
        
        List<PlayerWithScoreTO> items = new ArrayList<>();
        for (Object[] row : results) {
            UUID id = row[0] instanceof UUID ? (UUID) row[0] : UUID.fromString(row[0].toString());
            String firstName = (String) row[1];
            String lastName = (String) row[2];
            int totalPoints = row[3] instanceof Number ? ((Number) row[3]).intValue() : 0;
            int sequenceIndex = row[4] instanceof Number ? ((Number) row[4]).intValue() : 0;
            
            OffsetDateTime updatedAt;
            if (row[5] instanceof Timestamp) {
                updatedAt = ((Timestamp) row[5]).toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
            } else if (row[5] instanceof OffsetDateTime) {
                updatedAt = (OffsetDateTime) row[5];
            } else {
                updatedAt = OffsetDateTime.now();
            }
            
            items.add(new PlayerWithScoreTO(id, firstName, lastName, totalPoints, sequenceIndex, updatedAt));
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
