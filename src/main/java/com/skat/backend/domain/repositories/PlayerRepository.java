package com.skat.backend.domain.repositories;

import com.skat.backend.domain.entities.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerEntity, UUID> {

    boolean existsByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);

    boolean existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndIdNot(String firstName, String lastName, UUID id);

    @Query(value = """
        SELECT p.id, p.first_name, p.last_name,
               COALESCE(ps.total_points, 0) as current_total_points,
               COALESCE(ps.sequence_index, 0) as current_sequence_index,
               COALESCE(ps.created_at, CURRENT_TIMESTAMP) as updated_at
        FROM player p
        LEFT JOIN LATERAL (
            SELECT player_id, total_points, sequence_index, created_at
            FROM player_score
            WHERE player_id = p.id
            ORDER BY sequence_index DESC
            LIMIT 1
        ) ps ON true
        ORDER BY
            CASE WHEN :sort = 'SCORE_DESC' THEN COALESCE(ps.total_points, 0) END DESC,
            CASE WHEN :sort = 'NAME' THEN p.last_name END ASC,
            CASE WHEN :sort = 'SCORE_DESC' THEN p.last_name END ASC,
            p.first_name ASC
        OFFSET :offset ROWS
        FETCH FIRST :limit ROWS ONLY
        """, nativeQuery = true)
    List<Object[]> findPlayersWithLatestScore(@Param("sort") String sort, @Param("offset") int offset, @Param("limit") int limit);
}
