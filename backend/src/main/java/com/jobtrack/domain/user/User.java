package com.jobtrack.domain.user;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record User(
        UUID id,
        String email,
        String passwordHash,
        UserRole role,
        Instant createdAt,
        Instant updatedAt) {

    public User {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(email, "email");
        Objects.requireNonNull(passwordHash, "passwordHash");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static User create(String email, String passwordHash) {
        Instant now = Instant.now();
        return new User(UUID.randomUUID(), email, passwordHash, UserRole.JOB_SEEKER, now, now);
    }

    public User withRole(UserRole newRole) {
        return new User(id, email, passwordHash, newRole, createdAt, Instant.now());
    }
}
