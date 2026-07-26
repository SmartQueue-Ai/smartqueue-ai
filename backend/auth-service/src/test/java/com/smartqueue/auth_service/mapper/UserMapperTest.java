package com.smartqueue.auth_service.mapper;

import com.smartqueue.auth_service.domain.Role;
import com.smartqueue.auth_service.domain.RoleName;
import com.smartqueue.auth_service.domain.User;
import com.smartqueue.auth_service.dto.RoleResponse;
import com.smartqueue.auth_service.dto.UserRequest;
import com.smartqueue.auth_service.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    @DisplayName("Should map User entity to UserResponse DTO")
    void shouldMapUserEntityToUserResponse() {
        Role role = Role.builder()
                .id(1L)
                .name(RoleName.ROLE_USER)
                .description("Standard User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("john_doe")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .enabled(true)
                .accountNonLocked(true)
                .roles(Set.of(role))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        UserResponse response = userMapper.toUserResponse(user);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(user.getId());
        assertThat(response.getUsername()).isEqualTo("john_doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getRoles()).hasSize(1);
    }

    @Test
    @DisplayName("Should map UserRequest DTO to User entity")
    void shouldMapUserRequestToUserEntity() {
        UserRequest request = UserRequest.builder()
                .username("jane_doe")
                .email("jane@example.com")
                .password("password123")
                .firstName("Jane")
                .lastName("Doe")
                .roles(Set.of(RoleName.ROLE_USER))
                .build();

        User user = userMapper.toUserEntity(request);

        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("jane_doe");
        assertThat(user.getEmail()).isEqualTo("jane@example.com");
        assertThat(user.getFirstName()).isEqualTo("Jane");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
    }

    @Test
    @DisplayName("Should handle null inputs cleanly")
    void shouldHandleNullInputs() {
        assertThat(userMapper.toUserResponse(null)).isNull();
        assertThat(userMapper.toUserEntity(null)).isNull();
        assertThat(userMapper.toRoleResponse(null)).isNull();
    }
}
