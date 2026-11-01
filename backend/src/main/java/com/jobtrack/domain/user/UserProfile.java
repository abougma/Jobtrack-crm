package com.jobtrack.domain.user;

import java.time.Instant;
import java.util.UUID;

public record UserProfile(
        UUID userId,
        String firstName,
        String lastName,
        String headline,
        String location,
        Instant updatedAt) {

    public static UserProfile empty(UUID userId) {
        return new UserProfile(userId, null, null, null, null, Instant.now());
    }

    public UserProfile update(String firstName, String lastName, String headline, String location) {
        return new UserProfile(userId, firstName, lastName, headline, location, Instant.now());
    }
}
