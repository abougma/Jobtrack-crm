package com.jobtrack.domain.application;

import java.util.EnumSet;
import java.util.Set;

public enum ApplicationStatus {
    TO_APPLY,
    CV_READY,
    APPLIED,
    FOLLOWED_UP,
    INTERVIEW,
    OFFER,
    REJECTED,
    NO_RESPONSE,
    WITHDRAWN;

    private static final Set<ApplicationStatus> RESPONSE_STATUSES =
            EnumSet.of(INTERVIEW, OFFER, REJECTED);
    private static final Set<ApplicationStatus> INTERVIEW_STATUSES =
            EnumSet.of(INTERVIEW, OFFER);
    private static final Set<ApplicationStatus> CLOSED_STATUSES =
            EnumSet.of(OFFER, REJECTED, NO_RESPONSE, WITHDRAWN);

    public boolean countsAsSent() {
        return this != TO_APPLY && this != CV_READY;
    }

    public boolean isResponse() {
        return RESPONSE_STATUSES.contains(this);
    }

    public boolean isInterview() {
        return INTERVIEW_STATUSES.contains(this);
    }

    public boolean isClosed() {
        return CLOSED_STATUSES.contains(this);
    }

    public boolean isActive() {
        return !isClosed();
    }
}
