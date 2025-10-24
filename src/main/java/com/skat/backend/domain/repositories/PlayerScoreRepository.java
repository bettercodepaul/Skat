package com.skat.backend.domain.repositories;

import com.skat.backend.domain.entities.PlayerScoreEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerScoreRepository extends JpaRepository<PlayerScoreEntity, UUID> {

	@Query("SELECT CASE WHEN COUNT(ps) > 0 THEN true ELSE false END FROM PlayerScoreEntity ps " +
		"WHERE ps.player.id = :playerId")
	boolean existsByPlayerId(@Param("playerId") UUID playerId);

	@Modifying
	@Query("UPDATE PlayerScoreEntity ps SET ps.player = NULL WHERE ps.player.id = :playerId")
	void nullifyPlayerReferences(@Param("playerId") UUID playerId);

	@Query(value = """
		SELECT ps.*
		FROM player_score ps
		INNER JOIN (
		    SELECT player_id, MAX(sequence_index) as max_seq
		    FROM player_score
		    WHERE player_id IN :playerIds
		    GROUP BY player_id
		) latest ON ps.player_id = latest.player_id AND ps.sequence_index = latest.max_seq
		""", nativeQuery = true)
	List<PlayerScoreEntity> findLatestScoresForPlayers(@Param("playerIds") List<UUID> playerIds);
}
