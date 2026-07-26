package com.smartqueue.inventory_service.dto;

import com.smartqueue.inventory_service.entity.ResourceStatus;
import com.smartqueue.inventory_service.entity.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceResponse {

    private UUID id;
    private String name;
    private ResourceType type;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private ResourceStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
