package com.skat.backend.domain.entities;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "player_score",
    indexes = {
        @Index(name = "player_score_player_IDX", columnList = "player_id"),
        @Index(name = "player_score_game_IDX", columnList = "game_id"),
        @Index(name = "player_score_sequence_IDX", columnList = "sequence_index")
    }
)
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

    public PlayerScoreEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(PlayerEntity player) {
        this.player = player;
    }

    public GameEntity getGame() {
        return game;
    }

    public void setGame(GameEntity game) {
        this.game = game;
    }

    public Integer getSequenceIndex() {
        return sequenceIndex;
    }

    public void setSequenceIndex(Integer sequenceIndex) {
        this.sequenceIndex = sequenceIndex;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
