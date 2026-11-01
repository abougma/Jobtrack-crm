package com.jobtrack.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "organizations")
public class OrganizationJpaEntity {
    @Id private UUID id;
    @Column(nullable = false) private String name;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected OrganizationJpaEntity() {}

    public OrganizationJpaEntity(UUID id, String name, Instant createdAt) {
        this.id = id; this.name = name; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public Instant getCreatedAt() { return createdAt; }
}
