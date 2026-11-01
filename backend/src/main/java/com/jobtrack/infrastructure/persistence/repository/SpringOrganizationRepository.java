package com.jobtrack.infrastructure.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.jobtrack.infrastructure.persistence.entity.OrganizationJpaEntity;

public interface SpringOrganizationRepository
        extends JpaRepository<OrganizationJpaEntity, UUID>, JpaSpecificationExecutor<OrganizationJpaEntity> {
    List<OrganizationJpaEntity> findAllByIdIn(Collection<UUID> ids);
}
