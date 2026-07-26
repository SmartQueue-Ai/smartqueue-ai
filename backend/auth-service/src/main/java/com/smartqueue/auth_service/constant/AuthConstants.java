package com.smartqueue.auth_service.constant;

public final class AuthConstants {

    private AuthConstants() {
        // Private constructor to prevent instantiation
    }

    public static final String SERVICE_NAME = "Auth Service";
    public static final String API_VERSION = "v1";
    public static final String BASE_AUTH_PATH = "/api/v1/auth";
    
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_OPERATOR = "ROLE_OPERATOR";
}
