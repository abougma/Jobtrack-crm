package com.jobtrack.infrastructure.persistence.entity;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "profile_educations")
public class ProfileEducationJpaEntity {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(nullable = false) private String institution;
    @Column(nullable = false) private String degree;
    @Column(name = "started_on") private LocalDate startedOn;
    @Column(name = "ended_on") private LocalDate endedOn;
    @Column(columnDefinition = "TEXT") private String description;

    protected ProfileEducationJpaEntity() {}

    public ProfileEducationJpaEntity(UUID id, UUID userId, String institution, String degree,
                                     LocalDate startedOn, LocalDate endedOn, String description) {
        this.id = id; this.userId = userId; this.institution = institution; this.degree = degree;
        this.startedOn = startedOn; this.endedOn = endedOn; this.description = description;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getInstitution() { return institution; }
    public String getDegree() { return degree; }
    public LocalDate getStartedOn() { return startedOn; }
    public LocalDate getEndedOn() { return endedOn; }
    public String getDescription() { return description; }
}
