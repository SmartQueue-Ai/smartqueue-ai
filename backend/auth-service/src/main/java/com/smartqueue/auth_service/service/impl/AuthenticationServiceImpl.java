package com.smartqueue.auth_service.service.impl;

import com.smartqueue.auth_service.config.JwtProperties;
import com.smartqueue.auth_service.domain.Role;
import com.smartqueue.auth_service.domain.RoleName;
import com.smartqueue.auth_service.domain.User;
import com.smartqueue.auth_service.dto.AuthResponse;
import com.smartqueue.auth_service.dto.LoginRequest;
import com.smartqueue.auth_service.dto.RefreshTokenRequest;
import com.smartqueue.auth_service.dto.RegisterRequest;
import com.smartqueue.auth_service.dto.UserResponse;
import com.smartqueue.auth_service.event.UserRegisteredEvent;
import com.smartqueue.auth_service.exception.AuthException;
import com.smartqueue.auth_service.exception.InvalidTokenException;
import com.smartqueue.auth_service.exception.ResourceNotFoundException;
import com.smartqueue.auth_service.exception.UnauthorizedException;
import com.smartqueue.auth_service.mapper.UserMapper;
import com.smartqueue.auth_service.publisher.EventPublisher;
import com.smartqueue.auth_service.repository.RoleRepository;
import com.smartqueue.auth_service.repository.UserRepository;
import com.smartqueue.auth_service.security.JwtTokenProvider;
import com.smartqueue.auth_service.security.UserPrincipal;
import com.smartqueue.auth_service.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final StringRedisTemplate redisTemplate;
    private final UserMapper userMapper;
    private final EventPublisher eventPublisher;

    private static final String REDIS_REFRESH_KEY_PREFIX = "smartqueue:auth:refresh:";
    private static final String REDIS_BLACKLIST_KEY_PREFIX = "smartqueue:auth:blacklist:";

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Processing user registration for username: {}, email: {}", request.getUsername(), request.getEmail());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email is already registered");
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(RoleName.ROLE_USER)
                        .description("Standard user role")
                        .build()));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true)
                .accountNonLocked(true)
                .build();

        user.addRole(userRole);
        User savedUser = userRepository.save(user);

        log.info("User registered successfully with ID: {}", savedUser.getId());

        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .build();

        eventPublisher.publishUserRegisteredEvent(event);

        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login attempt for user/email: {}", request.getUsernameOrEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByEmailWithRoles(userPrincipal.getEmail())
                .orElseGet(() -> userRepository.findByUsernameWithRoles(userPrincipal.getUsername())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found")));

        String accessToken = jwtTokenProvider.generateAccessToken(authentication, user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername(), user.getId());

        // Store refresh token in Redis
        String redisKey = REDIS_REFRESH_KEY_PREFIX + user.getUsername();
        redisTemplate.opsForValue().set(redisKey, refreshToken, jwtProperties.getRefreshExpirationMs(), TimeUnit.MILLISECONDS);

        log.info("Login successful for user: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInMs(jwtProperties.getExpirationMs())
                .user(userMapper.toUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(requestRefreshToken)) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        String username = jwtTokenProvider.getUsernameFromToken(requestRefreshToken);
        String redisKey = REDIS_REFRESH_KEY_PREFIX + username;
        String storedRefreshToken = redisTemplate.opsForValue().get(redisKey);

        if (!StringUtils.hasText(storedRefreshToken) || !storedRefreshToken.equals(requestRefreshToken)) {
            throw new InvalidTokenException("Refresh token is expired, revoked, or invalid");
        }

        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for refresh token"));

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getId());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername(), user.getId());

        // Update refresh token in Redis
        redisTemplate.opsForValue().set(redisKey, newRefreshToken, jwtProperties.getRefreshExpirationMs(), TimeUnit.MILLISECONDS);

        log.info("Refresh token successfully rotated for user: {}", username);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresInMs(jwtProperties.getExpirationMs())
                .user(userMapper.toUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public void logout(String token, String usernameOrEmail) {
        log.info("Processing logout for user: {}", usernameOrEmail);

        if (StringUtils.hasText(usernameOrEmail)) {
            String redisKey = REDIS_REFRESH_KEY_PREFIX + usernameOrEmail;
            redisTemplate.delete(redisKey);
        }

        if (StringUtils.hasText(token)) {
            String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            String blacklistKey = REDIS_BLACKLIST_KEY_PREFIX + rawToken;
            redisTemplate.opsForValue().set(blacklistKey, "revoked", jwtProperties.getExpirationMs(), TimeUnit.MILLISECONDS);
        }

        SecurityContextHolder.clearContext();
        log.info("Logout completed successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("User is not authenticated");
        }

        String username;
        if (authentication.getPrincipal() instanceof UserPrincipal principal) {
            username = principal.getUsername();
        } else if (authentication.getPrincipal() instanceof String principalStr) {
            username = principalStr;
        } else {
            throw new UnauthorizedException("Invalid authentication principal");
        }

        User user = userRepository.findByUsernameWithRoles(username)
                .orElseGet(() -> userRepository.findByEmailWithRoles(username)
                        .orElseThrow(() -> new ResourceNotFoundException("Current authenticated user not found")));

        return userMapper.toUserResponse(user);
    }
}
