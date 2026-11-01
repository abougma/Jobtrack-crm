package com.jobtrack.application.port;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jobtrack.domain.application.Application;

public interface ApplicationRepository {
    Page<Application> search(UUID userId, ApplicationFilter filter, Pageable pageable);
    Optional<Application> findByIdAndUserId(UUID id, UUID userId);
    List<Application> findAllByUserId(UUID userId);
    List<Application> findAllByUserIdIn(Collection<UUID> userIds);
    long count();
    Application save(Application application);
    void delete(Application application);
}
