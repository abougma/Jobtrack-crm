package com.jobtrack.application.port;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jobtrack.domain.plan.Plan;
import com.jobtrack.domain.plan.UserPlan;

public interface UserPlanRepository {
    Optional<UserPlan> findById(UUID userId);
    List<UserPlan> findAllByIdIn(Collection<UUID> userIds);
    long countByPlan(Plan plan);
    UserPlan save(UserPlan plan);
}
