package com.jobtrack.application.service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobtrack.application.port.ApplicationRepository;
import com.jobtrack.application.port.InteractionRepository;
import com.jobtrack.application.port.ReminderRepository;
import com.jobtrack.application.port.StatusHistoryRepository;
import com.jobtrack.domain.application.Application;
import com.jobtrack.domain.application.ApplicationStatus;
import com.jobtrack.domain.application.StatusChange;
import com.jobtrack.domain.crm.Interaction;
import com.jobtrack.domain.crm.InteractionType;
import com.jobtrack.domain.crm.Reminder;

@Service
public class DashboardService {
    private final ApplicationRepository applications;
    private final ReminderRepository reminders;
    private final StatusHistoryRepository history;
    private final InteractionRepository interactions;

    public DashboardService(ApplicationRepository applications, ReminderRepository reminders,
                            StatusHistoryRepository history, InteractionRepository interactions) {
        this.applications = applications;
        this.reminders = reminders;
        this.history = history;
        this.interactions = interactions;
    }

    @Transactional(readOnly = true)
    public DashboardSnapshot get(UUID userId) {
        List<Application> owned = applications.findAllByUserId(userId);
        List<UUID> ids = owned.stream().map(Application::id).toList();
        Map<UUID, List<StatusChange>> changes = ids.isEmpty() ? Map.of()
                : history.findAllByApplicationIdIn(ids).stream()
                    .collect(Collectors.groupingBy(StatusChange::applicationId));

        long applied = owned.stream().filter(application -> application.status().countsAsSent()).count();
        long responses = owned.stream().filter(application -> application.status().isResponse()).count();
        long interviews = owned.stream().filter(application -> application.status().isInterview()).count();
        long offers = owned.stream().filter(application -> application.status() == ApplicationStatus.OFFER).count();
        long rejected = owned.stream().filter(application -> application.status() == ApplicationStatus.REJECTED).count();
        long noResponse = owned.stream().filter(application -> application.status() == ApplicationStatus.NO_RESPONSE).count();

        Map<ApplicationStatus, Long> byStatus = new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus status : ApplicationStatus.values()) {
            byStatus.put(status, 0L);
        }
        owned.forEach(application -> byStatus.merge(application.status(), 1L, Long::sum));

        List<SourceMetric> bySource = sourceMetrics(owned);
        LocalDate today = LocalDate.now();
        LocalDate firstWeek = today.with(DayOfWeek.MONDAY).minusWeeks(7);
        List<WeekMetric> weekly = weeklyMetrics(owned, changes, firstWeek);
        double averageResponseDays = averageResponseDays(owned, changes);

        Instant now = Instant.now();
        List<Reminder> due = ids.isEmpty() ? List.of()
                : reminders.findByApplicationIds(ids, null, now, false);
        List<Interaction> upcoming = ids.isEmpty() ? List.of()
                : interactions.findByApplicationIds(ids, InteractionType.INTERVIEW, now);

