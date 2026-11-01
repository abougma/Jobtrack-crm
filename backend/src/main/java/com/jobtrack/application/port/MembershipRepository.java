package com.jobtrack.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jobtrack.domain.organization.OrganizationMembership;

public interface MembershipRepository {
    OrganizationMembership save(OrganizationMembership membership);
    Optional<OrganizationMembership> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);
    List<OrganizationMembership> findAllByUserId(UUID userId);
    List<OrganizationMembership> findAllByOrganizationId(UUID organizationId);
    long countByOrganizationId(UUID organizationId);
    long count();
    void delete(OrganizationMembership membership);
}
