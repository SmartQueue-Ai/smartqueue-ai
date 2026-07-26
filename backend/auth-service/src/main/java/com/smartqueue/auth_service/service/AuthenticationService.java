package com.smartqueue.auth_service.service;

import com.smartqueue.auth_service.dto.AuthResponse;
import com.smartqueue.auth_service.dto.LoginRequest;
import com.smartqueue.auth_service.dto.RefreshTokenRequest;
import com.smartqueue.auth_service.dto.RegisterRequest;
import com.smartqueue.auth_service.dto.UserResponse;

public interface AuthenticationService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String token, String usernameOrEmail);

    UserResponse getCurrentUser();
}
