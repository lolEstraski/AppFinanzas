package com.finanzas.app.auth.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.finanzas.app.auth.domain.AuthProvider;
import com.finanzas.app.auth.domain.PasswordResetToken;
import com.finanzas.app.auth.domain.Role;
import com.finanzas.app.auth.domain.User;
import com.finanzas.app.config.JpaAuditingConfig;

import jakarta.persistence.EntityManager;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class PasswordResetTokenJpaRepositoryTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PasswordResetTokenJpaRepository passwordResetTokenJpaRepository;

    @Autowired
    private EntityManager entityManager;

    private User persistedUser() {
        User user = new User();
        user.setEmail("jane@example.com");
        user.setPasswordHash("hashed-password");
        user.setFullName("Jane Doe");
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setRole(Role.USER);
        return userJpaRepository.saveAndFlush(user);
    }

    private PasswordResetToken tokenFor(User user, String hash) {
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(hash);
        token.setExpiresAt(Instant.now().plusSeconds(1800));
        return token;
    }

    @Test
    void findsTokenByHash() {
        User user = persistedUser();
        passwordResetTokenJpaRepository.saveAndFlush(tokenFor(user, "hash-1"));

        assertThat(passwordResetTokenJpaRepository.findByTokenHash("hash-1")).isPresent();
        assertThat(passwordResetTokenJpaRepository.findByTokenHash("missing")).isEmpty();
    }

    @Test
    void loadsAssociatedUserEagerlyToAvoidLazyInitializationAfterSessionCloses() {
        User user = persistedUser();
        passwordResetTokenJpaRepository.saveAndFlush(tokenFor(user, "hash-1"));
        entityManager.clear();

        PasswordResetToken reloaded = passwordResetTokenJpaRepository.findByTokenHash("hash-1").orElseThrow();

        assertThat(Hibernate.isInitialized(reloaded.getUser())).isTrue();
        assertThat(reloaded.getUser().getEmail()).isEqualTo("jane@example.com");
    }
}
