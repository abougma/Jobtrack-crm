package com.jobtrack.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobtrack.infrastructure.persistence.entity.GeneratedCvJpaEntity;

public interface SpringGeneratedCvRepository extends JpaRepository<GeneratedCvJpaEntity, UUID> {
    List<GeneratedCvJpaEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<GeneratedCvJpaEntity> findByIdAndUserId(UUID id, UUID userId);
    Optional<GeneratedCvJpaEntity> findFirstByApplicationIdAndUserIdOrderByCreatedAtDesc(UUID applicationId, UUID userId);
}
