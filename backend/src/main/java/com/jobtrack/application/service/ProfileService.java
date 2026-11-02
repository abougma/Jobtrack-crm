package com.jobtrack.application.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobtrack.application.port.ProfileRepository;
import com.jobtrack.domain.profile.*;

@Service
public class ProfileService {
    private final ProfileRepository profiles;

    public ProfileService(ProfileRepository profiles) {
        this.profiles = profiles;
    }

    @Transactional(readOnly = true)
    public ProfileSnapshot get(UUID userId) {
        return profiles.load(userId);
    }

    @Transactional
    public Experience addExperience(UUID userId, ExperienceDraft draft) {
        validateDates(draft.startedOn(), draft.endedOn());
        return profiles.saveExperience(Experience.create(userId, draft.company().trim(), draft.jobTitle().trim(),
                draft.startedOn(), draft.endedOn(), draft.description()));
    }

    @Transactional
    public Experience updateExperience(UUID userId, UUID id, ExperienceDraft draft) {
        validateDates(draft.startedOn(), draft.endedOn());
        Experience experience = profiles.findExperience(id, userId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Expérience introuvable"));
        return profiles.saveExperience(experience.update(draft.company().trim(), draft.jobTitle().trim(),
                draft.startedOn(), draft.endedOn(), draft.description()));
    }

    @Transactional
    public void deleteExperience(UUID userId, UUID id) {
        profiles.deleteExperience(profiles.findExperience(id, userId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Expérience introuvable")));
    }

    @Transactional
    public Education addEducation(UUID userId, EducationDraft draft) {
        validateDates(draft.startedOn(), draft.endedOn());
        return profiles.saveEducation(Education.create(userId, draft.institution().trim(), draft.degree().trim(),
                draft.startedOn(), draft.endedOn(), draft.description()));
    }

    @Transactional
    public Education updateEducation(UUID userId, UUID id, EducationDraft draft) {
        validateDates(draft.startedOn(), draft.endedOn());
        Education education = profiles.findEducation(id, userId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Formation introuvable"));
        return profiles.saveEducation(education.update(draft.institution().trim(), draft.degree().trim(),
                draft.startedOn(), draft.endedOn(), draft.description()));
    }

    @Transactional
    public void deleteEducation(UUID userId, UUID id) {
        profiles.deleteEducation(profiles.findEducation(id, userId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Formation introuvable")));
    }

    @Transactional
    public Project addProject(UUID userId, ProjectDraft draft) {
        return profiles.saveProject(Project.create(userId, draft.name().trim(), draft.description(),
                draft.technologies()));
    }

    @Transactional
    public Project updateProject(UUID userId, UUID id, ProjectDraft draft) {
        Project project = profiles.findProject(id, userId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Projet introuvable"));
        return profiles.saveProject(project.update(draft.name().trim(), draft.description(), draft.technologies()));
    }

    @Transactional
    public void deleteProject(UUID userId, UUID id) {
        profiles.deleteProject(profiles.findProject(id, userId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Projet introuvable")));
    }

    @Transactional
    public Skill addSkill(UUID userId, SkillDraft draft) {
        return profiles.saveSkill(Skill.create(userId, draft.name().trim(), draft.level(), draft.context(),
                draft.category().trim()));
    }

    @Transactional
    public Skill updateSkill(UUID userId, UUID id, SkillDraft draft) {
        Skill skill = profiles.findSkill(id, userId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Compétence introuvable"));
        return profiles.saveSkill(skill.update(draft.name().trim(), draft.level(), draft.context(),
                draft.category().trim()));
    }

    @Transactional
    public void deleteSkill(UUID userId, UUID id) {
        profiles.deleteSkill(profiles.findSkill(id, userId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Compétence introuvable")));
    }

    @Transactional
    public Language addLanguage(UUID userId, LanguageDraft draft) {
        return profiles.saveLanguage(Language.create(userId, draft.name().trim(), draft.level()));
    }

    @Transactional
    public Language updateLanguage(UUID userId, UUID id, LanguageDraft draft) {
        Language language = profiles.findLanguage(id, userId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Langue introuvable"));
        return profiles.saveLanguage(language.update(draft.name().trim(), draft.level()));
    }

    @Transactional
    public void deleteLanguage(UUID userId, UUID id) {
        profiles.deleteLanguage(profiles.findLanguage(id, userId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Langue introuvable")));
    }

    private void validateDates(LocalDate startedOn, LocalDate endedOn) {
        if (startedOn != null && endedOn != null && endedOn.isBefore(startedOn)) {
            throw new ApiExceptions.BadRequestException(
                    "La date de fin doit être postérieure ou égale à la date de début");
        }
    }

    public record ExperienceDraft(String company, String jobTitle, LocalDate startedOn, LocalDate endedOn,
                                  String description) {}
    public record EducationDraft(String institution, String degree, LocalDate startedOn, LocalDate endedOn,
                                 String description) {}
    public record ProjectDraft(String name, String description, String technologies) {}
    public record SkillDraft(String name, SkillLevel level, SkillContext context, String category) {}
    public record LanguageDraft(String name, LanguageLevel level) {}
}
