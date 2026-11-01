package com.jobtrack.application.port;

import java.util.Optional;

import com.jobtrack.domain.organization.OrganizationInvitation;

public interface InvitationRepository {
    OrganizationInvitation save(OrganizationInvitation invitation);
    Optional<OrganizationInvitation> findByTokenHash(String tokenHash);
}
