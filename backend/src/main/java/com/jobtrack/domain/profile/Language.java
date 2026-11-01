package com.jobtrack.domain.profile;

import java.util.UUID;

public record Language(
        UUID id,
        UUID userId,
        String name,
        LanguageLevel level) {

    public static Language create(UUID userId, String name, LanguageLevel level) {
        return new Language(UUID.randomUUID(), userId, name, level);
    }

    public Language update(String name, LanguageLevel level) {
        return new Language(id, userId, name, level);
    }
}
