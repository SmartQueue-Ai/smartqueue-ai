package com.smartqueue.auth_service.constant;

public final class SecurityConstants {

    private SecurityConstants() {
        // Private constructor to prevent instantiation
    }

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_LOG_VAR = "correlationId";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String[] PUBLIC_ENDPOINTS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/**",
            "/api/v1/auth/**",
            "/error"
    };
}
