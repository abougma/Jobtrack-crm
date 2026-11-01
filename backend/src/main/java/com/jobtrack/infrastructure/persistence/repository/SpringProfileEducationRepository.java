package com.jobtrack.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobtrack.infrastructure.persistence.entity.ProfileEducationJpaEntity;

public interface SpringProfileEducationRepository extends JpaRepository<ProfileEducationJpaEntity, UUID> {
    List<ProfileEducationJpaEntity> findAllByUserIdOrderByStartedOnDesc(UUID userId);
    Optional<ProfileEducationJpaEntity> findByIdAndUserId(UUID id, UUID userId);
}
