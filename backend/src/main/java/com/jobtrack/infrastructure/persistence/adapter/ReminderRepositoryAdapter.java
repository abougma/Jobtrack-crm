package com.jobtrack.infrastructure.persistence.adapter;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.jobtrack.application.port.ReminderRepository;
import com.jobtrack.domain.crm.Reminder;
import com.jobtrack.infrastructure.persistence.repository.SpringReminderRepository;

@Component
public class ReminderRepositoryAdapter implements ReminderRepository {
    private final SpringReminderRepository repository;

    public ReminderRepositoryAdapter(SpringReminderRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Reminder> findAllByApplicationId(UUID applicationId) {
        return repository.findAllByApplicationIdOrderByDueAtAsc(applicationId).stream()
                .map(DomainMapper::toDomain).toList();
    }

    @Override
    public Optional<Reminder> findByIdAndApplicationId(UUID id, UUID applicationId) {
        return repository.findByIdAndApplicationId(id, applicationId).map(DomainMapper::toDomain);
    }

    @Override
    public List<Reminder> findByApplicationIds(Collection<UUID> applicationIds, Instant from, Instant to,
                                               Boolean completed) {
        if (applicationIds == null || applicationIds.isEmpty()) {
            return List.of();
        }
        return repository.findAllByApplicationIdInOrderByDueAtAsc(applicationIds).stream()
                .map(DomainMapper::toDomain)
                .filter(reminder -> completed == null || reminder.completed() == completed)
                .filter(reminder -> from == null || !reminder.dueAt().isBefore(from))
                .filter(reminder -> to == null || !reminder.dueAt().isAfter(to))
                .collect(Collectors.toList());
    }

    @Override
    public Reminder save(Reminder reminder) {
        return DomainMapper.toDomain(repository.save(DomainMapper.toEntity(reminder)));
    }

    @Override
    public void delete(Reminder reminder) {
        repository.deleteById(reminder.id());
    }
}
