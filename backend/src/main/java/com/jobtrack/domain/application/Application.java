package com.jobtrack.domain.application;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record Application(
        UUID id,
        UUID userId,
        UUID contactId,
        String company,
        String position,
        String location,
        WorkMode workMode,
        ContractType contractType,
        Integer salaryMin,
        Integer salaryMax,
        String source,
        String offerUrl,
        ApplicationStatus status,
        LocalDate appliedAt,
        String notes,
        Instant createdAt,
        Instant updatedAt) {

    public Application {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(company, "company");
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(status, "status");
    }

    public static Application draft(
            UUID userId, UUID contactId, String company, String position, String location,
            WorkMode workMode, ContractType contractType, Integer salaryMin, Integer salaryMax,
            String source, String offerUrl, LocalDate appliedAt, String notes, ApplicationStatus status) {
        Instant now = Instant.now();
        return new Application(UUID.randomUUID(), userId, contactId, company, position, location,
                workMode, contractType, salaryMin, salaryMax, source, offerUrl,
                status == null ? ApplicationStatus.TO_APPLY : status, appliedAt, notes, now, now);
    }

    public Application updateDetails(
            UUID contactId, String company, String position, String location,
            WorkMode workMode, ContractType contractType, Integer salaryMin, Integer salaryMax,
            String source, String offerUrl, LocalDate appliedAt, String notes) {
        return new Application(id, userId, contactId, company, position, location, workMode, contractType,
                salaryMin, salaryMax, source, offerUrl, status, appliedAt, notes, createdAt, Instant.now());
    }

    public Application changeStatus(ApplicationStatus newStatus) {
        Objects.requireNonNull(newStatus, "newStatus");
        return new Application(id, userId, contactId, company, position, location, workMode, contractType,
                salaryMin, salaryMax, source, offerUrl, newStatus, appliedAt, notes, createdAt, Instant.now());
    }
}
