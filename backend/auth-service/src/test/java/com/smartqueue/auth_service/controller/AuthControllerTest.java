package com.smartqueue.auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartqueue.auth_service.config.SecurityConfig;
import com.smartqueue.auth_service.dto.*;
import com.smartqueue.auth_service.exception.AuthException;
import com.smartqueue.auth_service.exception.GlobalExceptionHandler;
import com.smartqueue.auth_service.exception.InvalidTokenException;
import com.smartqueue.auth_service.exception.UnauthorizedException;
import com.smartqueue.auth_service.filter.JwtAuthenticationFilter;
import com.smartqueue.auth_service.security.JwtTokenProvider;
import com.smartqueue.auth_service.service.AuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain filterChain = invocation.getArgument(2);
            filterChain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Should return 201 Created on valid registration")
    void register_ShouldReturn201Created() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("Password123")
                .firstName("John")
                .lastName("Doe")
                .build();

        UserResponse userResponse = UserResponse.builder()
                .id(UUID.randomUUID())
                .username("john_doe")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .enabled(true)
                .accountNonLocked(true)
                .build();

        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("john_doe"))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Should return 400 Bad Request when validation fails")
    void register_ShouldReturn400BadRequestOnValidationError() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .username("ab") // too short
                .email("invalid-email") // invalid email pattern
                .password("") // empty password
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.validationErrors.username").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Should return 400 Bad Request when username already taken")
    void register_ShouldReturn400BadRequestWhenUsernameTaken() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("existing_user")
                .email("test@example.com")
                .password("Password123")
                .build();

        when(authenticationService.register(any(RegisterRequest.class)))
                .thenThrow(new AuthException("Username is already taken"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Username is already taken"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Should return 200 OK on valid credentials")
    void login_ShouldReturn200OK() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("john_doe")
                .password("Password123")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("mock_access_token")
                .refreshToken("mock_refresh_token")
                .tokenType("Bearer")
                .expiresInMs(900000)
                .build();

        when(authenticationService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock_access_token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Should return 401 Unauthorized on invalid credentials")
    void login_ShouldReturn401UnauthorizedOnInvalidCredentials() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("john_doe")
                .password("WrongPassword")
                .build();

        when(authenticationService.login(any(LoginRequest.class)))
                .thenThrow(new UnauthorizedException("Invalid username/email or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid username/email or password"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Should return 200 OK and new tokens")
    void refresh_ShouldReturn200OK() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("mock_refresh_token");

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("new_access_token")
                .refreshToken("new_refresh_token")
                .build();

        when(authenticationService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new_access_token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Should return 401 Unauthorized when refresh token expired")
    void refresh_ShouldReturn401UnauthorizedWhenTokenExpired() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("expired_refresh_token");

        when(authenticationService.refreshToken(any(RefreshTokenRequest.class)))
                .thenThrow(new InvalidTokenException("Refresh token is expired, revoked, or invalid"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Refresh token is expired, revoked, or invalid"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - Should return 200 OK")
    void logout_ShouldReturn200OK() throws Exception {
        doNothing().when(authenticationService).logout(any(), any());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer mock_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Successfully logged out"));
    }

    @Test
    @WithMockUser(username = "john_doe")
    @DisplayName("GET /api/v1/auth/me - Should return 200 OK when authenticated")
    void getCurrentUser_ShouldReturn200OKWhenAuthenticated() throws Exception {
        UserResponse userResponse = UserResponse.builder()
                .id(UUID.randomUUID())
                .username("john_doe")
                .email("john@example.com")
                .enabled(true)
                .build();

        when(authenticationService.getCurrentUser()).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("john_doe"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me - Should return 401 Unauthorized when unauthenticated")
    void getCurrentUser_ShouldReturn401UnauthorizedWhenUnauthenticated() throws Exception {
        when(authenticationService.getCurrentUser())
                .thenThrow(new UnauthorizedException("User is not authenticated"));

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
