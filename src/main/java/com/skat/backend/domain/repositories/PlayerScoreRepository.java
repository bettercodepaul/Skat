package com.skat.backend.domain.repositories;

import com.skat.backend.domain.entities.PlayerScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlayerScoreRepository extends JpaRepository<PlayerScoreEntity, UUID> {

    @Query("SELECT CASE WHEN COUNT(ps) > 0 THEN true ELSE false END FROM PlayerScoreEntity ps " +
           "WHERE ps.player.id = :playerId")
    boolean existsByPlayerId(@Param("playerId") UUID playerId);

    @Modifying
    @Query("UPDATE PlayerScoreEntity ps SET ps.player = NULL WHERE ps.player.id = :playerId")
    void nullifyPlayerReferences(@Param("playerId") UUID playerId);
}
