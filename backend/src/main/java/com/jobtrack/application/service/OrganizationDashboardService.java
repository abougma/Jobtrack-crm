package com.jobtrack.application.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobtrack.application.port.*;
import com.jobtrack.domain.application.Application;
import com.jobtrack.domain.application.ApplicationStatus;
import com.jobtrack.domain.organization.*;
import com.jobtrack.domain.user.UserProfile;

@Service
public class OrganizationDashboardService {
    private final OrganizationService organizations;
    private final MembershipRepository memberships;
    private final GroupRepository groups;
    private final ConsentRepository consents;
    private final ApplicationRepository applications;
    private final UserProfileRepository profiles;
    private final ApplicationService applicationService;

    public OrganizationDashboardService(OrganizationService organizations, MembershipRepository memberships,
                                        GroupRepository groups, ConsentRepository consents,
                                        ApplicationRepository applications, UserProfileRepository profiles,
                                        ApplicationService applicationService) {
        this.organizations = organizations;
        this.memberships = memberships;
        this.groups = groups;
        this.consents = consents;
        this.applications = applications;
        this.profiles = profiles;
        this.applicationService = applicationService;
    }

    @Transactional(readOnly = true)
    public Snapshot get(UUID userId, UUID organizationId, UUID groupId) {
        organizations.requireMember(userId, organizationId);
        List<UUID> memberIds = memberIds(organizationId, groupId);
        Map<UUID, OrganizationConsent> activeConsents = consents.findActiveByOrganizationId(organizationId).stream()
                .filter(consent -> memberIds.contains(consent.userId()))
                .collect(Collectors.toMap(OrganizationConsent::userId, consent -> consent));
        List<Application> apps = applications.findAllByUserIdIn(activeConsents.keySet());
        long interviews = apps.stream().filter(application -> application.status().isInterview()).count();
        long offers = apps.stream().filter(application -> application.status() == ApplicationStatus.OFFER).count();
        long responses = apps.stream().filter(application -> application.status().isResponse()).count();

        List<GroupMetric> groupMetrics = groups.findAllByOrganizationId(organizationId).stream().map(group -> {
            List<UUID> groupMemberIds = groups.findMemberIds(group.id());
            Set<UUID> consenting = activeConsents.keySet().stream()
                    .filter(groupMemberIds::contains).collect(Collectors.toSet());
            List<Application> groupApps = apps.stream()
                    .filter(application -> consenting.contains(application.userId())).toList();
            long groupInterviews = groupApps.stream().filter(application -> application.status().isInterview()).count();
            return new GroupMetric(group.id(), group.name(), groupMemberIds.size(), groupApps.size(), groupInterviews);
        }).toList();

        List<MemberDetail> details = activeConsents.values().stream()
                .filter(consent -> consent.scope() == ConsentScope.APPLICATION_DETAILS)
                .map(consent -> {
                    UserProfile profile = profiles.findById(consent.userId()).orElse(null);
                    List<Application> own = apps.stream()
                            .filter(application -> application.userId().equals(consent.userId())).toList();
                    String displayName = displayName(profile);
                    return new MemberDetail(consent.userId(), displayName, own.size(),
                            own.stream().filter(application -> application.status().isInterview()).count(),
                            consent.scope().name());
                }).toList();

        return new Snapshot(memberIds.size(), activeConsents.size(), apps.size(), interviews, offers,
                rate(responses, apps.size()), rate(interviews, apps.size()), groupMetrics, details);
    }

    @Transactional(readOnly = true)
    public List<ApplicationService.ApplicationSummary> memberApplications(UUID requesterId, UUID organizationId,
                                                                          UUID memberUserId) {
        OrganizationMembership requester = organizations.requireMember(requesterId, organizationId);
        if (!requester.role().canViewSharedDetails()) {
            throw new ApiExceptions.ForbiddenException("Droits insuffisants pour consulter ce membre");
        }
        memberships.findByOrganizationIdAndUserId(organizationId, memberUserId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Membre introuvable"));
        OrganizationConsent consent = consents.findByOrganizationIdAndUserId(organizationId, memberUserId)
                .filter(OrganizationConsent::isActive)
                .orElseThrow(() -> new ApiExceptions.ForbiddenException("Ce membre n'a pas partagé le détail"));
        if (consent.scope() != ConsentScope.APPLICATION_DETAILS) {
            throw new ApiExceptions.ForbiddenException("Ce membre n'a partagé que des indicateurs agrégés");
        }
        return applicationService.list(memberUserId, ApplicationFilter.empty(), Pageable.unpaged()).getContent();
    }

    private List<UUID> memberIds(UUID organizationId, UUID groupId) {
        if (groupId == null) {
            return memberships.findAllByOrganizationId(organizationId).stream()
                    .map(OrganizationMembership::userId).toList();
        }
        OrganizationGroup group = groups.findByIdAndOrganizationId(groupId, organizationId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Groupe introuvable"));
        return groups.findMemberIds(group.id());
    }

    private String displayName(UserProfile profile) {
        if (profile == null) {
            return "Membre";
        }
        String first = profile.firstName() == null ? "" : profile.firstName();
        String last = profile.lastName() == null ? "" : profile.lastName();
        String value = (first + " " + (last.isBlank() ? "" : last.substring(0, 1) + ".")).trim();
        return value.isBlank() ? "Membre" : value;
    }

    private double rate(long numerator, long denominator) {
        return denominator == 0 ? 0 : Math.round(numerator * 1000.0 / denominator) / 10.0;
    }

    public record Snapshot(long members, long membersWithConsent, long applications, long interviews,
                           long offers, double responseRate, double interviewRate,
                           List<GroupMetric> groups, List<MemberDetail> memberDetails) {}
    public record GroupMetric(UUID id, String name, long members, long applications, long interviews) {}
    public record MemberDetail(UUID userId, String displayName, long applications, long interviews,
                               String consentScope) {}
}
