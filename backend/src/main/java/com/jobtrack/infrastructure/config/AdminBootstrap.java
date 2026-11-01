package com.jobtrack.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.jobtrack.application.port.AdminAllowlist;
import com.jobtrack.application.port.UserRepository;
import com.jobtrack.domain.user.User;
import com.jobtrack.domain.user.UserRole;

/**
 * Promeut au démarrage les comptes existants listés dans JOBTRACK_ADMIN_EMAILS.
 * Idempotent : un compte déjà administrateur n'est pas modifié.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository users;
    private final AdminAllowlist allowlist;

    public AdminBootstrap(UserRepository users, AdminAllowlist allowlist) {
        this.users = users;
        this.allowlist = allowlist;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (allowlist.configuredEmails().isEmpty()) {
            return;
        }
        int promoted = 0;
        for (String email : allowlist.configuredEmails()) {
            User user = users.findByEmail(email).orElse(null);
            if (user != null && user.role() != UserRole.PLATFORM_ADMIN) {
                users.save(user.withRole(UserRole.PLATFORM_ADMIN));
                promoted++;
            }
        }
        if (promoted > 0) {
            log.info("{} compte(s) existant(s) promu(s) administrateur plateforme.", promoted);
        }
    }
}
