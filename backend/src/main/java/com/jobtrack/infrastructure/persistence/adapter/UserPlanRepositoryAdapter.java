package com.jobtrack.infrastructure.persistence.adapter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jobtrack.application.port.UserPlanRepository;
import com.jobtrack.domain.plan.Plan;
import com.jobtrack.domain.plan.UserPlan;
import com.jobtrack.infrastructure.persistence.repository.SpringUserPlanRepository;

@Component
public class UserPlanRepositoryAdapter implements UserPlanRepository {
    private final SpringUserPlanRepository repository;

    public UserPlanRepositoryAdapter(SpringUserPlanRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<UserPlan> findById(UUID userId) {
        return repository.findById(userId).map(DomainMapper::toDomain);
    }

    @Override
    public List<UserPlan> findAllByIdIn(Collection<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return repository.findAllById(userIds).stream().map(DomainMapper::toDomain).toList();
    }

    @Override
    public long countByPlan(Plan plan) {
        return repository.countByPlan(plan);
    }

    @Override
    public UserPlan save(UserPlan plan) {
        return DomainMapper.toDomain(repository.save(DomainMapper.toEntity(plan)));
    }
}
