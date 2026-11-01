package com.jobtrack.domain.application;

import java.time.Instant;
import java.util.UUID;

public record StatusChange(
        UUID id,
        UUID applicationId,
        ApplicationStatus previousStatus,
        ApplicationStatus newStatus,
        Instant changedAt) {

    public static StatusChange of(UUID applicationId, ApplicationStatus previousStatus, ApplicationStatus newStatus) {
        return new StatusChange(UUID.randomUUID(), applicationId, previousStatus, newStatus, Instant.now());
    }
}
