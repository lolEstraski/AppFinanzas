package com.finanzas.app.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.finanzas.app.auth.application.TokenHasher;
import com.finanzas.app.auth.application.TokenService;
import com.finanzas.app.auth.application.dto.AuthResponse;
import com.finanzas.app.auth.application.dto.RefreshRequest;
import com.finanzas.app.auth.domain.RefreshToken;
import com.finanzas.app.auth.domain.RefreshTokenRepository;
import com.finanzas.app.auth.domain.User;
import com.finanzas.app.auth.domain.exception.InvalidOrExpiredTokenException;
import com.finanzas.app.config.JwtProperties;

@ExtendWith(MockitoExtension.class)
class RefreshAccessTokenUseCaseTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private TokenService tokenService;

    private RefreshAccessTokenUseCase refreshAccessTokenUseCase;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties("test-secret", 900000L, 1209600000L);
        refreshAccessTokenUseCase = new RefreshAccessTokenUseCase(refreshTokenRepository, tokenService, jwtProperties);
    }

    private RefreshToken validToken(User user) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(TokenHasher.sha256("raw-refresh-token"));
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        token.setRevoked(false);
        return token;
    }

    @Test
    void rotatesValidRefreshToken() {
        User user = new User();
        user.setId(1L);
        RefreshToken existing = validToken(user);
        given(refreshTokenRepository.findByTokenHash(TokenHasher.sha256("raw-refresh-token")))
                .willReturn(Optional.of(existing));
        given(tokenService.generateAccessToken(user)).willReturn("new-access-token");
        given(tokenService.generateOpaqueToken()).willReturn("new-raw-refresh-token");

        AuthResponse response = refreshAccessTokenUseCase.refresh(new RefreshRequest("raw-refresh-token"));

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-raw-refresh-token");
        assertThat(existing.isRevoked()).isTrue();
        verify(refreshTokenRepository, times(2)).save(any());
    }

    @Test
    void throwsWhenTokenNotFound() {
        given(refreshTokenRepository.findByTokenHash(any())).willReturn(Optional.empty());

        assertThatThrownBy(() -> refreshAccessTokenUseCase.refresh(new RefreshRequest("unknown-token")))
                .isInstanceOf(InvalidOrExpiredTokenException.class);
    }

    @Test
    void throwsWhenTokenIsRevoked() {
        User user = new User();
        RefreshToken revoked = validToken(user);
        revoked.setRevoked(true);
        given(refreshTokenRepository.findByTokenHash(any())).willReturn(Optional.of(revoked));

        assertThatThrownBy(() -> refreshAccessTokenUseCase.refresh(new RefreshRequest("raw-refresh-token")))
                .isInstanceOf(InvalidOrExpiredTokenException.class);
    }

    @Test
    void throwsWhenTokenIsExpired() {
        User user = new User();
        RefreshToken expired = validToken(user);
        expired.setExpiresAt(Instant.now().minusSeconds(10));
        given(refreshTokenRepository.findByTokenHash(any())).willReturn(Optional.of(expired));

        assertThatThrownBy(() -> refreshAccessTokenUseCase.refresh(new RefreshRequest("raw-refresh-token")))
                .isInstanceOf(InvalidOrExpiredTokenException.class);
    }
}
