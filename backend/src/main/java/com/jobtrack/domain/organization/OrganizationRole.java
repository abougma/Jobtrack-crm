package com.jobtrack.domain.organization;

public enum OrganizationRole {
    ORG_ADMIN,
    ORG_COACH,
    MEMBER;

    public boolean canManageOrganization() {
        return this == ORG_ADMIN;
    }

    public boolean canViewSharedDetails() {
        return this == ORG_ADMIN || this == ORG_COACH;
    }
}
