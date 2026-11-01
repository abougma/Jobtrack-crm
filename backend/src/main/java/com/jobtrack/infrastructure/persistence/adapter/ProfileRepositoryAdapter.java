package com.jobtrack.infrastructure.persistence.adapter;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.jobtrack.application.port.ProfileRepository;
import com.jobtrack.domain.profile.*;
import com.jobtrack.infrastructure.persistence.repository.*;

@Component
public class ProfileRepositoryAdapter implements ProfileRepository {
    private final SpringProfileExperienceRepository experiences;
    private final SpringProfileEducationRepository educations;
    private final SpringProfileProjectRepository projects;
    private final SpringProfileSkillRepository skills;
    private final SpringProfileLanguageRepository languages;

    public ProfileRepositoryAdapter(SpringProfileExperienceRepository experiences,
                                    SpringProfileEducationRepository educations,
                                    SpringProfileProjectRepository projects,
                                    SpringProfileSkillRepository skills,
                                    SpringProfileLanguageRepository languages) {
        this.experiences = experiences;
        this.educations = educations;
        this.projects = projects;
        this.skills = skills;
        this.languages = languages;
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileSnapshot load(UUID userId) {
        return new ProfileSnapshot(
                experiences.findAllByUserIdOrderByStartedOnDesc(userId).stream().map(DomainMapper::toDomain).toList(),
                educations.findAllByUserIdOrderByStartedOnDesc(userId).stream().map(DomainMapper::toDomain).toList(),
                projects.findAllByUserIdOrderByNameAsc(userId).stream().map(DomainMapper::toDomain).toList(),
                skills.findAllByUserIdOrderByNameAsc(userId).stream().map(DomainMapper::toDomain).toList(),
                languages.findAllByUserIdOrderByNameAsc(userId).stream().map(DomainMapper::toDomain).toList());
    }

    @Override
    public Optional<Experience> findExperience(UUID id, UUID userId) {
        return experiences.findByIdAndUserId(id, userId).map(DomainMapper::toDomain);
    }

    @Override
    public Experience saveExperience(Experience experience) {
        return DomainMapper.toDomain(experiences.save(DomainMapper.toEntity(experience)));
    }

    @Override
    public void deleteExperience(Experience experience) {
        experiences.deleteById(experience.id());
    }

    @Override
    public Optional<Education> findEducation(UUID id, UUID userId) {
        return educations.findByIdAndUserId(id, userId).map(DomainMapper::toDomain);
    }

    @Override
    public Education saveEducation(Education education) {
        return DomainMapper.toDomain(educations.save(DomainMapper.toEntity(education)));
    }

    @Override
    public void deleteEducation(Education education) {
        educations.deleteById(education.id());
    }

    @Override
    public Optional<Project> findProject(UUID id, UUID userId) {
        return projects.findByIdAndUserId(id, userId).map(DomainMapper::toDomain);
    }

    @Override
    public Project saveProject(Project project) {
        return DomainMapper.toDomain(projects.save(DomainMapper.toEntity(project)));
    }

    @Override
    public void deleteProject(Project project) {
        projects.deleteById(project.id());
    }

    @Override
    public Optional<Skill> findSkill(UUID id, UUID userId) {
        return skills.findByIdAndUserId(id, userId).map(DomainMapper::toDomain);
    }

    @Override
    public Skill saveSkill(Skill skill) {
        return DomainMapper.toDomain(skills.save(DomainMapper.toEntity(skill)));
    }

    @Override
    public void deleteSkill(Skill skill) {
        skills.deleteById(skill.id());
    }

    @Override
    public Optional<Language> findLanguage(UUID id, UUID userId) {
        return languages.findByIdAndUserId(id, userId).map(DomainMapper::toDomain);
    }

    @Override
    public Language saveLanguage(Language language) {
        return DomainMapper.toDomain(languages.save(DomainMapper.toEntity(language)));
    }

    @Override
    public void deleteLanguage(Language language) {
        languages.deleteById(language.id());
    }
}
