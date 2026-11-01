package com.jobtrack.domain.organization;

import java.time.Instant;
import java.util.UUID;

public record OrganizationGroup(UUID id, UUID organizationId, String name, Instant createdAt) {

    public static OrganizationGroup create(UUID organizationId, String name) {
        return new OrganizationGroup(UUID.randomUUID(), organizationId, name, Instant.now());
    }
}
