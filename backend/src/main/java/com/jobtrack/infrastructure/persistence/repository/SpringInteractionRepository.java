package com.jobtrack.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobtrack.domain.crm.InteractionType;
import com.jobtrack.infrastructure.persistence.entity.InteractionJpaEntity;

public interface SpringInteractionRepository extends JpaRepository<InteractionJpaEntity, UUID> {
    List<InteractionJpaEntity> findAllByApplicationIdOrderByOccurredAtDesc(UUID applicationId);
    Optional<InteractionJpaEntity> findByIdAndApplicationId(UUID id, UUID applicationId);
    List<InteractionJpaEntity> findAllByApplicationIdIn(Collection<UUID> applicationIds);
    List<InteractionJpaEntity> findAllByApplicationIdInAndTypeAndOccurredAtAfter(
            Collection<UUID> applicationIds, InteractionType type, Instant after);
}
