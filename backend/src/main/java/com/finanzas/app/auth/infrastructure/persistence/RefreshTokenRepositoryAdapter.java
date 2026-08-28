package com.finanzas.app.auth.infrastructure.persistence;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.finanzas.app.auth.domain.RefreshToken;
import com.finanzas.app.auth.domain.RefreshTokenRepository;

@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;

    public RefreshTokenRepositoryAdapter(RefreshTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return jpaRepository.save(refreshToken);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash);
    }

    @Override
    @Transactional
    public void revokeAllForUser(Long userId) {
        jpaRepository.revokeAllForUser(userId);
    }
}
