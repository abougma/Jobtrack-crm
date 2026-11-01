package com.jobtrack.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenJpaEntity {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "token_hash", nullable = false, unique = true, length = 64) private String tokenHash;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "revoked_at") private Instant revokedAt;

    protected RefreshTokenJpaEntity() {}

    public RefreshTokenJpaEntity(UUID id, UUID userId, String tokenHash, Instant expiresAt, Instant revokedAt) {
        this.id = id; this.userId = userId; this.tokenHash = tokenHash;
        this.expiresAt = expiresAt; this.revokedAt = revokedAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getTokenHash() { return tokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getRevokedAt() { return revokedAt; }
}
