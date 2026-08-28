package com.finanzas.app.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JwtPropertiesTest {

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    void bindsJwtPropertiesFromConfiguration() {
        assertThat(jwtProperties.secret()).isEqualTo("test-only-secret-please-change-32-bytes-minimum");
        assertThat(jwtProperties.accessTokenExpirationMs()).isEqualTo(900000L);
        assertThat(jwtProperties.refreshTokenExpirationMs()).isEqualTo(1209600000L);
    }
}
