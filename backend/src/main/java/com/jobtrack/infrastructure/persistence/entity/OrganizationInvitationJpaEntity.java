package com.jobtrack.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import com.jobtrack.domain.organization.OrganizationRole;
import jakarta.persistence.*;

@Entity
@Table(name = "organization_invitations")
public class OrganizationInvitationJpaEntity {
    @Id private UUID id;
    @Column(name = "organization_id", nullable = false) private UUID organizationId;
    @Column(nullable = false) private String email;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private OrganizationRole role;
    @Column(name = "token_hash", nullable = false, unique = true, length = 64) private String tokenHash;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "accepted_at") private Instant acceptedAt;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected OrganizationInvitationJpaEntity() {}

    public OrganizationInvitationJpaEntity(UUID id, UUID organizationId, String email, OrganizationRole role,
                                           String tokenHash, Instant expiresAt, Instant acceptedAt,
                                           UUID createdBy, Instant createdAt) {
        this.id = id; this.organizationId = organizationId; this.email = email; this.role = role;
        this.tokenHash = tokenHash; this.expiresAt = expiresAt; this.acceptedAt = acceptedAt;
        this.createdBy = createdBy; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getOrganizationId() { return organizationId; }
    public String getEmail() { return email; }
    public OrganizationRole getRole() { return role; }
    public String getTokenHash() { return tokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getAcceptedAt() { return acceptedAt; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
}
