package com.jobtrack.domain.profile;

import java.util.List;

public record ProfileSnapshot(
        List<Experience> experiences,
        List<Education> educations,
        List<Project> projects,
        List<Skill> skills,
        List<Language> languages) {

    public ProfileSnapshot {
        experiences = List.copyOf(experiences);
        educations = List.copyOf(educations);
        projects = List.copyOf(projects);
        skills = List.copyOf(skills);
        languages = List.copyOf(languages);
    }
}
