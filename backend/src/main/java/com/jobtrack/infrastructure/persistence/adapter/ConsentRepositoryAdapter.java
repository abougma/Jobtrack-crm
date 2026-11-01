package com.jobtrack.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jobtrack.application.port.ConsentRepository;
import com.jobtrack.domain.organization.OrganizationConsent;
import com.jobtrack.infrastructure.persistence.repository.SpringConsentRepository;

@Component
public class ConsentRepositoryAdapter implements ConsentRepository {
    private final SpringConsentRepository repository;

    public ConsentRepositoryAdapter(SpringConsentRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrganizationConsent save(OrganizationConsent consent) {
        return DomainMapper.toDomain(repository.save(DomainMapper.toEntity(consent)));
    }

    @Override
    public Optional<OrganizationConsent> findByOrganizationIdAndUserId(UUID organizationId, UUID userId) {
        return repository.findByOrganizationIdAndUserId(organizationId, userId).map(DomainMapper::toDomain);
    }

    @Override
    public List<OrganizationConsent> findActiveByOrganizationId(UUID organizationId) {
        return repository.findAllByOrganizationIdAndRevokedAtIsNull(organizationId).stream()
                .map(DomainMapper::toDomain).toList();
    }

    @Override
    public List<OrganizationConsent> findAllByUserId(UUID userId) {
        return repository.findAllByUserId(userId).stream().map(DomainMapper::toDomain).toList();
    }
}
