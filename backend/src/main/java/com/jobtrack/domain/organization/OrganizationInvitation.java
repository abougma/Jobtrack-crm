package com.jobtrack.domain.organization;

import java.time.Instant;
import java.util.UUID;

public record OrganizationInvitation(
        UUID id,
        UUID organizationId,
        String email,
        OrganizationRole role,
        String tokenHash,
        Instant expiresAt,
        Instant acceptedAt,
        UUID createdBy,
        Instant createdAt) {

    public static OrganizationInvitation create(UUID organizationId, String email, OrganizationRole role,
                                                String tokenHash, Instant expiresAt, UUID createdBy) {
        return new OrganizationInvitation(UUID.randomUUID(), organizationId, email, role, tokenHash,
                expiresAt, null, createdBy, Instant.now());
    }

    public boolean isUsable(Instant now) {
        return acceptedAt == null && expiresAt.isAfter(now);
    }

    public OrganizationInvitation accept() {
        return new OrganizationInvitation(id, organizationId, email, role, tokenHash, expiresAt,
                Instant.now(), createdBy, createdAt);
    }
}
