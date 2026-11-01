package com.jobtrack.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jobtrack.domain.organization.OrganizationGroup;

public interface GroupRepository {
    OrganizationGroup save(OrganizationGroup group);
    List<OrganizationGroup> findAllByOrganizationId(UUID organizationId);
    Optional<OrganizationGroup> findByIdAndOrganizationId(UUID id, UUID organizationId);
    List<UUID> findMemberIds(UUID groupId);
    void addMember(UUID groupId, UUID userId);
    void removeMember(UUID groupId, UUID userId);
    long countMembers(UUID groupId);
}
