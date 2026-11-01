package com.jobtrack.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jobtrack.domain.organization.OrganizationConsent;

public interface ConsentRepository {
    OrganizationConsent save(OrganizationConsent consent);
    Optional<OrganizationConsent> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);
    List<OrganizationConsent> findActiveByOrganizationId(UUID organizationId);
    List<OrganizationConsent> findAllByUserId(UUID userId);
}
