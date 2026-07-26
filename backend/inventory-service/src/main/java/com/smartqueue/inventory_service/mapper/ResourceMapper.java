package com.smartqueue.inventory_service.mapper;

import com.smartqueue.inventory_service.entity.Resource;
import com.smartqueue.inventory_service.dto.ResourceRequest;
import com.smartqueue.inventory_service.dto.ResourceResponse;
import org.springframework.stereotype.Component;

@Component
public class ResourceMapper {

    public ResourceResponse toResourceResponse(Resource resource) {
        if (resource == null) {
            return null;
        }

        return ResourceResponse.builder()
                .id(resource.getId())
                .name(resource.getName())
                .type(resource.getType())
                .totalQuantity(resource.getTotalQuantity())
                .availableQuantity(resource.getAvailableQuantity())
                .status(resource.getStatus())
                .createdAt(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt())
                .build();
    }

    public Resource toResourceEntity(ResourceRequest request) {
        if (request == null) {
            return null;
        }

        return Resource.builder()
                .name(request.getName())
                .type(request.getType())
                .totalQuantity(request.getTotalQuantity())
                .availableQuantity(request.getTotalQuantity())
                .status(request.getStatus())
                .build();
    }
}
