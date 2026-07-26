package com.smartqueue.auth_service.service;

import com.smartqueue.auth_service.config.JwtProperties;
import com.smartqueue.auth_service.domain.Role;
import com.smartqueue.auth_service.domain.RoleName;
import com.smartqueue.auth_service.domain.User;
import com.smartqueue.auth_service.dto.*;
import com.smartqueue.auth_service.event.UserRegisteredEvent;
import com.smartqueue.auth_service.exception.AuthException;
import com.smartqueue.auth_service.exception.InvalidTokenException;
import com.smartqueue.auth_service.mapper.UserMapper;
import com.smartqueue.auth_service.publisher.EventPublisher;
import com.smartqueue.auth_service.repository.RoleRepository;
import com.smartqueue.auth_service.repository.UserRepository;
import com.smartqueue.auth_service.security.JwtTokenProvider;
import com.smartqueue.auth_service.security.UserPrincipal;
import com.smartqueue.auth_service.service.impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private UserMapper userMapper;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private User sampleUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().id(1L).name(RoleName.ROLE_USER).description("User role").build();
        sampleUser = User.builder()
                .id(UUID.randomUUID())
                .username("johndoe")
                .email("john@example.com")
                .passwordHash("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .build();
        sampleUser.addRole(userRole);
    }

    @Test
    void register_WhenValidRequest_ShouldSaveUserAndPublishEvent() {
        RegisterRequest request = RegisterRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();

        UserResponse expectedResponse = UserResponse.builder()
                .id(sampleUser.getId())
                .username("johndoe")
                .email("john@example.com")
                .build();

        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(userMapper.toUserResponse(sampleUser)).thenReturn(expectedResponse);

        UserResponse result = authenticationService.register(request);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("johndoe");
        verify(eventPublisher, times(1)).publishUserRegisteredEvent(any(UserRegisteredEvent.class));
    }

    @Test
    void register_WhenUsernameExists_ShouldThrowException() {
        RegisterRequest request = RegisterRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("password123")
                .build();

        when(userRepository.existsByUsername("johndoe")).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Username is already taken");
    }

    @Test
    void login_WhenValidCredentials_ShouldReturnTokensAndStoreInRedis() {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("john@example.com")
                .password("password123")
                .build();

        UserPrincipal principal = UserPrincipal.create(sampleUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByEmailWithRoles("john@example.com")).thenReturn(Optional.of(sampleUser));
        when(jwtTokenProvider.generateAccessToken(any(Authentication.class), any(UUID.class))).thenReturn("access.token");
        when(jwtTokenProvider.generateRefreshToken(anyString(), any(UUID.class))).thenReturn("refresh.token");
        when(jwtProperties.getExpirationMs()).thenReturn(900000L);
        when(jwtProperties.getRefreshExpirationMs()).thenReturn(604800000L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        AuthResponse response = authenticationService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access.token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh.token");
        verify(valueOperations, times(1)).set(eq("smartqueue:auth:refresh:johndoe"), eq("refresh.token"), eq(604800000L), eq(java.util.concurrent.TimeUnit.MILLISECONDS));
    }

    @Test
    void refreshToken_WhenInvalidToken_ShouldThrowException() {
        RefreshTokenRequest request = RefreshTokenRequest.builder().refreshToken("invalid.refresh.token").build();

        when(jwtTokenProvider.validateToken("invalid.refresh.token")).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.refreshToken(request))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid or expired refresh token");
    }
}
