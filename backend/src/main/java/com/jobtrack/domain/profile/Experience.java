package com.jobtrack.domain.profile;

import java.time.LocalDate;
import java.util.UUID;

public record Experience(
        UUID id,
        UUID userId,
        String company,
        String jobTitle,
        LocalDate startedOn,
        LocalDate endedOn,
        String description) {

    public static Experience create(UUID userId, String company, String jobTitle, LocalDate startedOn,
                                    LocalDate endedOn, String description) {
        return new Experience(UUID.randomUUID(), userId, company, jobTitle, startedOn, endedOn, description);
    }

    public Experience update(String company, String jobTitle, LocalDate startedOn, LocalDate endedOn, String description) {
        return new Experience(id, userId, company, jobTitle, startedOn, endedOn, description);
    }
}
