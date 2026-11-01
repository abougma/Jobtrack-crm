package com.jobtrack.domain.profile;

import java.util.UUID;

public record Skill(
        UUID id,
        UUID userId,
        String name,
        SkillLevel level,
        SkillContext context) {

    public static Skill create(UUID userId, String name, SkillLevel level, SkillContext context) {
        return new Skill(UUID.randomUUID(), userId, name, level, context);
    }

    public Skill update(String name, SkillLevel level, SkillContext context) {
        return new Skill(id, userId, name, level, context);
    }
}
