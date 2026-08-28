package com.finanzas.app.auth.application.usecase;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.finanzas.app.auth.application.EmailSender;
import com.finanzas.app.auth.application.TokenHasher;
import com.finanzas.app.auth.application.TokenService;
import com.finanzas.app.auth.application.dto.ForgotPasswordRequest;
import com.finanzas.app.auth.domain.PasswordResetToken;
import com.finanzas.app.auth.domain.PasswordResetTokenRepository;
import com.finanzas.app.auth.domain.User;
import com.finanzas.app.auth.domain.UserRepository;

@Service
public class RequestPasswordResetUseCase {

    private static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(30);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TokenService tokenService;
    private final EmailSender emailSender;

    public RequestPasswordResetUseCase(UserRepository userRepository,
                                        PasswordResetTokenRepository passwordResetTokenRepository,
                                        TokenService tokenService,
                                        EmailSender emailSender) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.tokenService = tokenService;
        this.emailSender = emailSender;
    }

    public void requestReset(ForgotPasswordRequest request) {
        Optional<User> maybeUser = userRepository.findByEmail(request.email());
        if (maybeUser.isEmpty()) {
            return;
        }

        User user = maybeUser.get();
        String rawToken = tokenService.generateOpaqueToken();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setTokenHash(TokenHasher.sha256(rawToken));
        resetToken.setExpiresAt(Instant.now().plus(RESET_TOKEN_TTL));
        passwordResetTokenRepository.save(resetToken);

        emailSender.sendPasswordResetEmail(user.getEmail(), rawToken);
    }
}
