package com.jobtrack.domain.crm;

import java.time.Instant;
import java.util.UUID;

public record Contact(
        UUID id,
        UUID userId,
        String name,
        String role,
        String linkedinUrl,
        String email,
        Instant createdAt) {

    public static Contact create(UUID userId, String name, String role, String linkedinUrl, String email) {
        return new Contact(UUID.randomUUID(), userId, name, role, linkedinUrl, email, Instant.now());
    }

    public Contact update(String name, String role, String linkedinUrl, String email) {
        return new Contact(id, userId, name, role, linkedinUrl, email, createdAt);
    }
}
