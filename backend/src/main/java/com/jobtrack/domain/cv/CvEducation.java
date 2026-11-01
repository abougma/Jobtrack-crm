package com.jobtrack.domain.cv;

import java.time.LocalDate;

public record CvEducation(String degree, String institution, LocalDate startedOn, LocalDate endedOn) {}
