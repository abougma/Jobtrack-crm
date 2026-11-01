package com.jobtrack.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobtrack.infrastructure.persistence.entity.ProfileExperienceJpaEntity;

public interface SpringProfileExperienceRepository extends JpaRepository<ProfileExperienceJpaEntity, UUID> {
    List<ProfileExperienceJpaEntity> findAllByUserIdOrderByStartedOnDesc(UUID userId);
    Optional<ProfileExperienceJpaEntity> findByIdAndUserId(UUID id, UUID userId);
}
