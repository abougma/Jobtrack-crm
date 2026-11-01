package com.jobtrack.infrastructure.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobtrack.infrastructure.persistence.entity.ReminderJpaEntity;

public interface SpringReminderRepository extends JpaRepository<ReminderJpaEntity, UUID> {
    List<ReminderJpaEntity> findAllByApplicationIdOrderByDueAtAsc(UUID applicationId);
    Optional<ReminderJpaEntity> findByIdAndApplicationId(UUID id, UUID applicationId);
    List<ReminderJpaEntity> findAllByApplicationIdInOrderByDueAtAsc(Collection<UUID> applicationIds);
}
