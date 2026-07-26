package com.smartqueue.inventory_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    private Long ttlSeconds;
}
