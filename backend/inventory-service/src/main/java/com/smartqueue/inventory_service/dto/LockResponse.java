package com.smartqueue.inventory_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LockResponse {

    private String resourceId;
    private String userId;
    private String lockKey;
    @JsonProperty("isLocked")
    private boolean isLocked;
    private Instant expiresAt;
    private Long ttlRemainingSeconds;
}
