package com.smartqueue.auth_service.controller;

import com.smartqueue.auth_service.dto.*;
import com.smartqueue.auth_service.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "REST APIs for user authentication, registration, token refresh, and profile access")
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account, assigns ROLE_USER, and publishes UserRegisteredEvent.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.smartqueue.auth_service.dto.ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":true,\"message\":\"Success\",\"data\":{\"id\":\"a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11\",\"username\":\"john_doe\",\"email\":\"john@example.com\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"enabled\":true,\"accountNonLocked\":true,\"roles\":[{\"id\":1,\"name\":\"ROLE_USER\",\"description\":\"Standard user role\"}]}}"))),
            @ApiResponse(responseCode = "400", description = "Validation error or username/email already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Username is already taken\",\"path\":\"/api/v1/auth/register\"}"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.smartqueue.auth_service.dto.ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("REST Request to register user with username: {}", request.getUsername());
        UserResponse response = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(com.smartqueue.auth_service.dto.ApiResponse.success(response));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticates user credentials (username or email and password) and returns JWT access and refresh tokens.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.smartqueue.auth_service.dto.ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":true,\"message\":\"Success\",\"data\":{\"accessToken\":\"eyJhbGciOiJIUzI1NiJ9...\",\"refreshToken\":\"eyJhbGciOiJIUzI1NiJ9...\",\"tokenType\":\"Bearer\",\"expiresInMs\":900000}}"))),
            @ApiResponse(responseCode = "400", description = "Missing required parameters or validation error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or locked/disabled account",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Invalid username/email or password\",\"path\":\"/api/v1/auth/login\"}")))
    })
    public ResponseEntity<com.smartqueue.auth_service.dto.ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("REST Request to authenticate user: {}", request.getUsernameOrEmail());
        AuthResponse response = authenticationService.login(request);
        return ResponseEntity.ok(com.smartqueue.auth_service.dto.ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Exchanges a valid refresh token for a new access token and rotated refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.smartqueue.auth_service.dto.ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":true,\"message\":\"Success\",\"data\":{\"accessToken\":\"eyJhbGciOiJIUzI1NiJ9...\",\"refreshToken\":\"eyJhbGciOiJIUzI1NiJ9...\",\"tokenType\":\"Bearer\",\"expiresInMs\":900000}}"))),
            @ApiResponse(responseCode = "400", description = "Missing refresh token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Refresh token is expired, revoked, or invalid\",\"path\":\"/api/v1/auth/refresh\"}")))
    })
    public ResponseEntity<com.smartqueue.auth_service.dto.ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("REST Request to refresh token");
        AuthResponse response = authenticationService.refreshToken(request);
        return ResponseEntity.ok(com.smartqueue.auth_service.dto.ApiResponse.success(response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Blacklists current JWT access token in Redis and revokes refresh token in database.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged out successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.smartqueue.auth_service.dto.ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":true,\"message\":\"Success\",\"data\":\"Successfully logged out\"}")))
    })
    public ResponseEntity<com.smartqueue.auth_service.dto.ApiResponse<String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) RefreshTokenRequest request,
            Principal principal) {

        String usernameOrEmail = principal != null ? principal.getName() : null;
        authenticationService.logout(authHeader, usernameOrEmail);
        return ResponseEntity.ok(com.smartqueue.auth_service.dto.ApiResponse.success("Successfully logged out"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Retrieves profile and assigned roles for the currently authenticated user.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.smartqueue.auth_service.dto.ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":true,\"message\":\"Success\",\"data\":{\"id\":\"a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11\",\"username\":\"john_doe\",\"email\":\"john@example.com\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"enabled\":true,\"accountNonLocked\":true,\"roles\":[{\"id\":1,\"name\":\"ROLE_USER\",\"description\":\"Standard user role\"}]}}"))),
            @ApiResponse(responseCode = "401", description = "User is unauthenticated or token is invalid/expired",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User account not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.smartqueue.auth_service.dto.ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse response = authenticationService.getCurrentUser();
        return ResponseEntity.ok(com.smartqueue.auth_service.dto.ApiResponse.success(response));
    }
}
