package com.jobtrack.infrastructure.persistence.entity;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "profile_experiences")
public class ProfileExperienceJpaEntity {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(nullable = false) private String company;
    @Column(name = "job_title", nullable = false) private String jobTitle;
    @Column(name = "started_on", nullable = false) private LocalDate startedOn;
    @Column(name = "ended_on") private LocalDate endedOn;
    @Column(nullable = false, columnDefinition = "TEXT") private String description;

    protected ProfileExperienceJpaEntity() {}

    public ProfileExperienceJpaEntity(UUID id, UUID userId, String company, String jobTitle,
                                      LocalDate startedOn, LocalDate endedOn, String description) {
        this.id = id; this.userId = userId; this.company = company; this.jobTitle = jobTitle;
        this.startedOn = startedOn; this.endedOn = endedOn; this.description = description;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getCompany() { return company; }
    public String getJobTitle() { return jobTitle; }
    public LocalDate getStartedOn() { return startedOn; }
    public LocalDate getEndedOn() { return endedOn; }
    public String getDescription() { return description; }
}
