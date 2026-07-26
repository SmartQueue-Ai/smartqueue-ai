package com.smartqueue.auth_service.dto;

import com.smartqueue.auth_service.domain.RoleName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Role response details")
public class RoleResponse {

    @Schema(description = "Role ID", example = "1")
    private Long id;

    @Schema(description = "Role name enum", example = "ROLE_USER")
    private RoleName name;

    @Schema(description = "Role description", example = "Default user role")
    private String description;

    @Schema(description = "Role creation timestamp", example = "2026-07-27T00:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Role last updated timestamp", example = "2026-07-27T00:00:00")
    private LocalDateTime updatedAt;
}
