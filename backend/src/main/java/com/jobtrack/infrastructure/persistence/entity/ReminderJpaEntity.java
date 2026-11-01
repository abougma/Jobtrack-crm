package com.jobtrack.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "reminders")
public class ReminderJpaEntity {
    @Id private UUID id;
    @Column(name = "application_id", nullable = false) private UUID applicationId;
    @Column(nullable = false) private String title;
    @Column(name = "due_at", nullable = false) private Instant dueAt;
    @Column(nullable = false) private boolean completed;

    protected ReminderJpaEntity() {}

    public ReminderJpaEntity(UUID id, UUID applicationId, String title, Instant dueAt, boolean completed) {
        this.id = id; this.applicationId = applicationId; this.title = title;
        this.dueAt = dueAt; this.completed = completed;
    }

    public UUID getId() { return id; }
    public UUID getApplicationId() { return applicationId; }
    public String getTitle() { return title; }
    public Instant getDueAt() { return dueAt; }
    public boolean isCompleted() { return completed; }
}
