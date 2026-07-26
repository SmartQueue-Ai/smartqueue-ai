package com.smartqueue.auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User profile response details")
public class UserResponse {

    @Schema(description = "Unique user ID (UUID)", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    private UUID id;

    @Schema(description = "Username", example = "john_doe")
    private String username;

    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "First name", example = "John")
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Schema(description = "User account enabled status", example = "true")
    private boolean enabled;

    @Schema(description = "User account non-locked status", example = "true")
    private boolean accountNonLocked;

    @Schema(description = "Assigned user roles")
    private Set<RoleResponse> roles;

    @Schema(description = "Account creation timestamp", example = "2026-07-27T00:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Account last updated timestamp", example = "2026-07-27T00:00:00")
    private LocalDateTime updatedAt;
}
