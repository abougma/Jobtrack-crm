package com.jobtrack.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobtrack.infrastructure.persistence.entity.UserProfileJpaEntity;

public interface SpringUserProfileRepository extends JpaRepository<UserProfileJpaEntity, UUID> {}
