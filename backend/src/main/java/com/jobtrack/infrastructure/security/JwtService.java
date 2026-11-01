package com.jobtrack.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jobtrack.application.port.AccessTokenIssuer;
import com.jobtrack.domain.user.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService implements AccessTokenIssuer {
    private final SecretKey key;
    private final long accessTokenSeconds;

    public JwtService(@Value("${jobtrack.security.jwt-secret}") String secret,
                      @Value("${jobtrack.security.access-token-minutes}") long accessTokenMinutes) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET doit contenir au moins 32 octets");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.accessTokenSeconds = accessTokenMinutes * 60;
    }

    @Override
    public String issue(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.id().toString())
                .claim("email", user.email())
                .claim("role", user.role().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenSeconds)))
                .signWith(key)
                .compact();
    }

    @Override
    public long accessTokenSeconds() {
        return accessTokenSeconds;
    }

    public AuthenticatedUser parse(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return new AuthenticatedUser(UUID.fromString(claims.getSubject()), claims.get("role", String.class));
    }

    public record AuthenticatedUser(UUID userId, String role) {}
}
