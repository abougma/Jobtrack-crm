package com.jobtrack.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;

@Entity
@Table(name = "generated_cvs")
public class GeneratedCvJpaEntity {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "application_id") private UUID applicationId;
    @Column(nullable = false) private String title;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb") private String content;
    @Column(nullable = false) private int score;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected GeneratedCvJpaEntity() {}

    public GeneratedCvJpaEntity(UUID id, UUID userId, UUID applicationId, String title,
                                String content, int score, Instant createdAt) {
        this.id = id; this.userId = userId; this.applicationId = applicationId; this.title = title;
        this.content = content; this.score = score; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getApplicationId() { return applicationId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public int getScore() { return score; }
    public Instant getCreatedAt() { return createdAt; }
}
