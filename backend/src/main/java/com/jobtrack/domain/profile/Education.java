package com.jobtrack.domain.profile;

import java.time.LocalDate;
import java.util.UUID;

public record Education(
        UUID id,
        UUID userId,
        String institution,
        String degree,
        LocalDate startedOn,
        LocalDate endedOn,
        String description) {

    public static Education create(UUID userId, String institution, String degree, LocalDate startedOn,
                                   LocalDate endedOn, String description) {
        return new Education(UUID.randomUUID(), userId, institution, degree, startedOn, endedOn, description);
    }

    public Education update(String institution, String degree, LocalDate startedOn, LocalDate endedOn, String description) {
        return new Education(id, userId, institution, degree, startedOn, endedOn, description);
    }
}
