package com.jobtrack.infrastructure.persistence.adapter;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.jobtrack.application.port.OrganizationRepository;
import com.jobtrack.domain.organization.Organization;
import com.jobtrack.infrastructure.persistence.entity.OrganizationJpaEntity;
import com.jobtrack.infrastructure.persistence.repository.SpringOrganizationRepository;

@Component
public class OrganizationRepositoryAdapter implements OrganizationRepository {
    private final SpringOrganizationRepository repository;

    public OrganizationRepositoryAdapter(SpringOrganizationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Organization save(Organization organization) {
        return DomainMapper.toDomain(repository.save(DomainMapper.toEntity(organization)));
    }

    @Override
    public Optional<Organization> findById(UUID id) {
        return repository.findById(id).map(DomainMapper::toDomain);
    }

    @Override
    public List<Organization> findAllByIdIn(Collection<UUID> ids) {
        return repository.findAllByIdIn(ids).stream().map(DomainMapper::toDomain).toList();
    }

    @Override
    public Page<Organization> search(String query, Pageable pageable) {
        Specification<OrganizationJpaEntity> specification = (root, criteriaQuery, cb) -> cb.conjunction();
        if (query != null && !query.isBlank()) {
            String pattern = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, criteriaQuery, cb) ->
                    cb.like(cb.lower(root.get("name")), pattern));
        }
        return repository.findAll(specification, pageable).map(DomainMapper::toDomain);
    }

    @Override
    public long count() {
        return repository.count();
    }
}
