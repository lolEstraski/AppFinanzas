package com.finanzas.app.auth.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.finanzas.app.auth.domain.Role;
import com.finanzas.app.auth.domain.User;
import com.finanzas.app.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;

class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties(
                "test-secret-key-with-at-least-32-bytes-long", 900000L, 1209600000L);
        jwtTokenService = new JwtTokenService(jwtProperties);
    }

    private User sampleUser() {
        User user = new User();
        user.setId(42L);
        user.setEmail("jane@example.com");
        user.setRole(Role.USER);
        return user;
    }

    @Test
    void generatesTokenWithExpectedClaims() {
        String token = jwtTokenService.generateAccessToken(sampleUser());

        Claims claims = jwtTokenService.parseAndValidate(token);

        assertThat(claims.getSubject()).isEqualTo("jane@example.com");
        assertThat(claims.get("uid").toString()).isEqualTo("42");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    void rejectsTamperedToken() {
        String token = jwtTokenService.generateAccessToken(sampleUser());
        // Tamper the first character of the signature segment rather than the last
        // character of the token: a base64url group at the very end can carry
        // padding bits that don't affect the decoded byte, which made this
        // assertion flaky when it corrupted the tail instead.
        int signatureStart = token.lastIndexOf('.') + 1;
        char original = token.charAt(signatureStart);
        char replacement = original == 'A' ? 'B' : 'A';
        String tampered = token.substring(0, signatureStart) + replacement + token.substring(signatureStart + 1);

        assertThatThrownBy(() -> jwtTokenService.parseAndValidate(tampered))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void rejectsExpiredToken() throws InterruptedException {
        JwtProperties shortLived = new JwtProperties(
                "test-secret-key-with-at-least-32-bytes-long", 1L, 1209600000L);
        JwtTokenService shortLivedService = new JwtTokenService(shortLived);
        String token = shortLivedService.generateAccessToken(sampleUser());
        Thread.sleep(20);

        assertThatThrownBy(() -> shortLivedService.parseAndValidate(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void generatesUniqueOpaqueTokens() {
        String first = jwtTokenService.generateOpaqueToken();
        String second = jwtTokenService.generateOpaqueToken();

        assertThat(first).isNotEqualTo(second);
        assertThat(first.length()).isGreaterThan(30);
    }
}
