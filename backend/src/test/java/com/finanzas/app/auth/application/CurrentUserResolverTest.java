package com.finanzas.app.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.finanzas.app.auth.domain.User;
import com.finanzas.app.auth.domain.UserRepository;

@ExtendWith(MockitoExtension.class)
class CurrentUserResolverTest {

    @Mock
    private UserRepository userRepository;

    private CurrentUserResolver currentUserResolver;

    @Test
    void resolvesUserFromAuthenticationName() {
        currentUserResolver = new CurrentUserResolver(userRepository);
        User user = new User();
        user.setEmail("jane@example.com");
        given(userRepository.findByEmail("jane@example.com")).willReturn(Optional.of(user));
        Authentication authentication = new UsernamePasswordAuthenticationToken("jane@example.com", null);

        User resolved = currentUserResolver.resolve(authentication);

        assertThat(resolved).isEqualTo(user);
    }

    @Test
    void throwsWhenAuthenticatedUserMissingFromDatabase() {
        currentUserResolver = new CurrentUserResolver(userRepository);
        given(userRepository.findByEmail("ghost@example.com")).willReturn(Optional.empty());
        Authentication authentication = new UsernamePasswordAuthenticationToken("ghost@example.com", null);

        assertThatThrownBy(() -> currentUserResolver.resolve(authentication))
                .isInstanceOf(IllegalStateException.class);
    }
}
