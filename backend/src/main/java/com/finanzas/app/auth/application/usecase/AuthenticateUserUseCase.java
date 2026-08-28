package com.finanzas.app.auth.application.usecase;

import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.finanzas.app.auth.application.TokenHasher;
import com.finanzas.app.auth.application.TokenService;
import com.finanzas.app.auth.application.dto.AuthResponse;
import com.finanzas.app.auth.application.dto.LoginRequest;
import com.finanzas.app.auth.domain.RefreshToken;
import com.finanzas.app.auth.domain.RefreshTokenRepository;
import com.finanzas.app.auth.domain.User;
import com.finanzas.app.auth.domain.UserRepository;
import com.finanzas.app.auth.domain.exception.InvalidCredentialsException;
import com.finanzas.app.config.JwtProperties;

@Service
public class AuthenticateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    public AuthenticateUserUseCase(UserRepository userRepository,
                                    PasswordEncoder passwordEncoder,
                                    TokenService tokenService,
                                    RefreshTokenRepository refreshTokenRepository,
                                    JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProperties = jwtProperties;
    }

    public AuthResponse authenticate(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = tokenService.generateAccessToken(user);
        String rawRefreshToken = tokenService.generateOpaqueToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(TokenHasher.sha256(rawRefreshToken));
        refreshToken.setExpiresAt(Instant.now().plusMillis(jwtProperties.refreshTokenExpirationMs()));
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(accessToken, rawRefreshToken, jwtProperties.accessTokenExpirationMs() / 1000);
    }
}
