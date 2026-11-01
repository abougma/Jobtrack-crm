package com.jobtrack.infrastructure.persistence.adapter;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.jobtrack.application.port.UserRepository;
import com.jobtrack.domain.plan.Plan;
import com.jobtrack.domain.user.User;
import com.jobtrack.domain.user.UserRole;
import com.jobtrack.infrastructure.persistence.entity.UserJpaEntity;
import com.jobtrack.infrastructure.persistence.entity.UserPlanJpaEntity;
import com.jobtrack.infrastructure.persistence.repository.SpringUserRepository;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

@Component
public class UserRepositoryAdapter implements UserRepository {
    private final SpringUserRepository repository;

    public UserRepositoryAdapter(SpringUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id).map(DomainMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(DomainMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public Page<User> search(String query, UserRole role, Plan plan, Pageable pageable) {
        return repository.findAll(specification(query, role, plan), pageable).map(DomainMapper::toDomain);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public long countByRole(UserRole role) {
        return repository.countByRole(role);
    }

    @Override
    public long countByCreatedAtAfter(Instant instant) {
        return repository.countByCreatedAtAfter(instant);
    }

    @Override
    public User save(User user) {
        return DomainMapper.toDomain(repository.save(DomainMapper.toEntity(user)));
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private Specification<UserJpaEntity> specification(String query, UserRole role, Plan plan) {
        Specification<UserJpaEntity> specification = (root, criteriaQuery, cb) -> cb.conjunction();
        if (query != null && !query.isBlank()) {
            String pattern = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, criteriaQuery, cb) ->
                    cb.like(cb.lower(root.get("email")), pattern));
        }
        if (role != null) {
            specification = specification.and((root, criteriaQuery, cb) ->
                    cb.equal(root.get("role"), role));
        }
        if (plan != null) {
            specification = specification.and((root, criteriaQuery, cb) -> {
                Subquery<UUID> subquery = criteriaQuery.subquery(UUID.class);
                Root<UserPlanJpaEntity> planRoot = subquery.from(UserPlanJpaEntity.class);
                subquery.select(planRoot.get("userId")).where(cb.equal(planRoot.get("plan"), plan));
                return cb.in(root.get("id")).value(subquery);
            });
        }
        return specification;
    }
}
