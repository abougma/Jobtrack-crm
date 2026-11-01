package com.jobtrack.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jobtrack.application.port.MembershipRepository;
import com.jobtrack.domain.organization.OrganizationMembership;
import com.jobtrack.infrastructure.persistence.repository.SpringMembershipRepository;

@Component
public class MembershipRepositoryAdapter implements MembershipRepository {
    private final SpringMembershipRepository repository;

    public MembershipRepositoryAdapter(SpringMembershipRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrganizationMembership save(OrganizationMembership membership) {
        return DomainMapper.toDomain(repository.save(DomainMapper.toEntity(membership)));
    }

    @Override
    public Optional<OrganizationMembership> findByOrganizationIdAndUserId(UUID organizationId, UUID userId) {
        return repository.findByOrganizationIdAndUserId(organizationId, userId).map(DomainMapper::toDomain);
    }

    @Override
    public List<OrganizationMembership> findAllByUserId(UUID userId) {
        return repository.findAllByUserId(userId).stream().map(DomainMapper::toDomain).toList();
    }

    @Override
    public List<OrganizationMembership> findAllByOrganizationId(UUID organizationId) {
        return repository.findAllByOrganizationIdOrderByCreatedAtAsc(organizationId).stream()
                .map(DomainMapper::toDomain).toList();
    }

    @Override
    public long countByOrganizationId(UUID organizationId) {
        return repository.countByOrganizationId(organizationId);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public void delete(OrganizationMembership membership) {
        repository.deleteById(membership.id());
    }
}
