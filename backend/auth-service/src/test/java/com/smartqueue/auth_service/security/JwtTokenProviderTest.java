package com.smartqueue.auth_service.security;

import com.smartqueue.auth_service.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        jwtProperties.setExpirationMs(900000); // 15 mins
        jwtProperties.setRefreshExpirationMs(604800000); // 7 days

        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
    }

    @Test
    void generateAccessToken_ShouldCreateValidToken() {
        String username = "johndoe";
        UUID userId = UUID.randomUUID();

        String token = jwtTokenProvider.generateAccessToken(username, userId);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo(username);
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(userId);
    }

    @Test
    void generateRefreshToken_ShouldCreateValidRefreshToken() {
        String username = "johndoe";
        UUID userId = UUID.randomUUID();

        String refreshToken = jwtTokenProvider.generateRefreshToken(username, userId);

        assertThat(refreshToken).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(refreshToken)).isEqualTo(username);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        String invalidToken = "invalid.jwt.token";

        assertThat(jwtTokenProvider.validateToken(invalidToken)).isFalse();
    }
}
