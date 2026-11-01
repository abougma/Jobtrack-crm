package com.jobtrack.application.port;

import java.time.LocalDate;

import com.jobtrack.domain.application.ApplicationStatus;
import com.jobtrack.domain.application.ContractType;
import com.jobtrack.domain.application.WorkMode;

public record ApplicationFilter(
        ApplicationStatus status,
        String source,
        String search,
        ContractType contractType,
        WorkMode workMode,
        LocalDate from,
        LocalDate to) {

    public static ApplicationFilter empty() {
        return new ApplicationFilter(null, null, null, null, null, null, null);
    }
}
