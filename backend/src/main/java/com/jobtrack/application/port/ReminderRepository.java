package com.jobtrack.application.port;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jobtrack.domain.crm.Reminder;

public interface ReminderRepository {
    List<Reminder> findAllByApplicationId(UUID applicationId);
    Optional<Reminder> findByIdAndApplicationId(UUID id, UUID applicationId);
    List<Reminder> findByApplicationIds(Collection<UUID> applicationIds, Instant from, Instant to, Boolean completed);
    Reminder save(Reminder reminder);
    void delete(Reminder reminder);
}
