package com.jobtrack.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "user_profiles")
public class UserProfileJpaEntity {
    @Id @Column(name = "user_id") private UUID userId;
    @Column(name = "first_name") private String firstName;
    @Column(name = "last_name") private String lastName;
    private String headline;
    private String location;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected UserProfileJpaEntity() {}

    public UserProfileJpaEntity(UUID userId, String firstName, String lastName, String headline,
                                String location, Instant updatedAt) {
        this.userId = userId; this.firstName = firstName; this.lastName = lastName;
        this.headline = headline; this.location = location; this.updatedAt = updatedAt;
    }

    public UUID getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getHeadline() { return headline; }
    public String getLocation() { return location; }
    public Instant getUpdatedAt() { return updatedAt; }
}
