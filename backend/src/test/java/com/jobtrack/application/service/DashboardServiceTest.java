package com.jobtrack.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.jobtrack.application.port.ApplicationRepository;
import com.jobtrack.application.port.InteractionRepository;
import com.jobtrack.application.port.ReminderRepository;
import com.jobtrack.application.port.StatusHistoryRepository;
import com.jobtrack.domain.application.Application;
import com.jobtrack.domain.application.ApplicationStatus;

class DashboardServiceTest {

    @Test
    void aggregatesApplicationsIntoRatesAndFunnel() {
        UUID userId = UUID.randomUUID();
        Application applied = Application.draft(userId, null, "Acme", "Développeur Java", "Paris",
                null, null, null, null, "LinkedIn", null, LocalDate.now().minusDays(3), null,
                ApplicationStatus.APPLIED);
        Application interview = Application.draft(userId, null, "Globex", "Ingénieur logiciel", "Lyon",
                null, null, null, null, "Réseau", null, LocalDate.now().minusDays(1), null,
                ApplicationStatus.INTERVIEW);

        ApplicationRepository applications = mock(ApplicationRepository.class);
        when(applications.findAllByUserId(userId)).thenReturn(List.of(applied, interview));
        ReminderRepository reminders = mock(ReminderRepository.class);
        when(reminders.findByApplicationIds(anyCollection(), any(), any(), anyBoolean())).thenReturn(List.of());
        StatusHistoryRepository history = mock(StatusHistoryRepository.class);
        when(history.findAllByApplicationIdIn(anyCollection())).thenReturn(List.of());
        InteractionRepository interactions = mock(InteractionRepository.class);
        when(interactions.findByApplicationIds(anyCollection(), any(), any())).thenReturn(List.of());

        DashboardService service = new DashboardService(applications, reminders, history, interactions);
        DashboardService.DashboardSnapshot snapshot = service.get(userId);

        assertThat(snapshot.totalApplications()).isEqualTo(2);
        assertThat(snapshot.applied()).isEqualTo(2);
        assertThat(snapshot.responses()).isEqualTo(1);
        assertThat(snapshot.interviews()).isEqualTo(1);
        assertThat(snapshot.responseRate()).isEqualTo(50.0);
        assertThat(snapshot.interviewRate()).isEqualTo(50.0);
        assertThat(snapshot.applicationsByStatus()).hasSize(ApplicationStatus.values().length);
        assertThat(snapshot.weekly()).hasSize(8);
        assertThat(snapshot.funnel()).extracting(DashboardService.FunnelStage::stage)
                .containsExactly("Candidatures", "Réponses", "Entretiens", "Offres");
        assertThat(snapshot.upcomingInterviews()).isEmpty();
        assertThat(snapshot.dueReminders()).isEmpty();
    }

    @Test
    void returnsAnEmptyDashboardWhenTheUserHasNoApplication() {
        UUID userId = UUID.randomUUID();
        ApplicationRepository applications = mock(ApplicationRepository.class);
        when(applications.findAllByUserId(userId)).thenReturn(List.of());

        DashboardService service = new DashboardService(applications, mock(ReminderRepository.class),
                mock(StatusHistoryRepository.class), mock(InteractionRepository.class));
        DashboardService.DashboardSnapshot snapshot = service.get(userId);

        assertThat(snapshot.totalApplications()).isZero();
        assertThat(snapshot.applied()).isZero();
        assertThat(snapshot.responseRate()).isZero();
        assertThat(snapshot.averageResponseDays()).isZero();
        assertThat(Instant.now()).isAfter(Instant.EPOCH);
    }
}
