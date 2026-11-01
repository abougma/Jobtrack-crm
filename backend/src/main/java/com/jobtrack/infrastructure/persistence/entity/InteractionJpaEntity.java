package com.jobtrack.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import com.jobtrack.domain.crm.InteractionType;
import jakarta.persistence.*;

@Entity
@Table(name = "interactions")
public class InteractionJpaEntity {
    @Id private UUID id;
    @Column(name = "application_id", nullable = false) private UUID applicationId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private InteractionType type;
    @Column(columnDefinition = "TEXT") private String message;
    @Column(name = "occurred_at", nullable = false) private Instant occurredAt;

    protected InteractionJpaEntity() {}

    public InteractionJpaEntity(UUID id, UUID applicationId, InteractionType type, String message, Instant occurredAt) {
        this.id = id; this.applicationId = applicationId; this.type = type;
        this.message = message; this.occurredAt = occurredAt;
    }

    public UUID getId() { return id; }
    public UUID getApplicationId() { return applicationId; }
    public InteractionType getType() { return type; }
    public String getMessage() { return message; }
    public Instant getOccurredAt() { return occurredAt; }
}
