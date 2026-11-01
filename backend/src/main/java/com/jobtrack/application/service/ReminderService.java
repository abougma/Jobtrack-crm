package com.jobtrack.application.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobtrack.application.port.ApplicationRepository;
import com.jobtrack.application.port.ReminderRepository;
import com.jobtrack.domain.application.Application;
import com.jobtrack.domain.crm.Reminder;

@Service
public class ReminderService {
    private final ApplicationRepository applications;
    private final ReminderRepository reminders;

    public ReminderService(ApplicationRepository applications, ReminderRepository reminders) {
        this.applications = applications;
        this.reminders = reminders;
    }

    @Transactional(readOnly = true)
    public List<ReminderView> list(UUID userId, UUID applicationId) {
        Application application = requireOwnedApplication(userId, applicationId);
        return reminders.findAllByApplicationId(applicationId).stream()
                .map(reminder -> ReminderView.from(reminder, application)).toList();
    }

    @Transactional
    public ReminderView create(UUID userId, UUID applicationId, String title, Instant dueAt) {
        Application application = requireOwnedApplication(userId, applicationId);
        Reminder reminder = reminders.save(Reminder.create(applicationId, title.trim(), dueAt));
        return ReminderView.from(reminder, application);
    }

    @Transactional
    public ReminderView complete(UUID userId, UUID applicationId, UUID reminderId) {
        Application application = requireOwnedApplication(userId, applicationId);
        Reminder reminder = reminders.findByIdAndApplicationId(reminderId, applicationId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Rappel introuvable"));
        return ReminderView.from(reminders.save(reminder.complete()), application);
    }

    @Transactional
    public void delete(UUID userId, UUID applicationId, UUID reminderId) {
        requireOwnedApplication(userId, applicationId);
        Reminder reminder = reminders.findByIdAndApplicationId(reminderId, applicationId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Rappel introuvable"));
        reminders.delete(reminder);
    }

    @Transactional(readOnly = true)
    public List<ReminderView> global(UUID userId, Instant from, Instant to, Boolean completed) {
        List<Application> owned = applications.findAllByUserId(userId);
        if (owned.isEmpty()) {
            return List.of();
        }
        var byId = owned.stream().collect(java.util.stream.Collectors.toMap(Application::id, application -> application));
        return reminders.findByApplicationIds(byId.keySet(), from, to, completed).stream()
                .map(reminder -> ReminderView.from(reminder, byId.get(reminder.applicationId())))
                .sorted(Comparator.comparing(ReminderView::dueAt))
                .toList();
    }

    private Application requireOwnedApplication(UUID userId, UUID applicationId) {
        return applications.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Candidature introuvable"));
    }

    public record ReminderView(UUID id, UUID applicationId, String applicationCompany,
                               String applicationPosition, String title, Instant dueAt, boolean completed) {
        static ReminderView from(Reminder reminder, Application application) {
            return new ReminderView(reminder.id(), reminder.applicationId(),
                    application == null ? null : application.company(),
                    application == null ? null : application.position(),
                    reminder.title(), reminder.dueAt(), reminder.completed());
        }
    }
}
