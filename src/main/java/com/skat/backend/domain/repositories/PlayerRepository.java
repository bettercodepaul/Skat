package com.skat.backend.domain.repositories;

import com.skat.backend.domain.entities.PlayerEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerEntity, UUID> {

	boolean existsByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);

	boolean existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndIdNot(String firstName, String lastName, UUID id);

	@Query("SELECT p FROM PlayerEntity p ORDER BY p.lastName ASC, p.firstName ASC")
	List<PlayerEntity> findAllOrderedByName(Pageable pageable);

	@Query("SELECT p FROM PlayerEntity p")
	List<PlayerEntity> findAllPlayers(Pageable pageable);
}
