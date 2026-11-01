package com.jobtrack.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobtrack.infrastructure.persistence.entity.OrganizationGroupJpaEntity;

public interface SpringGroupRepository extends JpaRepository<OrganizationGroupJpaEntity, UUID> {
    List<OrganizationGroupJpaEntity> findAllByOrganizationIdOrderByNameAsc(UUID organizationId);
    Optional<OrganizationGroupJpaEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
