package com.finanzas.app.auth.application.usecase;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.finanzas.app.auth.application.TokenHasher;
import com.finanzas.app.auth.application.TokenService;
import com.finanzas.app.auth.application.dto.AuthResponse;
import com.finanzas.app.auth.application.dto.RefreshRequest;
import com.finanzas.app.auth.domain.RefreshToken;
import com.finanzas.app.auth.domain.RefreshTokenRepository;
import com.finanzas.app.auth.domain.User;
import com.finanzas.app.auth.domain.exception.InvalidOrExpiredTokenException;
import com.finanzas.app.config.JwtProperties;

@Service
public class RefreshAccessTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenService tokenService;
    private final JwtProperties jwtProperties;

    public RefreshAccessTokenUseCase(RefreshTokenRepository refreshTokenRepository,
                                      TokenService tokenService,
                                      JwtProperties jwtProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenService = tokenService;
        this.jwtProperties = jwtProperties;
    }

    public AuthResponse refresh(RefreshRequest request) {
        String hash = TokenHasher.sha256(request.refreshToken());
        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidOrExpiredTokenException("Invalid refresh token"));

        if (existing.isRevoked() || existing.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidOrExpiredTokenException("Refresh token expired or revoked");
        }

        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        User user = existing.getUser();
        String newAccessToken = tokenService.generateAccessToken(user);
        String newRawRefreshToken = tokenService.generateOpaqueToken();

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setUser(user);
        newRefreshToken.setTokenHash(TokenHasher.sha256(newRawRefreshToken));
        newRefreshToken.setExpiresAt(Instant.now().plusMillis(jwtProperties.refreshTokenExpirationMs()));
        refreshTokenRepository.save(newRefreshToken);

        return new AuthResponse(newAccessToken, newRawRefreshToken, jwtProperties.accessTokenExpirationMs() / 1000);
    }
}
