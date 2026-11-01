package com.jobtrack.domain.profile;

import java.util.UUID;

public record Project(
        UUID id,
        UUID userId,
        String name,
        String description,
        String technologies) {

    public static Project create(UUID userId, String name, String description, String technologies) {
        return new Project(UUID.randomUUID(), userId, name, description, technologies);
    }

    public Project update(String name, String description, String technologies) {
        return new Project(id, userId, name, description, technologies);
    }
}
