package com.jobtrack.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "organization_groups")
public class OrganizationGroupJpaEntity {
    @Id private UUID id;
    @Column(name = "organization_id", nullable = false) private UUID organizationId;
    @Column(nullable = false) private String name;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected OrganizationGroupJpaEntity() {}

    public OrganizationGroupJpaEntity(UUID id, UUID organizationId, String name, Instant createdAt) {
        this.id = id; this.organizationId = organizationId; this.name = name; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getOrganizationId() { return organizationId; }
    public String getName() { return name; }
    public Instant getCreatedAt() { return createdAt; }
}
