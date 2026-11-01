package com.jobtrack.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.jobtrack.domain.user.UserRole;
import com.jobtrack.infrastructure.persistence.entity.UserJpaEntity;

public interface SpringUserRepository
        extends JpaRepository<UserJpaEntity, UUID>, JpaSpecificationExecutor<UserJpaEntity> {
    Optional<UserJpaEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRole(UserRole role);
    long countByCreatedAtAfter(Instant instant);
}
