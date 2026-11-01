package com.jobtrack.application.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobtrack.application.port.*;
import com.jobtrack.domain.organization.*;
import com.jobtrack.domain.user.User;
import com.jobtrack.domain.user.UserProfile;

@Service
public class OrganizationService {
    private final OrganizationRepository organizations;
    private final MembershipRepository memberships;
    private final GroupRepository groups;
    private final ConsentRepository consents;
    private final InvitationRepository invitations;
    private final UserRepository users;
    private final UserProfileRepository profiles;
    private final TokenGenerator tokens;

    public OrganizationService(OrganizationRepository organizations, MembershipRepository memberships,
                               GroupRepository groups, ConsentRepository consents,
                               InvitationRepository invitations, UserRepository users,
                               UserProfileRepository profiles, TokenGenerator tokens) {
        this.organizations = organizations;
        this.memberships = memberships;
        this.groups = groups;
        this.consents = consents;
        this.invitations = invitations;
        this.users = users;
        this.profiles = profiles;
        this.tokens = tokens;
    }

    @Transactional
    public OrganizationView create(UUID userId, String name) {
        Organization organization = organizations.save(Organization.create(name.trim()));
        memberships.save(OrganizationMembership.create(organization.id(), userId, OrganizationRole.ORG_ADMIN));
        return view(organization, OrganizationRole.ORG_ADMIN);
    }

    @Transactional(readOnly = true)
    public List<OrganizationView> list(UUID userId) {
        List<OrganizationMembership> own = memberships.findAllByUserId(userId);
        if (own.isEmpty()) {
            return List.of();
        }
        Map<UUID, Organization> byId = organizations.findAllByIdIn(
                        own.stream().map(OrganizationMembership::organizationId).toList()).stream()
                .collect(Collectors.toMap(Organization::id, organization -> organization));
        return own.stream().map(membership -> {
            Organization organization = byId.get(membership.organizationId());
            return organization == null ? null : view(organization, membership.role());
        }).filter(Objects::nonNull).toList();
    }

    @Transactional(readOnly = true)
    public OrganizationView get(UUID userId, UUID organizationId) {
        OrganizationMembership membership = requireMember(userId, organizationId);
        return view(requireOrganization(organizationId), membership.role());
    }

    @Transactional
    public GroupView createGroup(UUID userId, UUID organizationId, String name) {
        requireAdmin(userId, organizationId);
        OrganizationGroup group = groups.save(OrganizationGroup.create(organizationId, name.trim()));
        return new GroupView(group.id(), group.name(), 0);
    }

    @Transactional(readOnly = true)
    public List<GroupView> listGroups(UUID userId, UUID organizationId) {
        requireMember(userId, organizationId);
        return groups.findAllByOrganizationId(organizationId).stream()
                .map(group -> new GroupView(group.id(), group.name(), groups.countMembers(group.id())))
                .toList();
    }

