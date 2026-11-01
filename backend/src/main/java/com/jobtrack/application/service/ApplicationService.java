package com.jobtrack.application.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobtrack.application.port.*;
import com.jobtrack.domain.application.*;
import com.jobtrack.domain.crm.Contact;
import com.jobtrack.domain.crm.Interaction;
import com.jobtrack.domain.crm.Reminder;
import com.jobtrack.domain.cv.GeneratedCv;

@Service
public class ApplicationService {
    private final ApplicationRepository applications;
    private final ContactRepository contacts;
    private final InteractionRepository interactions;
    private final ReminderRepository reminders;
    private final StatusHistoryRepository history;
    private final GeneratedCvRepository generatedCvs;

    public ApplicationService(ApplicationRepository applications, ContactRepository contacts,
                              InteractionRepository interactions, ReminderRepository reminders,
                              StatusHistoryRepository history, GeneratedCvRepository generatedCvs) {
        this.applications = applications;
        this.contacts = contacts;
        this.interactions = interactions;
        this.reminders = reminders;
        this.history = history;
        this.generatedCvs = generatedCvs;
    }

    @Transactional(readOnly = true)
    public Page<ApplicationSummary> list(UUID userId, ApplicationFilter filter, Pageable pageable) {
        Page<Application> page = applications.search(userId, filter, pageable);
        List<ApplicationSummary> content = toSummaries(userId, page.getContent());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<Application> listForExport(UUID userId, ApplicationFilter filter) {
        return applications.search(userId, filter, Pageable.unpaged()).getContent();
    }

    @Transactional(readOnly = true)
    public ApplicationDetail get(UUID userId, UUID applicationId) {
        Application application = findOwned(userId, applicationId);
        List<StatusChangeView> changes = history.findAllByApplicationId(applicationId).stream()
                .map(StatusChangeView::from).toList();
        List<InteractionView> interactionViews = interactions.findAllByApplicationId(applicationId).stream()
                .map(InteractionView::from).toList();
        List<ReminderView> reminderViews = reminders.findAllByApplicationId(applicationId).stream()
                .map(ReminderView::from).toList();
        UUID generatedCvId = generatedCvs.findLatestByApplicationIdAndUserId(applicationId, userId)
                .map(GeneratedCv::id).orElse(null);
        ApplicationSummary summary = toSummaries(userId, List.of(application)).getFirst();
        return new ApplicationDetail(summary, application.notes(), changes, interactionViews,
                reminderViews, generatedCvId);
    }

    @Transactional
    public ApplicationDetail create(UUID userId, ApplicationCommand command) {
        Application application = createEntity(userId, command);
        return get(userId, application.id());
    }

    @Transactional
    public Application createEntity(UUID userId, ApplicationCommand command) {
        validateContact(userId, command.contactId());
        Application application = Application.draft(userId, command.contactId(), command.company().trim(),
                command.position().trim(), command.location(), command.workMode(), command.contractType(),
                command.salaryMin(), command.salaryMax(), command.source(), command.offerUrl(),
                command.appliedAt(), command.notes(), command.status());
        application = applications.save(application);
        history.save(StatusChange.of(application.id(), null, application.status()));
        return application;
    }

    @Transactional
    public ApplicationDetail update(UUID userId, UUID applicationId, ApplicationCommand command) {
        validateContact(userId, command.contactId());
        Application application = findOwned(userId, applicationId);
        Application updated = application.updateDetails(command.contactId(), command.company().trim(),
                command.position().trim(), command.location(), command.workMode(), command.contractType(),
                command.salaryMin(), command.salaryMax(), command.source(), command.offerUrl(),
                command.appliedAt(), command.notes());
        if (command.status() != null && command.status() != updated.status()) {
            ApplicationStatus previous = updated.status();
            updated = updated.changeStatus(command.status());
            history.save(StatusChange.of(updated.id(), previous, command.status()));
        }
        applications.save(updated);
        return get(userId, applicationId);
    }

    @Transactional
    public ApplicationDetail changeStatus(UUID userId, UUID applicationId, ApplicationStatus newStatus) {
        Application application = findOwned(userId, applicationId);
        if (application.status() == newStatus) {
            return get(userId, applicationId);
        }
        ApplicationStatus previous = application.status();
        Application updated = applications.save(application.changeStatus(newStatus));
        history.save(StatusChange.of(updated.id(), previous, newStatus));
        return get(userId, applicationId);
    }

    @Transactional
    public void delete(UUID userId, UUID applicationId) {
        applications.delete(findOwned(userId, applicationId));
    }

    @Transactional(readOnly = true)
    public List<StatusChangeView> history(UUID userId, UUID applicationId) {
        findOwned(userId, applicationId);
        return history.findAllByApplicationId(applicationId).stream().map(StatusChangeView::from).toList();
    }

    private List<ApplicationSummary> toSummaries(UUID userId, List<Application> items) {
        if (items.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = items.stream().map(Application::id).toList();
        Map<UUID, Long> interactionCounts = interactions.countByApplicationIds(ids);
        Map<UUID, Contact> contactsById = contacts.findAllByUserId(userId).stream()
                .collect(Collectors.toMap(Contact::id, Function.identity()));
        Map<UUID, Instant> nextReminders = reminders.findByApplicationIds(ids, null, null, false).stream()
                .collect(Collectors.groupingBy(Reminder::applicationId,
                        Collectors.collectingAndThen(Collectors.minBy(Comparator.comparing(Reminder::dueAt)),
                                optional -> optional.map(Reminder::dueAt).orElse(null))));
        return items.stream().map(application -> {
            Contact contact = application.contactId() == null ? null : contactsById.get(application.contactId());
            return new ApplicationSummary(
                    application.id(), application.company(), application.position(), application.location(),
                    application.workMode(), application.contractType(), application.salaryMin(), application.salaryMax(),
                    application.source(), application.offerUrl(), application.status(), application.appliedAt(),
                    application.contactId(), contact == null ? null : contact.name(),
                    nextReminders.get(application.id()), interactionCounts.getOrDefault(application.id(), 0L),
                    application.createdAt(), application.updatedAt());
        }).toList();
    }

    private Application findOwned(UUID userId, UUID applicationId) {
        return applications.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Candidature introuvable"));
    }

    private void validateContact(UUID userId, UUID contactId) {
        if (contactId != null && contacts.findByIdAndUserId(contactId, userId).isEmpty()) {
            throw new ApiExceptions.NotFoundException("Contact introuvable");
        }
    }

    public record ApplicationCommand(
            UUID contactId, String company, String position, String location,
            WorkMode workMode, ContractType contractType, Integer salaryMin, Integer salaryMax,
            String source, String offerUrl, LocalDate appliedAt, String notes, ApplicationStatus status) {}

    public record ApplicationSummary(
            UUID id, String company, String position, String location, WorkMode workMode,
            ContractType contractType, Integer salaryMin, Integer salaryMax, String source,
            String offerUrl, ApplicationStatus status, LocalDate appliedAt, UUID contactId,
            String contactName, Instant nextReminderAt, long interactionCount,
            Instant createdAt, Instant updatedAt) {}

    public record ApplicationDetail(
            ApplicationSummary summary, String notes, List<StatusChangeView> history,
            List<InteractionView> interactions, List<ReminderView> reminders, UUID generatedCvId) {}

    public record StatusChangeView(UUID id, ApplicationStatus previousStatus, ApplicationStatus newStatus,
                                   Instant changedAt) {
        static StatusChangeView from(StatusChange change) {
            return new StatusChangeView(change.id(), change.previousStatus(), change.newStatus(), change.changedAt());
        }
    }

    public record InteractionView(UUID id, com.jobtrack.domain.crm.InteractionType type, String message,
                                  Instant occurredAt) {
        static InteractionView from(Interaction interaction) {
            return new InteractionView(interaction.id(), interaction.type(), interaction.message(), interaction.occurredAt());
        }
    }

    public record ReminderView(UUID id, String title, Instant dueAt, boolean completed) {
        static ReminderView from(Reminder reminder) {
            return new ReminderView(reminder.id(), reminder.title(), reminder.dueAt(), reminder.completed());
        }
    }
}
