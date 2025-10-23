package com.skat.backend.domain.entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(
    name = "player",
    uniqueConstraints = {
        @UniqueConstraint(name = "player_first_last_name_UQ", columnNames = {"first_name", "last_name"})
    },
    indexes = {
        @Index(name = "player_first_name_IDX", columnList = "first_name"),
        @Index(name = "player_last_name_IDX", columnList = "last_name")
    }
)
public class PlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    public PlayerEntity() {
    }

    public PlayerEntity(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
