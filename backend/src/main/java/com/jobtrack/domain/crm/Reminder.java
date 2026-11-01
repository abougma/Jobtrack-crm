package com.jobtrack.domain.crm;

import java.time.Instant;
import java.util.UUID;

public record Reminder(
        UUID id,
        UUID applicationId,
        String title,
        Instant dueAt,
        boolean completed) {

    public static Reminder create(UUID applicationId, String title, Instant dueAt) {
        return new Reminder(UUID.randomUUID(), applicationId, title, dueAt, false);
    }

    public Reminder complete() {
        return new Reminder(id, applicationId, title, dueAt, true);
    }
}
