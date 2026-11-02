package com.jobtrack.domain.user;

import java.time.Instant;
import java.util.UUID;

public record UserProfile(
        UUID userId,
        String firstName,
        String lastName,
        String headline,
        String location,
        String phone,
        String linkedinUrl,
        String portfolioUrl,
        Instant updatedAt) {
    public UserProfile(UUID userId, String firstName, String lastName, String headline,
                       String location, Instant updatedAt) {
        this(userId, firstName, lastName, headline, location, null, null, null, updatedAt);
    }

    public static UserProfile empty(UUID userId) {
        return new UserProfile(userId, null, null, null, null, null, null, null, Instant.now());
    }

    public UserProfile update(String firstName, String lastName, String headline, String location,
                              String phone, String linkedinUrl, String portfolioUrl) {
        return new UserProfile(userId, firstName, lastName, headline, location, phone,
                linkedinUrl, portfolioUrl, Instant.now());
    }
}
