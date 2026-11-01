package com.jobtrack.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.jobtrack.application.port.InvitationRepository;
import com.jobtrack.domain.organization.OrganizationInvitation;
import com.jobtrack.infrastructure.persistence.repository.SpringInvitationRepository;

@Component
public class InvitationRepositoryAdapter implements InvitationRepository {
    private final SpringInvitationRepository repository;

    public InvitationRepositoryAdapter(SpringInvitationRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrganizationInvitation save(OrganizationInvitation invitation) {
        return DomainMapper.toDomain(repository.save(DomainMapper.toEntity(invitation)));
    }

    @Override
    public Optional<OrganizationInvitation> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(DomainMapper::toDomain);
    }
}
