package com.jobtrack.application.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobtrack.application.port.AccessTokenIssuer;
import com.jobtrack.application.port.AdminAllowlist;
import com.jobtrack.application.port.PasswordHasher;
import com.jobtrack.application.port.RefreshTokenRepository;
import com.jobtrack.application.port.TokenGenerator;
import com.jobtrack.application.port.UserPlanRepository;
import com.jobtrack.application.port.UserProfileRepository;
import com.jobtrack.application.port.UserRepository;
import com.jobtrack.domain.plan.UserPlan;
import com.jobtrack.domain.user.RefreshToken;
import com.jobtrack.domain.user.User;
import com.jobtrack.domain.user.UserProfile;
import com.jobtrack.domain.user.UserRole;

@Service
public class AuthService {
    private final UserRepository users;
    private final UserProfileRepository profiles;
    private final UserPlanRepository plans;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordHasher passwordHasher;
    private final TokenGenerator tokenGenerator;
    private final AccessTokenIssuer accessTokens;
    private final UserService userService;
    private final AdminAllowlist adminAllowlist;
    private final EmailService emailService;
    private final long refreshTokenDays;

    public AuthService(UserRepository users, UserProfileRepository profiles, UserPlanRepository plans,
                       RefreshTokenRepository refreshTokens, PasswordHasher passwordHasher,
                       TokenGenerator tokenGenerator, AccessTokenIssuer accessTokens,
                       UserService userService, AdminAllowlist adminAllowlist,
                       EmailService emailService,
                       @Value("${jobtrack.security.refresh-token-days}") long refreshTokenDays) {
        this.users = users;
        this.profiles = profiles;
        this.plans = plans;
        this.refreshTokens = refreshTokens;
        this.passwordHasher = passwordHasher;
        this.tokenGenerator = tokenGenerator;
        this.accessTokens = accessTokens;
        this.userService = userService;
        this.adminAllowlist = adminAllowlist;
        this.emailService = emailService;
        this.refreshTokenDays = refreshTokenDays;
    }

    @Transactional
    public AuthResult register(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        if (users.existsByEmail(normalizedEmail)) {
            throw new ApiExceptions.ConflictException("Cette adresse e-mail est déjà utilisée");
        }
        User user = User.create(normalizedEmail, passwordHasher.hash(password));
        if (adminAllowlist.isAdmin(normalizedEmail)) {
            user = user.withRole(UserRole.PLATFORM_ADMIN);
        }
        user = users.save(user);
        profiles.save(UserProfile.empty(user.id()));
        plans.save(UserPlan.create(user.id()));
        emailService.sendWelcome(user.email());
        return issueTokens(user);
    }

    @Transactional
    public AuthResult login(String email, String password) {
        User user = users.findByEmail(normalizeEmail(email))
                .filter(candidate -> passwordHasher.matches(password, candidate.passwordHash()))
                .orElseThrow(() -> new ApiExceptions.UnauthorizedException("Identifiants invalides"));
        return issueTokens(user);
    }

    @Transactional
    public AuthResult refresh(String rawRefreshToken) {
        RefreshToken stored = refreshTokens.findByTokenHash(tokenGenerator.hash(rawRefreshToken))
                .filter(token -> token.isUsable(Instant.now()))
                .orElseThrow(() -> new ApiExceptions.UnauthorizedException("Refresh token invalide ou expiré"));
        refreshTokens.save(stored.revoke());
        User user = users.findById(stored.userId())
                .orElseThrow(() -> new ApiExceptions.UnauthorizedException("Utilisateur introuvable"));
        return issueTokens(user);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokens.findByTokenHash(tokenGenerator.hash(rawRefreshToken))
                .ifPresent(token -> refreshTokens.save(token.revoke()));
    }

    private AuthResult issueTokens(User user) {
        String rawRefreshToken = tokenGenerator.generate();
        refreshTokens.save(RefreshToken.create(user.id(), tokenGenerator.hash(rawRefreshToken),
                Instant.now().plus(refreshTokenDays, ChronoUnit.DAYS)));
        return new AuthResult(accessTokens.issue(user), rawRefreshToken, "Bearer",
                accessTokens.accessTokenSeconds(), userService.view(user));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public record AuthResult(String accessToken, String refreshToken, String tokenType,
                             long expiresIn, UserService.UserView user) {}
}
