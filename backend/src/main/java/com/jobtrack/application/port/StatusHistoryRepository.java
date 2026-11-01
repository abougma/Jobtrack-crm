package com.jobtrack.application.port;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.jobtrack.domain.application.StatusChange;

public interface StatusHistoryRepository {
    List<StatusChange> findAllByApplicationId(UUID applicationId);
    List<StatusChange> findAllByApplicationIdIn(Collection<UUID> applicationIds);
    StatusChange save(StatusChange change);
}
