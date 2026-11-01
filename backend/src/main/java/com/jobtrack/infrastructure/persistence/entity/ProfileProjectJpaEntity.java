package com.jobtrack.infrastructure.persistence.entity;

import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "profile_projects")
public class ProfileProjectJpaEntity {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(nullable = false) private String name;
    @Column(nullable = false, columnDefinition = "TEXT") private String description;
    @Column(columnDefinition = "TEXT") private String technologies;

    protected ProfileProjectJpaEntity() {}

    public ProfileProjectJpaEntity(UUID id, UUID userId, String name, String description, String technologies) {
        this.id = id; this.userId = userId; this.name = name;
        this.description = description; this.technologies = technologies;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getTechnologies() { return technologies; }
}
