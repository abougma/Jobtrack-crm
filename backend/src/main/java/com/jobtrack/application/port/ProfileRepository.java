package com.jobtrack.application.port;

import java.util.Optional;
import java.util.UUID;

import com.jobtrack.domain.profile.*;

public interface ProfileRepository {
    ProfileSnapshot load(UUID userId);

    Optional<Experience> findExperience(UUID id, UUID userId);
    Experience saveExperience(Experience experience);
    void deleteExperience(Experience experience);

    Optional<Education> findEducation(UUID id, UUID userId);
    Education saveEducation(Education education);
    void deleteEducation(Education education);

    Optional<Project> findProject(UUID id, UUID userId);
    Project saveProject(Project project);
    void deleteProject(Project project);

    Optional<Skill> findSkill(UUID id, UUID userId);
    Skill saveSkill(Skill skill);
    void deleteSkill(Skill skill);

    Optional<Language> findLanguage(UUID id, UUID userId);
    Language saveLanguage(Language language);
    void deleteLanguage(Language language);
}
