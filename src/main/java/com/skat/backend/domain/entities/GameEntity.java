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
	name = "game",
	indexes = {
			@Index(name = "game_main_player_IDX", columnList = "main_player_id"),
			@Index(name = "game_played_at_IDX", columnList = "played_at")
	})
@Data
@NoArgsConstructor
public class GameEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "player1_id")
	private PlayerEntity player1;

	@ManyToOne
	@JoinColumn(name = "player2_id")
	private PlayerEntity player2;

	@ManyToOne
	@JoinColumn(name = "player3_id")
	private PlayerEntity player3;

	@ManyToOne
	@JoinColumn(name = "main_player_id")
	private PlayerEntity mainPlayer;

	@Column(name = "bid_value")
	private Integer bidValue;

	@Column(name = "score")
	private Integer score;

	@Column(name = "played_at", nullable = false)
	private OffsetDateTime playedAt;
}
