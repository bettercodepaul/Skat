package com.skat.backend.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "player_score",
	indexes = {
			@Index(name = "player_score_player_IDX", columnList = "player_id"),
			@Index(name = "player_score_game_IDX", columnList = "game_id"),
			@Index(name = "player_score_sequence_IDX", columnList = "sequence_index")
	})
@Data
@NoArgsConstructor
public class PlayerScoreEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "player_id")
	private PlayerEntity player;

	@ManyToOne
	@JoinColumn(name = "game_id", nullable = false)
	private GameEntity game;

	@Column(name = "sequence_index", nullable = false)
	private Integer sequenceIndex;

	@Column(name = "total_points")
	private Integer totalPoints;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;
}
