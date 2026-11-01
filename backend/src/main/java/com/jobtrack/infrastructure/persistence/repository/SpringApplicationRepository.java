package com.jobtrack.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.jobtrack.infrastructure.persistence.entity.ApplicationJpaEntity;

public interface SpringApplicationRepository
        extends JpaRepository<ApplicationJpaEntity, UUID>, JpaSpecificationExecutor<ApplicationJpaEntity> {
    List<ApplicationJpaEntity> findAllByUserId(UUID userId);
    List<ApplicationJpaEntity> findAllByUserIdIn(java.util.Collection<UUID> userIds);
    Optional<ApplicationJpaEntity> findByIdAndUserId(UUID id, UUID userId);
}
