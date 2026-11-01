package com.jobtrack.infrastructure.persistence.adapter;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.jobtrack.application.port.InteractionRepository;
import com.jobtrack.domain.crm.Interaction;
import com.jobtrack.domain.crm.InteractionType;
import com.jobtrack.infrastructure.persistence.repository.SpringInteractionRepository;

@Component
public class InteractionRepositoryAdapter implements InteractionRepository {
    private final SpringInteractionRepository repository;

    public InteractionRepositoryAdapter(SpringInteractionRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Interaction> findAllByApplicationId(UUID applicationId) {
        return repository.findAllByApplicationIdOrderByOccurredAtDesc(applicationId).stream()
                .map(DomainMapper::toDomain).toList();
    }

    @Override
    public Optional<Interaction> findByIdAndApplicationId(UUID id, UUID applicationId) {
        return repository.findByIdAndApplicationId(id, applicationId).map(DomainMapper::toDomain);
    }

    @Override
    public Map<UUID, Long> countByApplicationIds(Collection<UUID> applicationIds) {
        if (applicationIds == null || applicationIds.isEmpty()) {
            return Map.of();
        }
        return repository.findAllByApplicationIdIn(applicationIds).stream()
                .collect(Collectors.groupingBy(interaction -> interaction.getApplicationId(), Collectors.counting()));
    }

    @Override
    public List<Interaction> findByApplicationIds(Collection<UUID> applicationIds, InteractionType type, Instant from) {
        if (applicationIds == null || applicationIds.isEmpty()) {
            return List.of();
        }
        return repository.findAllByApplicationIdInAndTypeAndOccurredAtAfter(applicationIds, type, from).stream()
                .map(DomainMapper::toDomain).toList();
    }

    @Override
    public Interaction save(Interaction interaction) {
        return DomainMapper.toDomain(repository.save(DomainMapper.toEntity(interaction)));
    }

    @Override
    public void delete(Interaction interaction) {
        repository.deleteById(interaction.id());
    }
}
