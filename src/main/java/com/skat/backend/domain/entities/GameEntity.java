package com.skat.backend.domain.entities;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "game",
    indexes = {
        @Index(name = "game_main_player_IDX", columnList = "main_player_id"),
        @Index(name = "game_played_at_IDX", columnList = "played_at")
    }
)
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

    public GameEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PlayerEntity getPlayer1() {
        return player1;
    }

    public void setPlayer1(PlayerEntity player1) {
        this.player1 = player1;
    }

    public PlayerEntity getPlayer2() {
        return player2;
    }

    public void setPlayer2(PlayerEntity player2) {
        this.player2 = player2;
    }

    public PlayerEntity getPlayer3() {
        return player3;
    }

    public void setPlayer3(PlayerEntity player3) {
        this.player3 = player3;
    }

    public PlayerEntity getMainPlayer() {
        return mainPlayer;
    }

    public void setMainPlayer(PlayerEntity mainPlayer) {
        this.mainPlayer = mainPlayer;
    }

    public Integer getBidValue() {
        return bidValue;
    }

    public void setBidValue(Integer bidValue) {
        this.bidValue = bidValue;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public OffsetDateTime getPlayedAt() {
        return playedAt;
    }

    public void setPlayedAt(OffsetDateTime playedAt) {
        this.playedAt = playedAt;
    }
}
