package com.jobtrack.application.port;

import java.util.Set;

/**
 * Liste d'adresses e-mail autorisées à obtenir le rôle PLATFORM_ADMIN.
 * Alimentée par configuration (JOBTRACK_ADMIN_EMAILS), jamais codée en dur.
 */
public interface AdminAllowlist {
    boolean isAdmin(String email);
    Set<String> configuredEmails();
}
