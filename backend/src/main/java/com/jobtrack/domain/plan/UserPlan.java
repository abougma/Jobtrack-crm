package com.jobtrack.domain.plan;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UserPlan(
        UUID userId,
        Plan plan,
        int cvAnalysisUsed,
        LocalDate cvAnalysisResetAt,
        Instant createdAt,
        Instant updatedAt) {

    public static UserPlan create(UUID userId) {
        Instant now = Instant.now();
        return new UserPlan(userId, Plan.FREE, 0, nextResetDate(), now, now);
    }

    public UserPlan consumeCvAnalysis() {
        LocalDate today = LocalDate.now();
        int used = cvAnalysisUsed;
        LocalDate resetAt = cvAnalysisResetAt;
        if (!today.isBefore(resetAt)) {
            used = 0;
            resetAt = nextResetDate();
        }
        if (used >= plan.cvAnalysisLimit()) {
            throw new IllegalStateException("Quota mensuel d'analyse CV atteint");
        }
        return new UserPlan(userId, plan, used + 1, resetAt, createdAt, Instant.now());
    }

    public UserPlan changePlan(Plan newPlan) {
        return new UserPlan(userId, newPlan, cvAnalysisUsed, cvAnalysisResetAt, createdAt, Instant.now());
    }

    private static LocalDate nextResetDate() {
        return LocalDate.now().withDayOfMonth(1).plusMonths(1);
    }
}
