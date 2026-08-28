package com.finanzas.app.auth.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import com.finanzas.app.auth.domain.AuthProvider;
import com.finanzas.app.auth.domain.Role;
import com.finanzas.app.auth.domain.User;
import com.finanzas.app.config.JpaAuditingConfig;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class UserJpaRepositoryTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    private User newUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hashed-password");
        user.setFullName("Jane Doe");
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setRole(Role.USER);
        return user;
    }

    @Test
    void findsUserByEmail() {
        userJpaRepository.saveAndFlush(newUser("jane@example.com"));

        assertThat(userJpaRepository.findByEmail("jane@example.com")).isPresent();
        assertThat(userJpaRepository.findByEmail("missing@example.com")).isEmpty();
    }

    @Test
    void reportsExistsByEmail() {
        userJpaRepository.saveAndFlush(newUser("jane@example.com"));

        assertThat(userJpaRepository.existsByEmail("jane@example.com")).isTrue();
        assertThat(userJpaRepository.existsByEmail("missing@example.com")).isFalse();
    }

    @Test
    void enforcesUniqueEmail() {
        userJpaRepository.saveAndFlush(newUser("jane@example.com"));

        assertThatThrownBy(() -> userJpaRepository.saveAndFlush(newUser("jane@example.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
