package com.jobtrack.infrastructure.config;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jobtrack.application.port.AdminAllowlist;

@Component
public class ConfiguredAdminAllowlist implements AdminAllowlist {
    private final Set<String> emails;

    public ConfiguredAdminAllowlist(@Value("${jobtrack.admin.emails:}") String configuredEmails) {
        this.emails = Arrays.stream(configuredEmails.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean isAdmin(String email) {
        return email != null && emails.contains(email.trim().toLowerCase(Locale.ROOT));
    }

    @Override
    public Set<String> configuredEmails() {
        return emails;
    }
}
