package com.jobtrack.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobtrack.infrastructure.persistence.entity.ProfileSkillJpaEntity;

public interface SpringProfileSkillRepository extends JpaRepository<ProfileSkillJpaEntity, UUID> {
    List<ProfileSkillJpaEntity> findAllByUserIdOrderByNameAsc(UUID userId);
    Optional<ProfileSkillJpaEntity> findByIdAndUserId(UUID id, UUID userId);
}
