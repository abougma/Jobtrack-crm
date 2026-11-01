package com.jobtrack.infrastructure.persistence.adapter;

import com.jobtrack.domain.application.*;
import com.jobtrack.domain.crm.*;
import com.jobtrack.domain.organization.*;
import com.jobtrack.domain.plan.UserPlan;
import com.jobtrack.domain.profile.*;
import com.jobtrack.domain.user.*;
import com.jobtrack.infrastructure.persistence.entity.*;

final class DomainMapper {
    private DomainMapper() {}

    static User toDomain(UserJpaEntity entity) {
        return new User(entity.getId(), entity.getEmail(), entity.getPasswordHash(), entity.getRole(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }

    static UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(user.id(), user.email(), user.passwordHash(), user.role(),
                user.createdAt(), user.updatedAt());
    }

    static UserProfile toDomain(UserProfileJpaEntity entity) {
        return new UserProfile(entity.getUserId(), entity.getFirstName(), entity.getLastName(),
                entity.getHeadline(), entity.getLocation(), entity.getUpdatedAt());
    }

    static UserProfileJpaEntity toEntity(UserProfile profile) {
        return new UserProfileJpaEntity(profile.userId(), profile.firstName(), profile.lastName(),
                profile.headline(), profile.location(), profile.updatedAt());
    }

    static RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return new RefreshToken(entity.getId(), entity.getUserId(), entity.getTokenHash(),
                entity.getExpiresAt(), entity.getRevokedAt());
    }

    static RefreshTokenJpaEntity toEntity(RefreshToken token) {
        return new RefreshTokenJpaEntity(token.id(), token.userId(), token.tokenHash(),
                token.expiresAt(), token.revokedAt());
    }

