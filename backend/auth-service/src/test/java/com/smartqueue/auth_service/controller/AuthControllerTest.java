package com.smartqueue.auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartqueue.auth_service.config.SecurityConfig;
import com.smartqueue.auth_service.dto.*;
import com.smartqueue.auth_service.filter.CorrelationIdFilter;
import com.smartqueue.auth_service.filter.JwtAuthenticationFilter;
import com.smartqueue.auth_service.filter.RequestLoggingFilter;
import com.smartqueue.auth_service.security.CustomUserDetailsService;
import com.smartqueue.auth_service.security.JwtTokenProvider;
import com.smartqueue.auth_service.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, CorrelationIdFilter.class, RequestLoggingFilter.class, JwtAuthenticationFilter.class})
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
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @Test
    void register_WhenValidRequest_ShouldReturn201Created() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();

        UserResponse userResponse = UserResponse.builder()
                .id(UUID.randomUUID())
                .username("johndoe")
                .email("john@example.com")
                .build();

        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("johndoe"));
    }

    @Test
    void register_WhenInvalidRequest_ShouldReturn400BadRequest() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .username("")
                .email("invalid-email")
                .password("123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void login_WhenValidRequest_ShouldReturn200OK() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("johndoe")
                .password("password123")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("access.token.jwt")
                .refreshToken("refresh.token.jwt")
                .tokenType("Bearer")
                .expiresInMs(900000)
                .build();

        when(authenticationService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access.token.jwt"));
    }

    @Test
    @WithMockUser(username = "johndoe")
    void getCurrentUser_WhenAuthenticated_ShouldReturn200OK() throws Exception {
        UserResponse userResponse = UserResponse.builder()
                .id(UUID.randomUUID())
                .username("johndoe")
                .email("john@example.com")
                .build();

        when(authenticationService.getCurrentUser()).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("johndoe"));
    }
}
