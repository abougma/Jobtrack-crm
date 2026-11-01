package com.jobtrack.domain.cv;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GeneratedCv(
        UUID id,
        UUID userId,
        UUID applicationId,
        String title,
        String headline,
        String summary,
        List<String> skills,
        List<CvExperience> experiences,
        List<CvProject> projects,
        List<CvEducation> educations,
        List<String> missingInformation,
        int score,
        Instant createdAt) {

    public GeneratedCv {
        skills = List.copyOf(skills);
        experiences = List.copyOf(experiences);
        projects = List.copyOf(projects);
        educations = List.copyOf(educations);
        missingInformation = List.copyOf(missingInformation);
    }
}
