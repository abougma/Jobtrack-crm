package com.jobtrack.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobtrack.infrastructure.persistence.entity.OrganizationInvitationJpaEntity;

public interface SpringInvitationRepository extends JpaRepository<OrganizationInvitationJpaEntity, UUID> {
    Optional<OrganizationInvitationJpaEntity> findByTokenHash(String tokenHash);
}
