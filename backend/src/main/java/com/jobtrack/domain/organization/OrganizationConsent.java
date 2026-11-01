package com.jobtrack.domain.organization;

import java.time.Instant;
import java.util.UUID;

public record OrganizationConsent(
        UUID id,
        UUID organizationId,
        UUID userId,
        ConsentScope scope,
        Instant grantedAt,
        Instant revokedAt) {

    public static OrganizationConsent grant(UUID organizationId, UUID userId, ConsentScope scope) {
        return new OrganizationConsent(UUID.randomUUID(), organizationId, userId, scope, Instant.now(), null);
    }

    public boolean isActive() {
        return revokedAt == null;
    }

    public OrganizationConsent updateScope(ConsentScope newScope) {
        return new OrganizationConsent(id, organizationId, userId, newScope, Instant.now(), null);
    }

    public OrganizationConsent revoke() {
        return new OrganizationConsent(id, organizationId, userId, scope, grantedAt, Instant.now());
    }
}
