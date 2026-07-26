package com.smartqueue.auth_service.controller;

import com.smartqueue.auth_service.dto.ApiResponse;
import com.smartqueue.auth_service.dto.AuthResponse;
import com.smartqueue.auth_service.dto.LoginRequest;
import com.smartqueue.auth_service.dto.RefreshTokenRequest;
import com.smartqueue.auth_service.dto.RegisterRequest;
import com.smartqueue.auth_service.dto.UserResponse;
import com.smartqueue.auth_service.service.AuthenticationService;
import com.smartqueue.auth_service.util.CorrelationIdUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and Identity Management APIs")
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user account and publishes UserRegisteredEvent")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        String correlationId = CorrelationIdUtil.getCurrentCorrelationId();
        UserResponse response = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User registered successfully", correlationId));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticates user credentials and returns JWT Access & Refresh Tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        String correlationId = CorrelationIdUtil.getCurrentCorrelationId();
        AuthResponse response = authenticationService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful", correlationId));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Rotates access and refresh tokens using a valid refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        String correlationId = CorrelationIdUtil.getCurrentCorrelationId();
        AuthResponse response = authenticationService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully", correlationId));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Logout user", description = "Revokes refresh token in Redis and blacklists active access token")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        String correlationId = CorrelationIdUtil.getCurrentCorrelationId();
        String authHeader = request.getHeader("Authorization");
        String username = SecurityContextHolder.getContext().getAuthentication() != null ?
                SecurityContextHolder.getContext().getAuthentication().getName() : null;

        authenticationService.logout(authHeader, username);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", "Logout completed", correlationId));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get current user profile", description = "Retrieves the authenticated user profile details")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        String correlationId = CorrelationIdUtil.getCurrentCorrelationId();
        UserResponse response = authenticationService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(response, "User profile retrieved successfully", correlationId));
    }
}