        return new DashboardSnapshot(
                owned.size(), applied, responses, interviews, offers, rejected, noResponse,
                rate(responses, applied), rate(interviews, applied), rate(offers, applied),
                averageResponseDays,
                byStatus.entrySet().stream().map(entry -> new StatusMetric(entry.getKey(), entry.getValue())).toList(),
                bySource,
                weekly,
                List.of(new FunnelStage("Candidatures", applied), new FunnelStage("Réponses", responses),
                        new FunnelStage("Entretiens", interviews), new FunnelStage("Offres", offers)),
                due.stream().map(reminder -> new DueReminder(reminder.id(), reminder.applicationId(),
                        reminder.title(), reminder.dueAt())).toList(),
                upcoming.stream().map(interaction -> {
                    Application application = owned.stream()
                            .filter(item -> item.id().equals(interaction.applicationId())).findFirst().orElse(null);
                    return new UpcomingInterview(interaction.id(), interaction.applicationId(),
                            application == null ? null : application.company(),
                            application == null ? null : application.position(),
                            interaction.occurredAt());
                }).toList(),
                bySource.stream().sorted(Comparator.comparingDouble(SourceMetric::interviewRate).reversed()
                        .thenComparing(Comparator.comparingLong(SourceMetric::count).reversed())).limit(5).toList());
    }

    private List<SourceMetric> sourceMetrics(List<Application> owned) {
        Map<String, List<Application>> grouped = owned.stream().collect(Collectors.groupingBy(
                application -> application.source() == null || application.source().isBlank()
                        ? "Non renseignée" : application.source()));
        return grouped.entrySet().stream().map(entry -> {
            long count = entry.getValue().size();
            long responses = entry.getValue().stream().filter(application -> application.status().isResponse()).count();
            long interviews = entry.getValue().stream().filter(application -> application.status().isInterview()).count();
            return new SourceMetric(entry.getKey(), count, responses, interviews,
                    rate(responses, count), rate(interviews, count));
        }).sorted(Comparator.comparingLong(SourceMetric::count).reversed()).toList();
    }

    private List<WeekMetric> weeklyMetrics(List<Application> owned, Map<UUID, List<StatusChange>> changes,
                                           LocalDate firstWeek) {
        List<WeekMetric> weeks = new ArrayList<>();
        for (int index = 0; index < 8; index++) {
            LocalDate start = firstWeek.plusWeeks(index);
            LocalDate end = start.plusWeeks(1);
            long count = owned.stream().filter(application -> inRange(applicationDate(application), start, end)).count();
            long responses = changes.values().stream().flatMap(List::stream)
                    .filter(change -> change.newStatus().isResponse())
                    .filter(change -> inRange(date(change.changedAt()), start, end))
                    .map(StatusChange::applicationId).distinct().count();
            long interviews = changes.values().stream().flatMap(List::stream)
                    .filter(change -> change.newStatus().isInterview())
                    .filter(change -> inRange(date(change.changedAt()), start, end))
                    .map(StatusChange::applicationId).distinct().count();
            weeks.add(new WeekMetric(start, count, responses, interviews));
        }
        return weeks;
    }

    private double averageResponseDays(List<Application> owned, Map<UUID, List<StatusChange>> changes) {
        List<Long> delays = new ArrayList<>();
        for (Application application : owned) {
            List<StatusChange> appChanges = changes.getOrDefault(application.id(), List.of());
            appChanges.stream()
                    .filter(change -> change.newStatus().isResponse())
                    .min(Comparator.comparing(StatusChange::changedAt))
                    .ifPresent(change -> delays.add(ChronoUnit.DAYS.between(
                            applicationDate(application), date(change.changedAt()))));
        }
        if (delays.isEmpty()) {
            return 0;
        }
        return Math.round(delays.stream().mapToLong(Long::longValue).average().orElse(0) * 10.0) / 10.0;
    }

    private static LocalDate applicationDate(Application application) {
        return application.appliedAt() != null ? application.appliedAt() : date(application.createdAt());
    }

    private static LocalDate date(Instant instant) {
        return instant.atZone(ZoneOffset.UTC).toLocalDate();
    }

    private static boolean inRange(LocalDate value, LocalDate start, LocalDate end) {
        return !value.isBefore(start) && value.isBefore(end);
    }

    private static double rate(long numerator, long denominator) {
        return denominator == 0 ? 0 : Math.round((numerator * 1000.0 / denominator)) / 10.0;
    }

    public record DashboardSnapshot(
            int totalApplications, long applied, long responses, long interviews, long offers,
            long rejected, long noResponse, double responseRate, double interviewRate, double offerRate,
            double averageResponseDays, List<StatusMetric> applicationsByStatus,
            List<SourceMetric> applicationsBySource, List<WeekMetric> weekly, List<FunnelStage> funnel,
            List<DueReminder> dueReminders, List<UpcomingInterview> upcomingInterviews,
            List<SourceMetric> topSources) {}

    public record StatusMetric(ApplicationStatus status, long count) {}
    public record SourceMetric(String source, long count, long responses, long interviews,
                               double responseRate, double interviewRate) {}
    public record WeekMetric(LocalDate weekStart, long applications, long responses, long interviews) {}
    public record FunnelStage(String stage, long count) {}
    public record DueReminder(UUID id, UUID applicationId, String title, Instant dueAt) {}
    public record UpcomingInterview(UUID id, UUID applicationId, String company, String position, Instant occurredAt) {}
}
