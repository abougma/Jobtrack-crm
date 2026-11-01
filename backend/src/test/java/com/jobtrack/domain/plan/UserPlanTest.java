package com.jobtrack.domain.plan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class UserPlanTest {

    @Test
    void newUserStartsWithTheFreePlanAndNoUsage() {
        UserPlan plan = UserPlan.create(UUID.randomUUID());

        assertThat(plan.plan()).isEqualTo(Plan.FREE);
        assertThat(plan.cvAnalysisUsed()).isZero();
        assertThat(plan.cvAnalysisResetAt()).isAfter(LocalDate.now());
    }

    @Test
    void consumesTheQuotaAndRejectsBeyondTheLimit() {
        UserPlan plan = UserPlan.create(UUID.randomUUID());
        for (int index = 0; index < Plan.FREE.cvAnalysisLimit(); index++) {
            plan = plan.consumeCvAnalysis();
        }
        assertThat(plan.cvAnalysisUsed()).isEqualTo(Plan.FREE.cvAnalysisLimit());

        UserPlan exhausted = plan;
        assertThatThrownBy(exhausted::consumeCvAnalysis)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Quota");
    }

    @Test
    void changingPlanKeepsTheUsage() {
        UserPlan plan = UserPlan.create(UUID.randomUUID()).consumeCvAnalysis().changePlan(Plan.PRO);

        assertThat(plan.plan()).isEqualTo(Plan.PRO);
        assertThat(plan.cvAnalysisUsed()).isEqualTo(1);
        assertThat(Plan.PRO.cvAnalysisLimit()).isGreaterThan(Plan.FREE.cvAnalysisLimit());
    }
}
