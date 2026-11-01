package com.jobtrack.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.jobtrack.application.port.UserPlanRepository;
import com.jobtrack.domain.plan.Plan;
import com.jobtrack.domain.plan.UserPlan;

class QuotaServiceTest {

    private static final class InMemoryUserPlanRepository implements UserPlanRepository {
        private final Map<UUID, UserPlan> store = new HashMap<>();

        @Override
        public Optional<UserPlan> findById(UUID userId) {
            return Optional.ofNullable(store.get(userId));
        }

        @Override
        public List<UserPlan> findAllByIdIn(Collection<UUID> userIds) {
            return userIds.stream().map(store::get).filter(plan -> plan != null).toList();
        }

        @Override
        public long countByPlan(Plan plan) {
            return store.values().stream().filter(candidate -> candidate.plan() == plan).count();
        }

        @Override
        public UserPlan save(UserPlan plan) {
            store.put(plan.userId(), plan);
            return plan;
        }
    }

    @Test
    void consumesTheFirstAnalysisAndCreatesThePlanOnDemand() {
        InMemoryUserPlanRepository repository = new InMemoryUserPlanRepository();
        QuotaService service = new QuotaService(repository);
        UUID userId = UUID.randomUUID();

        QuotaService.QuotaSnapshot snapshot = service.consumeCvAnalysis(userId);

        assertThat(snapshot.plan()).isEqualTo(Plan.FREE.name());
        assertThat(snapshot.used()).isEqualTo(1);
        assertThat(snapshot.limit()).isEqualTo(Plan.FREE.cvAnalysisLimit());
        assertThat(repository.findById(userId)).isPresent();
    }

    @Test
    void rejectsAnAnalysisWhenTheQuotaIsExhausted() {
        InMemoryUserPlanRepository repository = new InMemoryUserPlanRepository();
        UUID userId = UUID.randomUUID();
        UserPlan plan = UserPlan.create(userId);
        for (int index = 0; index < Plan.FREE.cvAnalysisLimit(); index++) {
            plan = plan.consumeCvAnalysis();
        }
        repository.save(plan);
        QuotaService service = new QuotaService(repository);

        assertThatThrownBy(() -> service.consumeCvAnalysis(userId))
                .isInstanceOf(ApiExceptions.QuotaExceededException.class);
    }
}
