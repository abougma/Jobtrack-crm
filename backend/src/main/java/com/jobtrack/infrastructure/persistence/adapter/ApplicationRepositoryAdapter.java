package com.jobtrack.infrastructure.persistence.adapter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.jobtrack.application.port.ApplicationFilter;
import com.jobtrack.application.port.ApplicationRepository;
import com.jobtrack.domain.application.Application;
import com.jobtrack.infrastructure.persistence.entity.ApplicationJpaEntity;
import com.jobtrack.infrastructure.persistence.repository.SpringApplicationRepository;

import jakarta.persistence.criteria.Predicate;

@Component
public class ApplicationRepositoryAdapter implements ApplicationRepository {
    private final SpringApplicationRepository repository;

    public ApplicationRepositoryAdapter(SpringApplicationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Application> search(UUID userId, ApplicationFilter filter, Pageable pageable) {
        ApplicationFilter effective = filter == null ? ApplicationFilter.empty() : filter;
        return repository.findAll(specification(userId, effective), pageable).map(DomainMapper::toDomain);
    }

    @Override
    public Optional<Application> findByIdAndUserId(UUID id, UUID userId) {
        return repository.findByIdAndUserId(id, userId).map(DomainMapper::toDomain);
    }

    @Override
    public List<Application> findAllByUserId(UUID userId) {
        return repository.findAllByUserId(userId).stream().map(DomainMapper::toDomain).toList();
    }

    @Override
    public List<Application> findAllByUserIdIn(Collection<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return repository.findAllByUserIdIn(userIds).stream().map(DomainMapper::toDomain).toList();
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public Application save(Application application) {
        return DomainMapper.toDomain(repository.save(DomainMapper.toEntity(application)));
    }

    @Override
    public void delete(Application application) {
        repository.deleteById(application.id());
    }

    private Specification<ApplicationJpaEntity> specification(UUID userId, ApplicationFilter filter) {
        return (root, query, builder) -> {
            Predicate predicate = builder.equal(root.get("userId"), userId);
            if (filter.status() != null) {
                predicate = builder.and(predicate, builder.equal(root.get("status"), filter.status()));
            }
            if (hasText(filter.source())) {
                predicate = builder.and(predicate,
                        builder.equal(builder.lower(root.get("source")), filter.source().trim().toLowerCase()));
            }
            if (filter.contractType() != null) {
                predicate = builder.and(predicate, builder.equal(root.get("contractType"), filter.contractType()));
            }
            if (filter.workMode() != null) {
                predicate = builder.and(predicate, builder.equal(root.get("workMode"), filter.workMode()));
            }
            if (filter.from() != null) {
                predicate = builder.and(predicate,
                        builder.greaterThanOrEqualTo(root.get("appliedAt"), filter.from()));
            }
            if (filter.to() != null) {
                predicate = builder.and(predicate, builder.lessThanOrEqualTo(root.get("appliedAt"), filter.to()));
            }
            if (hasText(filter.search())) {
                String pattern = "%" + filter.search().trim().toLowerCase() + "%";
                predicate = builder.and(predicate, builder.or(
                        builder.like(builder.lower(root.get("company")), pattern),
                        builder.like(builder.lower(root.get("position")), pattern),
                        builder.like(builder.lower(root.get("location")), pattern),
                        builder.like(builder.lower(root.get("notes")), pattern)));
            }
            return predicate;
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
