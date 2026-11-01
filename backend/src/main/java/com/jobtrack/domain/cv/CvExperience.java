package com.jobtrack.domain.cv;

import java.time.LocalDate;

public record CvExperience(
        String jobTitle,
        String company,
        LocalDate startedOn,
        LocalDate endedOn,
        String description) {}
