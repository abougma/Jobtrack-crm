package com.jobtrack.application.port;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jobtrack.domain.user.UserProfile;

public interface UserProfileRepository {
    Optional<UserProfile> findById(UUID userId);
    List<UserProfile> findAllByIdIn(Collection<UUID> userIds);
    UserProfile save(UserProfile profile);
}
