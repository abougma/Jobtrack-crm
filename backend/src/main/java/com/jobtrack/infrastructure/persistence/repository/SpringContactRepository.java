package com.jobtrack.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobtrack.infrastructure.persistence.entity.ContactJpaEntity;

public interface SpringContactRepository extends JpaRepository<ContactJpaEntity, UUID> {
    List<ContactJpaEntity> findAllByUserIdOrderByNameAsc(UUID userId);
    Optional<ContactJpaEntity> findByIdAndUserId(UUID id, UUID userId);
}
