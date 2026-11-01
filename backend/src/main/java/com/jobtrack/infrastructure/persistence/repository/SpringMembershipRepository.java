package com.jobtrack.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobtrack.infrastructure.persistence.entity.OrganizationMembershipJpaEntity;

public interface SpringMembershipRepository extends JpaRepository<OrganizationMembershipJpaEntity, UUID> {
    Optional<OrganizationMembershipJpaEntity> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);
    List<OrganizationMembershipJpaEntity> findAllByUserId(UUID userId);
    List<OrganizationMembershipJpaEntity> findAllByOrganizationIdOrderByCreatedAtAsc(UUID organizationId);
    long countByOrganizationId(UUID organizationId);
}
