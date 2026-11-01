package com.jobtrack.domain.crm;

import java.time.Instant;
import java.util.UUID;

public record Interaction(
        UUID id,
        UUID applicationId,
        InteractionType type,
        String message,
        Instant occurredAt) {

    public static Interaction create(UUID applicationId, InteractionType type, String message, Instant occurredAt) {
        return new Interaction(UUID.randomUUID(), applicationId, type, message, occurredAt);
    }
}
