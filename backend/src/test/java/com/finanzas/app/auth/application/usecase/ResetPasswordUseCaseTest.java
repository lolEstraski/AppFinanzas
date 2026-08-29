package com.finanzas.app.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.finanzas.app.auth.application.TokenHasher;
import com.finanzas.app.auth.application.dto.ResetPasswordRequest;
import com.finanzas.app.auth.domain.PasswordResetToken;
import com.finanzas.app.auth.domain.PasswordResetTokenRepository;
import com.finanzas.app.auth.domain.User;
import com.finanzas.app.auth.domain.UserRepository;
import com.finanzas.app.auth.domain.exception.InvalidOrExpiredTokenException;

@ExtendWith(MockitoExtension.class)
class ResetPasswordUseCaseTest {

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private ResetPasswordUseCase resetPasswordUseCase;

    private PasswordResetToken tokenFor(User user) {
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(TokenHasher.sha256("raw-reset-token"));
        token.setExpiresAt(Instant.now().plusSeconds(1800));
        token.setUsed(false);
        return token;
    }

    @Test
    void resetsPasswordWithValidToken() {
        resetPasswordUseCase = new ResetPasswordUseCase(passwordResetTokenRepository, userRepository, passwordEncoder);
        User user = new User();
        user.setId(1L);
        PasswordResetToken token = tokenFor(user);
        given(passwordResetTokenRepository.findByTokenHash(TokenHasher.sha256("raw-reset-token")))
                .willReturn(Optional.of(token));
        given(passwordEncoder.encode("NewSecret123!")).willReturn("new-hashed-password");

        resetPasswordUseCase.resetPassword(new ResetPasswordRequest("raw-reset-token", "NewSecret123!"));

        assertThat(user.getPasswordHash()).isEqualTo("new-hashed-password");
        assertThat(token.isUsed()).isTrue();
        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).save(token);
    }

    @Test
    void throwsWhenTokenNotFound() {
        resetPasswordUseCase = new ResetPasswordUseCase(passwordResetTokenRepository, userRepository, passwordEncoder);
        given(passwordResetTokenRepository.findByTokenHash(any())).willReturn(Optional.empty());

        assertThatThrownBy(() -> resetPasswordUseCase.resetPassword(new ResetPasswordRequest("unknown", "NewSecret123!")))
                .isInstanceOf(InvalidOrExpiredTokenException.class);
    }

    @Test
    void throwsWhenTokenAlreadyUsed() {
        resetPasswordUseCase = new ResetPasswordUseCase(passwordResetTokenRepository, userRepository, passwordEncoder);
        User user = new User();
        PasswordResetToken token = tokenFor(user);
        token.setUsed(true);
        given(passwordResetTokenRepository.findByTokenHash(any())).willReturn(Optional.of(token));

        assertThatThrownBy(() -> resetPasswordUseCase.resetPassword(new ResetPasswordRequest("raw-reset-token", "NewSecret123!")))
                .isInstanceOf(InvalidOrExpiredTokenException.class);
    }

    @Test
    void throwsWhenTokenExpired() {
        resetPasswordUseCase = new ResetPasswordUseCase(passwordResetTokenRepository, userRepository, passwordEncoder);
        User user = new User();
        PasswordResetToken token = tokenFor(user);
        token.setExpiresAt(Instant.now().minusSeconds(10));
        given(passwordResetTokenRepository.findByTokenHash(any())).willReturn(Optional.of(token));

        assertThatThrownBy(() -> resetPasswordUseCase.resetPassword(new ResetPasswordRequest("raw-reset-token", "NewSecret123!")))
                .isInstanceOf(InvalidOrExpiredTokenException.class);
    }
}
