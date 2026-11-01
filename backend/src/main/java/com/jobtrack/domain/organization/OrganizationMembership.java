package com.jobtrack.domain.organization;

import java.time.Instant;
import java.util.UUID;

public record OrganizationMembership(
        UUID id,
        UUID organizationId,
        UUID userId,
        OrganizationRole role,
        Instant createdAt) {

    public static OrganizationMembership create(UUID organizationId, UUID userId, OrganizationRole role) {
        return new OrganizationMembership(UUID.randomUUID(), organizationId, userId, role, Instant.now());
    }
}
