package com.jobtrack.domain.organization;

import java.time.Instant;
import java.util.UUID;

public record Organization(UUID id, String name, Instant createdAt) {

    public static Organization create(String name) {
        return new Organization(UUID.randomUUID(), name, Instant.now());
    }
}
