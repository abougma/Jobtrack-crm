package com.jobtrack.infrastructure.persistence.entity;

import java.util.UUID;

import com.jobtrack.domain.profile.LanguageLevel;
import jakarta.persistence.*;

@Entity
@Table(name = "profile_languages")
public class ProfileLanguageJpaEntity {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(nullable = false) private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private LanguageLevel level;

    protected ProfileLanguageJpaEntity() {}

    public ProfileLanguageJpaEntity(UUID id, UUID userId, String name, LanguageLevel level) {
        this.id = id; this.userId = userId; this.name = name; this.level = level;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public LanguageLevel getLevel() { return level; }
}
