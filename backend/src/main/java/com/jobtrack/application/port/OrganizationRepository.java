package com.jobtrack.application.port;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jobtrack.domain.organization.Organization;

public interface OrganizationRepository {
    Organization save(Organization organization);
    Optional<Organization> findById(UUID id);
    List<Organization> findAllByIdIn(Collection<UUID> ids);
    Page<Organization> search(String query, Pageable pageable);
    long count();
}
