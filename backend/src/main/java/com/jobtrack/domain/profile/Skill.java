package com.jobtrack.domain.profile;

import java.util.UUID;

public record Skill(
        UUID id,
        UUID userId,
        String name,
        SkillLevel level,
        SkillContext context,
        String category) {

    public Skill {
        category = category == null || category.isBlank() ? "Autres" : category.trim();
    }

    public Skill(UUID id, UUID userId, String name, SkillLevel level, SkillContext context) {
        this(id, userId, name, level, context, "Autres");
    }

    public static Skill create(UUID userId, String name, SkillLevel level, SkillContext context) {
        return create(userId, name, level, context, "Autres");
    }

    public static Skill create(UUID userId, String name, SkillLevel level, SkillContext context, String category) {
        return new Skill(UUID.randomUUID(), userId, name, level, context, category);
    }

    public Skill update(String name, SkillLevel level, SkillContext context) {
        return update(name, level, context, category);
    }

    public Skill update(String name, SkillLevel level, SkillContext context, String category) {
        return new Skill(id, userId, name, level, context, category);
    }
}
