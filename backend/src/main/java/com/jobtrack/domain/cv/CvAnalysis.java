package com.jobtrack.domain.cv;

import java.util.List;

public record CvAnalysis(
        int score,
        List<String> coveredSkills,
        List<String> missingSkills,
        List<String> missingInformation,
        List<String> alerts,
        String experienceGap,
        List<String> suggestions) {

    public CvAnalysis {
        coveredSkills = List.copyOf(coveredSkills);
        missingSkills = List.copyOf(missingSkills);
        missingInformation = List.copyOf(missingInformation);
        alerts = List.copyOf(alerts);
        suggestions = List.copyOf(suggestions);
    }
}
