package com.jobtrack.infrastructure.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobtrack.infrastructure.persistence.entity.ApplicationStatusHistoryJpaEntity;

public interface SpringStatusHistoryRepository extends JpaRepository<ApplicationStatusHistoryJpaEntity, UUID> {
    List<ApplicationStatusHistoryJpaEntity> findAllByApplicationIdOrderByChangedAtAsc(UUID applicationId);
    List<ApplicationStatusHistoryJpaEntity> findAllByApplicationIdIn(Collection<UUID> applicationIds);
}
