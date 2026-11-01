package com.jobtrack.infrastructure.persistence.adapter;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jobtrack.application.port.StatusHistoryRepository;
import com.jobtrack.domain.application.StatusChange;
import com.jobtrack.infrastructure.persistence.repository.SpringStatusHistoryRepository;

@Component
public class StatusHistoryRepositoryAdapter implements StatusHistoryRepository {
    private final SpringStatusHistoryRepository repository;

    public StatusHistoryRepositoryAdapter(SpringStatusHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<StatusChange> findAllByApplicationId(UUID applicationId) {
        return repository.findAllByApplicationIdOrderByChangedAtAsc(applicationId).stream()
                .map(DomainMapper::toDomain).toList();
    }

    @Override
    public List<StatusChange> findAllByApplicationIdIn(Collection<UUID> applicationIds) {
        if (applicationIds == null || applicationIds.isEmpty()) {
            return List.of();
        }
        return repository.findAllByApplicationIdIn(applicationIds).stream().map(DomainMapper::toDomain).toList();
    }

    @Override
    public StatusChange save(StatusChange change) {
        return DomainMapper.toDomain(repository.save(DomainMapper.toEntity(change)));
    }
}
