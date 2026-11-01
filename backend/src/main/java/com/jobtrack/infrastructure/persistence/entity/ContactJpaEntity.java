package com.jobtrack.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "contacts")
public class ContactJpaEntity {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(nullable = false) private String name;
    private String role;
    @Column(name = "linkedin_url") private String linkedinUrl;
    private String email;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected ContactJpaEntity() {}

    public ContactJpaEntity(UUID id, UUID userId, String name, String role, String linkedinUrl,
                            String email, Instant createdAt) {
        this.id = id; this.userId = userId; this.name = name; this.role = role;
        this.linkedinUrl = linkedinUrl; this.email = email; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getLinkedinUrl() { return linkedinUrl; }
    public String getEmail() { return email; }
    public Instant getCreatedAt() { return createdAt; }
}
