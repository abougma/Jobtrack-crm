package com.jobtrack.infrastructure.persistence.adapter;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jobtrack.application.port.RefreshTokenRepository;
import com.jobtrack.domain.user.RefreshToken;
import com.jobtrack.infrastructure.persistence.repository.SpringRefreshTokenRepository;

@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {
    private final SpringRefreshTokenRepository repository;

    public RefreshTokenRepositoryAdapter(SpringRefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(DomainMapper::toDomain);
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        return DomainMapper.toDomain(repository.save(DomainMapper.toEntity(token)));
    }

    @Override
    public void deleteAllForUser(UUID userId) {
        repository.deleteAllByUserId(userId);
    }
}
