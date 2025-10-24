package com.skat.backend.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "player",
	uniqueConstraints = {
			@UniqueConstraint(name = "player_first_last_name_UQ", columnNames = { "first_name", "last_name" })
	},
	indexes = {
			@Index(name = "player_first_name_IDX", columnList = "first_name"),
			@Index(name = "player_last_name_IDX", columnList = "last_name")
	})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "first_name", nullable = false, length = 50)
	private String firstName;

	@Column(name = "last_name", nullable = false, length = 50)
	private String lastName;

	public PlayerEntity(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}
}
