package com.jobtrack.domain.user;

import java.time.Instant;
import java.util.UUID;

public record RefreshToken(
        UUID id,
        UUID userId,
        String tokenHash,
        Instant expiresAt,
        Instant revokedAt) {

    public static RefreshToken create(UUID userId, String tokenHash, Instant expiresAt) {
        return new RefreshToken(UUID.randomUUID(), userId, tokenHash, expiresAt, null);
    }

    public boolean isUsable(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }

    public RefreshToken revoke() {
        return new RefreshToken(id, userId, tokenHash, expiresAt, Instant.now());
    }
}
