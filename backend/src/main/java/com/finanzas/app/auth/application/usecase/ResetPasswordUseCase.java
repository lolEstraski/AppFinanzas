package com.finanzas.app.auth.application.usecase;

import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.finanzas.app.auth.application.TokenHasher;
import com.finanzas.app.auth.application.dto.ResetPasswordRequest;
import com.finanzas.app.auth.domain.PasswordResetToken;
import com.finanzas.app.auth.domain.PasswordResetTokenRepository;
import com.finanzas.app.auth.domain.User;
import com.finanzas.app.auth.domain.UserRepository;
import com.finanzas.app.auth.domain.exception.InvalidOrExpiredTokenException;

@Service
public class ResetPasswordUseCase {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ResetPasswordUseCase(PasswordResetTokenRepository passwordResetTokenRepository,
                                 UserRepository userRepository,
                                 PasswordEncoder passwordEncoder) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void resetPassword(ResetPasswordRequest request) {
        String hash = TokenHasher.sha256(request.token());
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidOrExpiredTokenException("Invalid reset token"));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidOrExpiredTokenException("Reset token expired or already used");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}
