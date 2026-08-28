package com.finanzas.app.auth.infrastructure.persistence;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.finanzas.app.auth.domain.PasswordResetToken;
import com.finanzas.app.auth.domain.PasswordResetTokenRepository;

@Component
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepository {

    private final PasswordResetTokenJpaRepository jpaRepository;

    public PasswordResetTokenRepositoryAdapter(PasswordResetTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        return jpaRepository.save(token);
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash);
    }
}