    @Transactional
    public InvitationResult invite(UUID userId, UUID organizationId, String email, OrganizationRole role) {
        requireAdmin(userId, organizationId);
        String rawToken = tokens.generate();
        Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);
        invitations.save(OrganizationInvitation.create(organizationId, email.trim().toLowerCase(Locale.ROOT),
                role, tokens.hash(rawToken), expiresAt, userId));
        return new InvitationResult(rawToken, expiresAt);
    }

    @Transactional
    public OrganizationView join(UUID userId, String rawToken) {
        OrganizationInvitation invitation = invitations.findByTokenHash(tokens.hash(rawToken))
                .filter(candidate -> candidate.isUsable(Instant.now()))
                .orElseThrow(() -> new ApiExceptions.BadRequestException("Invitation invalide ou expirée"));
        User user = users.findById(userId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Utilisateur introuvable"));
        if (!invitation.email().equalsIgnoreCase(user.email())) {
            throw new ApiExceptions.ForbiddenException("Cette invitation ne correspond pas à votre adresse e-mail");
        }
        OrganizationMembership membership = memberships
                .findByOrganizationIdAndUserId(invitation.organizationId(), userId)
                .orElseGet(() -> memberships.save(OrganizationMembership.create(
                        invitation.organizationId(), userId, invitation.role())));
        invitations.save(invitation.accept());
        return view(requireOrganization(invitation.organizationId()), membership.role());
    }

    @Transactional(readOnly = true)
    public List<MemberView> listMembers(UUID userId, UUID organizationId) {
        OrganizationMembership requester = requireMember(userId, organizationId);
        if (!requester.role().canViewSharedDetails()) {
            throw new ApiExceptions.ForbiddenException("Droits insuffisants pour consulter les membres");
        }
        Map<UUID, OrganizationConsent> activeConsents = consents.findActiveByOrganizationId(organizationId).stream()
                .collect(Collectors.toMap(OrganizationConsent::userId, consent -> consent));
        return memberships.findAllByOrganizationId(organizationId).stream().map(membership -> {
            User member = users.findById(membership.userId()).orElse(null);
            UserProfile profile = profiles.findById(membership.userId()).orElse(null);
            OrganizationConsent consent = activeConsents.get(membership.userId());
            return new MemberView(membership.userId(), member == null ? null : member.email(),
                    profile == null ? null : profile.firstName(), profile == null ? null : profile.lastName(),
                    membership.role().name(), consent == null ? null : consent.scope().name(),
                    membership.createdAt());
        }).toList();
    }

    @Transactional
    public void leave(UUID userId, UUID organizationId) {
        OrganizationMembership membership = requireMember(userId, organizationId);
        if (membership.role() == OrganizationRole.ORG_ADMIN) {
            long admins = memberships.findAllByOrganizationId(organizationId).stream()
                    .filter(item -> item.role() == OrganizationRole.ORG_ADMIN).count();
            if (admins <= 1) {
                throw new ApiExceptions.ConflictException(
                        "Le dernier administrateur ne peut pas quitter l'organisation");
            }
        }
        memberships.delete(membership);
    }

    @Transactional(readOnly = true)
    public ConsentView getConsent(UUID userId, UUID organizationId) {
        requireMember(userId, organizationId);
        return consents.findByOrganizationIdAndUserId(organizationId, userId)
                .filter(OrganizationConsent::isActive)
                .map(consent -> new ConsentView(consent.scope().name(), true))
                .orElse(new ConsentView(null, false));
    }

    @Transactional
    public ConsentView updateConsent(UUID userId, UUID organizationId, ConsentScope scope) {
        requireMember(userId, organizationId);
        OrganizationConsent consent = consents.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseGet(() -> OrganizationConsent.grant(organizationId, userId, scope));
        consent = consents.save(consent.updateScope(scope));
        return new ConsentView(consent.scope().name(), consent.isActive());
    }

    @Transactional
    public void revokeConsent(UUID userId, UUID organizationId) {
        requireMember(userId, organizationId);
        consents.findByOrganizationIdAndUserId(organizationId, userId)
                .ifPresent(consent -> consents.save(consent.revoke()));
    }

    @Transactional(readOnly = true)
    public OrganizationMembership requireMember(UUID userId, UUID organizationId) {
        return memberships.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new ApiExceptions.ForbiddenException("Accès organisation refusé"));
    }

    @Transactional(readOnly = true)
    public OrganizationMembership requireAdmin(UUID userId, UUID organizationId) {
        OrganizationMembership membership = requireMember(userId, organizationId);
        if (membership.role() != OrganizationRole.ORG_ADMIN) {
            throw new ApiExceptions.ForbiddenException("Droits administrateur requis");
        }
        return membership;
    }

    private Organization requireOrganization(UUID organizationId) {
        return organizations.findById(organizationId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Organisation introuvable"));
    }

    private OrganizationView view(Organization organization, OrganizationRole role) {
        List<GroupView> groupViews = groups.findAllByOrganizationId(organization.id()).stream()
                .map(group -> new GroupView(group.id(), group.name(), groups.countMembers(group.id())))
                .toList();
        return new OrganizationView(organization.id(), organization.name(), role.name(),
                memberships.countByOrganizationId(organization.id()), organization.createdAt(), groupViews);
    }

    public record OrganizationView(UUID id, String name, String role, long memberCount,
                                   Instant createdAt, List<GroupView> groups) {}
    public record GroupView(UUID id, String name, long memberCount) {}
    public record MemberView(UUID userId, String email, String firstName, String lastName,
                             String role, String consentScope, Instant joinedAt) {}
    public record InvitationResult(String token, Instant expiresAt) {}
    public record ConsentView(String scope, boolean active) {}
}
