package com.jobtrack.infrastructure.persistence.entity;

import java.time.*;
import java.util.UUID;

import com.jobtrack.domain.plan.Plan;
import jakarta.persistence.*;

@Entity
@Table(name = "user_plans")
public class UserPlanJpaEntity {
    @Id @Column(name = "user_id") private UUID userId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private Plan plan;
    @Column(name = "cv_analysis_used", nullable = false) private int cvAnalysisUsed;
    @Column(name = "cv_analysis_reset_at", nullable = false) private LocalDate cvAnalysisResetAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected UserPlanJpaEntity() {}

    public UserPlanJpaEntity(UUID userId, Plan plan, int cvAnalysisUsed, LocalDate cvAnalysisResetAt,
                             Instant createdAt, Instant updatedAt) {
        this.userId = userId; this.plan = plan; this.cvAnalysisUsed = cvAnalysisUsed;
        this.cvAnalysisResetAt = cvAnalysisResetAt; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public UUID getUserId() { return userId; }
    public Plan getPlan() { return plan; }
    public int getCvAnalysisUsed() { return cvAnalysisUsed; }
    public LocalDate getCvAnalysisResetAt() { return cvAnalysisResetAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
