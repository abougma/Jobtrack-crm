package com.jobtrack.application.port;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.jobtrack.domain.crm.Interaction;

public interface InteractionRepository {
    List<Interaction> findAllByApplicationId(UUID applicationId);
    Optional<Interaction> findByIdAndApplicationId(UUID id, UUID applicationId);
    Map<UUID, Long> countByApplicationIds(Collection<UUID> applicationIds);
    List<Interaction> findByApplicationIds(Collection<UUID> applicationIds, com.jobtrack.domain.crm.InteractionType type, Instant from);
    Interaction save(Interaction interaction);
    void delete(Interaction interaction);
}
