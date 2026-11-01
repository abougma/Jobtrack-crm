package com.jobtrack.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobtrack.infrastructure.persistence.entity.OrganizationConsentJpaEntity;

public interface SpringConsentRepository extends JpaRepository<OrganizationConsentJpaEntity, UUID> {
    Optional<OrganizationConsentJpaEntity> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);
    List<OrganizationConsentJpaEntity> findAllByOrganizationIdAndRevokedAtIsNull(UUID organizationId);
    List<OrganizationConsentJpaEntity> findAllByUserId(UUID userId);
}
