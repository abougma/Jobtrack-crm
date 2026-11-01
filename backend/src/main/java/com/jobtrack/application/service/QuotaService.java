package com.jobtrack.application.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobtrack.application.port.UserPlanRepository;
import com.jobtrack.domain.plan.UserPlan;

@Service
public class QuotaService {
    private final UserPlanRepository plans;

    public QuotaService(UserPlanRepository plans) {
        this.plans = plans;
    }

    @Transactional
    public QuotaSnapshot consumeCvAnalysis(UUID userId) {
        UserPlan plan = plans.findById(userId).orElseGet(() -> UserPlan.create(userId));
        try {
            plan = plan.consumeCvAnalysis();
        } catch (IllegalStateException exception) {
            throw new ApiExceptions.QuotaExceededException(exception.getMessage());
        }
        plans.save(plan);
        return snapshot(plan);
    }

    @Transactional(readOnly = true)
    public QuotaSnapshot get(UUID userId) {
        return snapshot(plans.findById(userId).orElseGet(() -> UserPlan.create(userId)));
    }

    private QuotaSnapshot snapshot(UserPlan plan) {
        return new QuotaSnapshot(plan.plan().name(), plan.cvAnalysisUsed(), plan.plan().cvAnalysisLimit(),
                plan.cvAnalysisResetAt(), plan.plan().features());
    }

    public record QuotaSnapshot(String plan, int used, int limit, LocalDate resetAt,
                                java.util.List<String> features) {}
}
