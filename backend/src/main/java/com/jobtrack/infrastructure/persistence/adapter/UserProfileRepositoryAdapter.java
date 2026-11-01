package com.jobtrack.infrastructure.persistence.adapter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jobtrack.application.port.UserProfileRepository;
import com.jobtrack.domain.user.UserProfile;
import com.jobtrack.infrastructure.persistence.repository.SpringUserProfileRepository;

@Component
public class UserProfileRepositoryAdapter implements UserProfileRepository {
    private final SpringUserProfileRepository repository;

    public UserProfileRepositoryAdapter(SpringUserProfileRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<UserProfile> findById(UUID userId) {
        return repository.findById(userId).map(DomainMapper::toDomain);
    }

    @Override
    public List<UserProfile> findAllByIdIn(Collection<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return repository.findAllById(userIds).stream().map(DomainMapper::toDomain).toList();
    }

    @Override
    public UserProfile save(UserProfile profile) {
        return DomainMapper.toDomain(repository.save(DomainMapper.toEntity(profile)));
    }
}
