package com.jobtrack.application.service;

import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobtrack.application.port.*;
import com.jobtrack.domain.application.Application;
import com.jobtrack.domain.plan.UserPlan;
import com.jobtrack.domain.user.User;

@Service
public class PrivacyService {
    private final UserRepository users;
    private final UserProfileRepository profiles;
    private final UserPlanRepository plans;
    private final ApplicationRepository applications;
    private final ContactRepository contacts;
    private final InteractionRepository interactions;
    private final ReminderRepository reminders;
    private final StatusHistoryRepository history;
    private final ProfileRepository profileRepository;
    private final GeneratedCvRepository cvs;
    private final MembershipRepository memberships;
    private final ConsentRepository consents;
    private final OrganizationRepository organizations;

    public PrivacyService(UserRepository users, UserProfileRepository profiles, UserPlanRepository plans,
                          ApplicationRepository applications, ContactRepository contacts,
                          InteractionRepository interactions, ReminderRepository reminders,
                          StatusHistoryRepository history, ProfileRepository profileRepository,
                          GeneratedCvRepository cvs, MembershipRepository memberships,
                          ConsentRepository consents, OrganizationRepository organizations) {
        this.users = users;
        this.profiles = profiles;
        this.plans = plans;
        this.applications = applications;
        this.contacts = contacts;
        this.interactions = interactions;
        this.reminders = reminders;
        this.history = history;
        this.profileRepository = profileRepository;
        this.cvs = cvs;
        this.memberships = memberships;
        this.consents = consents;
        this.organizations = organizations;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> export(UUID userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Utilisateur introuvable"));
        List<Application> owned = applications.findAllByUserId(userId);
        Map<String, Object> export = new LinkedHashMap<>();
        export.put("exportVersion", "2");
        export.put("exportedAt", java.time.Instant.now().toString());
        export.put("user", Map.of("id", user.id(), "email", user.email(), "role", user.role().name(),
                "createdAt", user.createdAt().toString()));
        profiles.findById(userId).ifPresent(profile -> export.put("identity", profile));
        plans.findById(userId).map(plan -> Map.of("plan", plan.plan().name(), "cvAnalysisUsed",
                plan.cvAnalysisUsed(), "limit", plan.plan().cvAnalysisLimit(),
                "resetAt", plan.cvAnalysisResetAt().toString()))
                .ifPresent(plan -> export.put("plan", plan));
        export.put("applications", owned.stream().map(application -> applicationExport(userId, application)).toList());
        export.put("contacts", contacts.findAllByUserId(userId));
        export.put("profile", profileRepository.load(userId));
        export.put("generatedCvs", cvs.findAllByUserId(userId));
        export.put("organizations", memberships.findAllByUserId(userId).stream()
                .map(membership -> {
                    Map<String, Object> value = new LinkedHashMap<>();
                    value.put("organizationId", membership.organizationId());
                    value.put("role", membership.role().name());
                    organizations.findById(membership.organizationId())
                            .ifPresent(organization -> value.put("name", organization.name()));
                    consents.findByOrganizationIdAndUserId(membership.organizationId(), userId)
                            .ifPresent(consent -> value.put("consent", Map.of("scope", consent.scope().name(),
                                    "active", consent.isActive())));
                    return value;
                }).toList());
        return export;
    }

    private Map<String, Object> applicationExport(UUID userId, Application application) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("application", application);
        value.put("history", history.findAllByApplicationId(application.id()));
        value.put("interactions", interactions.findAllByApplicationId(application.id()));
        value.put("reminders", reminders.findAllByApplicationId(application.id()));
        cvs.findLatestByApplicationIdAndUserId(application.id(), userId)
                .ifPresent(cv -> value.put("generatedCvId", cv.id()));
        return value;
    }

    @Transactional
    public void deleteAccount(UUID userId) {
        if (!users.existsById(userId)) {
            throw new ApiExceptions.NotFoundException("Utilisateur introuvable");
        }
        users.deleteById(userId);
    }
}
