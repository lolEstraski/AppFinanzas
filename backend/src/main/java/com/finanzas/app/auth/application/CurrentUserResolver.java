package com.finanzas.app.auth.application;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.finanzas.app.auth.domain.User;
import com.finanzas.app.auth.domain.UserRepository;

@Component
public class CurrentUserResolver {

    private final UserRepository userRepository;

    public CurrentUserResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User resolve(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + authentication.getName()));
    }
}
