package com.jobtrack.infrastructure.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobtrack.domain.plan.Plan;
import com.jobtrack.infrastructure.persistence.entity.UserPlanJpaEntity;

public interface SpringUserPlanRepository extends JpaRepository<UserPlanJpaEntity, UUID> {
    List<UserPlanJpaEntity> findAllByUserIdIn(Collection<UUID> userIds);
    long countByPlan(Plan plan);
}
