package com.finanzas.app.auth.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.finanzas.app.auth.domain.AuthProvider;
import com.finanzas.app.auth.domain.RefreshToken;
import com.finanzas.app.auth.domain.Role;
import com.finanzas.app.auth.domain.User;
import com.finanzas.app.config.JpaAuditingConfig;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class RefreshTokenJpaRepositoryTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private RefreshTokenJpaRepository refreshTokenJpaRepository;

    private User persistedUser() {
        User user = new User();
        user.setEmail("jane@example.com");
        user.setPasswordHash("hashed-password");
        user.setFullName("Jane Doe");
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setRole(Role.USER);
        return userJpaRepository.saveAndFlush(user);
    }

    private RefreshToken tokenFor(User user, String hash) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(hash);
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        return token;
    }

    @Test
    void findsTokenByHash() {
        User user = persistedUser();
        refreshTokenJpaRepository.saveAndFlush(tokenFor(user, "hash-1"));

        assertThat(refreshTokenJpaRepository.findByTokenHash("hash-1")).isPresent();
        assertThat(refreshTokenJpaRepository.findByTokenHash("missing")).isEmpty();
    }

    @Test
    void revokesAllTokensForUser() {
        User user = persistedUser();
        RefreshToken saved = refreshTokenJpaRepository.saveAndFlush(tokenFor(user, "hash-1"));
        assertThat(saved.isRevoked()).isFalse();

        refreshTokenJpaRepository.revokeAllForUser(user.getId());
        refreshTokenJpaRepository.flush();

        RefreshToken reloaded = refreshTokenJpaRepository.findByTokenHash("hash-1").orElseThrow();
        assertThat(reloaded.isRevoked()).isTrue();
    }
}
