package com.smartqueue.auth_service.dto;

import com.smartqueue.auth_service.domain.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponse {

    private Long id;
    private RoleName name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
