package com.jobtrack.domain.cv;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.UUID;

public record GeneratedCv(
        UUID id,
        UUID userId,
        UUID applicationId,
        String title,
        String fullName,
        String location,
        String email,
        String phone,
        String linkedinUrl,
        String portfolioUrl,
        String headline,
        String summary,
        List<String> skills,
        Map<String, List<String>> skillGroups,
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
        if (skillGroups == null) {
            skillGroups = Map.of();
        } else {
            Map<String, List<String>> copiedGroups = new LinkedHashMap<>();
            skillGroups.forEach((key, value) -> copiedGroups.put(key, List.copyOf(value)));
            skillGroups = java.util.Collections.unmodifiableMap(copiedGroups);
        }
    }
}
