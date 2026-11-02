package com.jobtrack.infrastructure.persistence.entity;

import java.util.UUID;

import com.jobtrack.domain.profile.SkillContext;
import com.jobtrack.domain.profile.SkillLevel;
import jakarta.persistence.*;

@Entity
@Table(name = "profile_skills")
public class ProfileSkillJpaEntity {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(nullable = false) private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private SkillLevel level;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private SkillContext context;
    @Column(length = 100) private String category;

    protected ProfileSkillJpaEntity() {}

    public ProfileSkillJpaEntity(UUID id, UUID userId, String name, SkillLevel level, SkillContext context,
                                 String category) {
        this.id = id; this.userId = userId; this.name = name; this.level = level; this.context = context;
        this.category = category;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public SkillLevel getLevel() { return level; }
    public SkillContext getContext() { return context; }
    public String getCategory() { return category; }
}