    static UserPlan toDomain(UserPlanJpaEntity entity) {
        return new UserPlan(entity.getUserId(), entity.getPlan(), entity.getCvAnalysisUsed(),
                entity.getCvAnalysisResetAt(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    static UserPlanJpaEntity toEntity(UserPlan plan) {
        return new UserPlanJpaEntity(plan.userId(), plan.plan(), plan.cvAnalysisUsed(),
                plan.cvAnalysisResetAt(), plan.createdAt(), plan.updatedAt());
    }

    static Application toDomain(ApplicationJpaEntity entity) {
        return new Application(entity.getId(), entity.getUserId(), entity.getContactId(), entity.getCompany(),
                entity.getPosition(), entity.getLocation(), entity.getWorkMode(), entity.getContractType(),
                entity.getSalaryMin(), entity.getSalaryMax(), entity.getSource(), entity.getOfferUrl(),
                entity.getStatus(), entity.getAppliedAt(), entity.getNotes(), entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    static ApplicationJpaEntity toEntity(Application application) {
        return new ApplicationJpaEntity(application.id(), application.userId(), application.contactId(),
                application.company(), application.position(), application.location(), application.workMode(),
                application.contractType(), application.salaryMin(), application.salaryMax(), application.source(),
                application.offerUrl(), application.status(), application.appliedAt(), application.notes(),
                application.createdAt(), application.updatedAt());
    }

    static StatusChange toDomain(ApplicationStatusHistoryJpaEntity entity) {
        return new StatusChange(entity.getId(), entity.getApplicationId(), entity.getPreviousStatus(),
                entity.getNewStatus(), entity.getChangedAt());
    }

    static ApplicationStatusHistoryJpaEntity toEntity(StatusChange change) {
        return new ApplicationStatusHistoryJpaEntity(change.id(), change.applicationId(),
                change.previousStatus(), change.newStatus(), change.changedAt());
    }

    static Contact toDomain(ContactJpaEntity entity) {
        return new Contact(entity.getId(), entity.getUserId(), entity.getName(), entity.getRole(),
                entity.getLinkedinUrl(), entity.getEmail(), entity.getCreatedAt());
    }

    static ContactJpaEntity toEntity(Contact contact) {
        return new ContactJpaEntity(contact.id(), contact.userId(), contact.name(), contact.role(),
                contact.linkedinUrl(), contact.email(), contact.createdAt());
    }

    static Interaction toDomain(InteractionJpaEntity entity) {
        return new Interaction(entity.getId(), entity.getApplicationId(), entity.getType(),
                entity.getMessage(), entity.getOccurredAt());
    }

    static InteractionJpaEntity toEntity(Interaction interaction) {
        return new InteractionJpaEntity(interaction.id(), interaction.applicationId(), interaction.type(),
                interaction.message(), interaction.occurredAt());
    }

    static Reminder toDomain(ReminderJpaEntity entity) {
        return new Reminder(entity.getId(), entity.getApplicationId(), entity.getTitle(),
                entity.getDueAt(), entity.isCompleted());
    }

    static ReminderJpaEntity toEntity(Reminder reminder) {
        return new ReminderJpaEntity(reminder.id(), reminder.applicationId(), reminder.title(),
                reminder.dueAt(), reminder.completed());
    }

    static Experience toDomain(ProfileExperienceJpaEntity entity) {
        return new Experience(entity.getId(), entity.getUserId(), entity.getCompany(), entity.getJobTitle(),
                entity.getStartedOn(), entity.getEndedOn(), entity.getDescription());
    }

    static ProfileExperienceJpaEntity toEntity(Experience experience) {
        return new ProfileExperienceJpaEntity(experience.id(), experience.userId(), experience.company(),
                experience.jobTitle(), experience.startedOn(), experience.endedOn(), experience.description());
    }

    static Education toDomain(ProfileEducationJpaEntity entity) {
        return new Education(entity.getId(), entity.getUserId(), entity.getInstitution(), entity.getDegree(),
                entity.getStartedOn(), entity.getEndedOn(), entity.getDescription());
    }

    static ProfileEducationJpaEntity toEntity(Education education) {
        return new ProfileEducationJpaEntity(education.id(), education.userId(), education.institution(),
                education.degree(), education.startedOn(), education.endedOn(), education.description());
    }

    static Project toDomain(ProfileProjectJpaEntity entity) {
        return new Project(entity.getId(), entity.getUserId(), entity.getName(), entity.getDescription(),
                entity.getTechnologies());
    }

    static ProfileProjectJpaEntity toEntity(Project project) {
        return new ProfileProjectJpaEntity(project.id(), project.userId(), project.name(),
                project.description(), project.technologies());
    }

    static Skill toDomain(ProfileSkillJpaEntity entity) {
        return new Skill(entity.getId(), entity.getUserId(), entity.getName(), entity.getLevel(), entity.getContext());
    }

    static ProfileSkillJpaEntity toEntity(Skill skill) {
        return new ProfileSkillJpaEntity(skill.id(), skill.userId(), skill.name(), skill.level(), skill.context());
    }

    static Language toDomain(ProfileLanguageJpaEntity entity) {
        return new Language(entity.getId(), entity.getUserId(), entity.getName(), entity.getLevel());
    }

    static ProfileLanguageJpaEntity toEntity(Language language) {
        return new ProfileLanguageJpaEntity(language.id(), language.userId(), language.name(), language.level());
    }

    static Organization toDomain(OrganizationJpaEntity entity) {
        return new Organization(entity.getId(), entity.getName(), entity.getCreatedAt());
    }

    static OrganizationJpaEntity toEntity(Organization organization) {
        return new OrganizationJpaEntity(organization.id(), organization.name(), organization.createdAt());
    }

    static OrganizationMembership toDomain(OrganizationMembershipJpaEntity entity) {
        return new OrganizationMembership(entity.getId(), entity.getOrganizationId(), entity.getUserId(),
                entity.getRole(), entity.getCreatedAt());
    }

    static OrganizationMembershipJpaEntity toEntity(OrganizationMembership membership) {
        return new OrganizationMembershipJpaEntity(membership.id(), membership.organizationId(),
                membership.userId(), membership.role(), membership.createdAt());
    }

    static OrganizationGroup toDomain(OrganizationGroupJpaEntity entity) {
        return new OrganizationGroup(entity.getId(), entity.getOrganizationId(), entity.getName(), entity.getCreatedAt());
    }

    static OrganizationGroupJpaEntity toEntity(OrganizationGroup group) {
        return new OrganizationGroupJpaEntity(group.id(), group.organizationId(), group.name(), group.createdAt());
    }

    static OrganizationConsent toDomain(OrganizationConsentJpaEntity entity) {
        return new OrganizationConsent(entity.getId(), entity.getOrganizationId(), entity.getUserId(),
                entity.getScope(), entity.getGrantedAt(), entity.getRevokedAt());
    }

    static OrganizationConsentJpaEntity toEntity(OrganizationConsent consent) {
        return new OrganizationConsentJpaEntity(consent.id(), consent.organizationId(), consent.userId(),
                consent.scope(), consent.grantedAt(), consent.revokedAt());
    }

    static OrganizationInvitation toDomain(OrganizationInvitationJpaEntity entity) {
        return new OrganizationInvitation(entity.getId(), entity.getOrganizationId(), entity.getEmail(),
                entity.getRole(), entity.getTokenHash(), entity.getExpiresAt(), entity.getAcceptedAt(),
                entity.getCreatedBy(), entity.getCreatedAt());
    }

    static OrganizationInvitationJpaEntity toEntity(OrganizationInvitation invitation) {
        return new OrganizationInvitationJpaEntity(invitation.id(), invitation.organizationId(), invitation.email(),
                invitation.role(), invitation.tokenHash(), invitation.expiresAt(), invitation.acceptedAt(),
                invitation.createdBy(), invitation.createdAt());
    }
}
