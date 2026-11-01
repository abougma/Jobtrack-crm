package com.jobtrack.application.port;

import java.util.Optional;

import com.jobtrack.domain.user.RefreshToken;

public interface RefreshTokenRepository {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    RefreshToken save(RefreshToken token);
    void deleteAllForUser(java.util.UUID userId);
}
