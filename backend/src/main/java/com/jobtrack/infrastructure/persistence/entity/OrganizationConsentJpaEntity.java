package com.jobtrack.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import com.jobtrack.domain.organization.ConsentScope;
import jakarta.persistence.*;

@Entity
@Table(name = "organization_consents")
public class OrganizationConsentJpaEntity {
    @Id private UUID id;
    @Column(name = "organization_id", nullable = false) private UUID organizationId;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private ConsentScope scope;
    @Column(name = "granted_at", nullable = false) private Instant grantedAt;
    @Column(name = "revoked_at") private Instant revokedAt;

    protected OrganizationConsentJpaEntity() {}

    public OrganizationConsentJpaEntity(UUID id, UUID organizationId, UUID userId, ConsentScope scope,
                                        Instant grantedAt, Instant revokedAt) {
        this.id = id; this.organizationId = organizationId; this.userId = userId;
        this.scope = scope; this.grantedAt = grantedAt; this.revokedAt = revokedAt;
    }

    public UUID getId() { return id; }
    public UUID getOrganizationId() { return organizationId; }
    public UUID getUserId() { return userId; }
    public ConsentScope getScope() { return scope; }
    public Instant getGrantedAt() { return grantedAt; }
    public Instant getRevokedAt() { return revokedAt; }
}
