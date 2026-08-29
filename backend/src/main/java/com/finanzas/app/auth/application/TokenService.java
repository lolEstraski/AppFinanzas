package com.finanzas.app.auth.application;

import com.finanzas.app.auth.domain.User;

public interface TokenService {

    String generateAccessToken(User user);

    String generateOpaqueToken();
}
