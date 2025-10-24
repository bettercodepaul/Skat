package com.skat.backend.domain.repositories;

import com.skat.backend.domain.entities.GameEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<GameEntity, UUID> {

	@Query("""
		SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM GameEntity g \
		WHERE g.player1.id = :playerId OR g.player2.id = :playerId \
		OR g.player3.id = :playerId OR g.mainPlayer.id = :playerId""")
	boolean existsByPlayerId(@Param("playerId") UUID playerId);

	@Modifying
	@Query("UPDATE GameEntity g SET g.player1 = NULL WHERE g.player1.id = :playerId")
	void nullifyPlayer1References(@Param("playerId") UUID playerId);

	@Modifying
	@Query("UPDATE GameEntity g SET g.player2 = NULL WHERE g.player2.id = :playerId")
	void nullifyPlayer2References(@Param("playerId") UUID playerId);

	@Modifying
	@Query("UPDATE GameEntity g SET g.player3 = NULL WHERE g.player3.id = :playerId")
	void nullifyPlayer3References(@Param("playerId") UUID playerId);

	@Modifying
	@Query("UPDATE GameEntity g SET g.mainPlayer = NULL WHERE g.mainPlayer.id = :playerId")
	void nullifyMainPlayerReferences(@Param("playerId") UUID playerId);
}
