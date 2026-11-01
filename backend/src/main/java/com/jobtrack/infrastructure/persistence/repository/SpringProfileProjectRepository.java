package com.jobtrack.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobtrack.infrastructure.persistence.entity.ProfileProjectJpaEntity;

public interface SpringProfileProjectRepository extends JpaRepository<ProfileProjectJpaEntity, UUID> {
    List<ProfileProjectJpaEntity> findAllByUserIdOrderByNameAsc(UUID userId);
    Optional<ProfileProjectJpaEntity> findByIdAndUserId(UUID id, UUID userId);
}
