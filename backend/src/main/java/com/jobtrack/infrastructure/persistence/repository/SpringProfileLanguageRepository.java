package com.jobtrack.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobtrack.infrastructure.persistence.entity.ProfileLanguageJpaEntity;

public interface SpringProfileLanguageRepository extends JpaRepository<ProfileLanguageJpaEntity, UUID> {
    List<ProfileLanguageJpaEntity> findAllByUserIdOrderByNameAsc(UUID userId);
    Optional<ProfileLanguageJpaEntity> findByIdAndUserId(UUID id, UUID userId);
}
