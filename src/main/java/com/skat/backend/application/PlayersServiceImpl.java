package com.skat.backend.application;

import com.skat.backend.api.exception.ConflictException;
import com.skat.backend.api.exception.NotFoundException;
import com.skat.backend.application.dto.PagingTO;
import com.skat.backend.application.dto.PlayerListResponseTO;
import com.skat.backend.application.dto.PlayerTO;
import com.skat.backend.application.dto.PlayerWithScoreTO;
import com.skat.backend.application.dto.PlayersQuery;
import com.skat.backend.application.dto.PlayersSort;
import com.skat.backend.application.dto.UpsertPlayerRequest;
import com.skat.backend.domain.entities.PlayerEntity;
import com.skat.backend.domain.entities.PlayerScoreEntity;
import com.skat.backend.domain.repositories.GameRepository;
import com.skat.backend.domain.repositories.PlayerRepository;
import com.skat.backend.domain.repositories.PlayerScoreRepository;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
		var pageable = PageRequest.of(query.startIndex() / query.pageSize(), query.pageSize());

		// Fetch players based on sort
		List<PlayerEntity> players;
		if (query.sort() == PlayersSort.NAME) {
			players = playerRepository.findAllOrderedByName(pageable);
		} else {
			players = playerRepository.findAllPlayers(pageable);
		}

		// Extract player IDs
		var playerIds = players.stream()
			.map(PlayerEntity::getId)
			.collect(Collectors.toList());

		// Fetch latest scores for these players
		var latestScores = new HashMap<UUID, PlayerScoreEntity>();
		if (!playerIds.isEmpty()) {
			var scores = playerScoreRepository.findLatestScoresForPlayers(playerIds);
			for (var score : scores) {
				if (score.getPlayer() != null) {
					latestScores.put(score.getPlayer().getId(), score);
				}
			}
		}

		// Map to DTOs
		var items = players.stream()
			.map(player -> {
				var score = latestScores.get(player.getId());
				var totalPoints = score != null ? score.getTotalPoints() : 0;
				var sequenceIndex = score != null ? score.getSequenceIndex() : 0;
				var updatedAt = score != null ? score.getCreatedAt() : OffsetDateTime.now();

				return new PlayerWithScoreTO(
					player.getId(),
					player.getFirstName(),
					player.getLastName(),
					totalPoints,
					sequenceIndex,
					updatedAt);
			})
			.collect(Collectors.toList());

		// Sort by score if needed
		if (query.sort() == PlayersSort.SCORE_DESC) {
			items.sort((a, b) -> {
				var scoreCompare = Integer.compare(b.current_total_points(), a.current_total_points());
				if (scoreCompare != 0)
					return scoreCompare;
				var lastNameCompare = a.last_name().compareTo(b.last_name());
				if (lastNameCompare != 0)
					return lastNameCompare;
				return a.first_name().compareTo(b.first_name());
			});
		}

		var total = playerRepository.count();
		var paging = new PagingTO(query.startIndex(), query.pageSize(), total);

		return new PlayerListResponseTO(items, paging, query.sort());
	}

	@Override
	@Transactional
	public PlayerTO createPlayer(UpsertPlayerRequest request) {
		var firstName = request.first_name().trim();
		var lastName = request.last_name().trim();

		if (playerRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCase(firstName, lastName)) {
			throw new ConflictException(
				"Player with first_name+last_name already exists",
				"first_name,last_name");
		}

		var player = new PlayerEntity(firstName, lastName);
		player = playerRepository.save(player);

		return new PlayerTO(player.getId(), player.getFirstName(), player.getLastName());
	}

	@Override
	@Transactional
	public PlayerTO updatePlayer(UUID id, UpsertPlayerRequest request) {
		var player = playerRepository.findById(id)
			.orElseThrow(() -> new NotFoundException("Player not found", "id"));

		var firstName = request.first_name().trim();
		var lastName = request.last_name().trim();

		// Check if the new name conflicts with another player
		if (playerRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndIdNot(firstName, lastName, id)) {
			throw new ConflictException(
				"Player with first_name+last_name already exists",
				"first_name,last_name");
		}

		player.setFirstName(firstName);
		player.setLastName(lastName);
		player = playerRepository.save(player);

		return new PlayerTO(player.getId(), player.getFirstName(), player.getLastName());
	}

	@Override
	@Transactional
	public void deletePlayer(UUID id, boolean forceDeletion) {
		var player = playerRepository.findById(id)
			.orElseThrow(() -> new NotFoundException("Player not found", "id"));

		if (!forceDeletion) {
			var hasGames = gameRepository.existsByPlayerId(id);
			var hasScores = playerScoreRepository.existsByPlayerId(id);

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
