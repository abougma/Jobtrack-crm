package com.jobtrack.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import com.jobtrack.domain.application.ApplicationStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "application_status_history")
public class ApplicationStatusHistoryJpaEntity {
    @Id private UUID id;
    @Column(name = "application_id", nullable = false) private UUID applicationId;
    @Enumerated(EnumType.STRING) @Column(name = "previous_status", length = 32) private ApplicationStatus previousStatus;
    @Enumerated(EnumType.STRING) @Column(name = "new_status", nullable = false, length = 32) private ApplicationStatus newStatus;
    @Column(name = "changed_at", nullable = false) private Instant changedAt;

    protected ApplicationStatusHistoryJpaEntity() {}

    public ApplicationStatusHistoryJpaEntity(UUID id, UUID applicationId, ApplicationStatus previousStatus,
                                             ApplicationStatus newStatus, Instant changedAt) {
        this.id = id; this.applicationId = applicationId; this.previousStatus = previousStatus;
        this.newStatus = newStatus; this.changedAt = changedAt;
    }

    public UUID getId() { return id; }
    public UUID getApplicationId() { return applicationId; }
    public ApplicationStatus getPreviousStatus() { return previousStatus; }
    public ApplicationStatus getNewStatus() { return newStatus; }
    public Instant getChangedAt() { return changedAt; }
}
