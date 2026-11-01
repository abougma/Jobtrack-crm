package com.jobtrack.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import com.jobtrack.domain.organization.OrganizationRole;
import jakarta.persistence.*;

@Entity
@Table(name = "organization_memberships")
public class OrganizationMembershipJpaEntity {
    @Id private UUID id;
    @Column(name = "organization_id", nullable = false) private UUID organizationId;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private OrganizationRole role;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected OrganizationMembershipJpaEntity() {}

    public OrganizationMembershipJpaEntity(UUID id, UUID organizationId, UUID userId,
                                           OrganizationRole role, Instant createdAt) {
        this.id = id; this.organizationId = organizationId; this.userId = userId;
        this.role = role; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getOrganizationId() { return organizationId; }
    public UUID getUserId() { return userId; }
    public OrganizationRole getRole() { return role; }
    public Instant getCreatedAt() { return createdAt; }
}
