package com.jobtrack.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import com.jobtrack.domain.user.UserRole;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserJpaEntity {
    @Id private UUID id;
    @Column(nullable = false, unique = true) private String email;
    @Column(name = "password_hash", nullable = false) private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private UserRole role;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected UserJpaEntity() {}

    public UserJpaEntity(UUID id, String email, String passwordHash, UserRole role,
                         Instant createdAt, Instant updatedAt) {
        this.id = id; this.email = email; this.passwordHash = passwordHash; this.role = role;
        this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole() { return role; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
