package com.smartqueue.inventory_service.repository;

import com.smartqueue.inventory_service.entity.Resource;
import com.smartqueue.inventory_service.entity.ResourceStatus;
import com.smartqueue.inventory_service.entity.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, UUID> {

    Optional<Resource> findByName(String name);

    List<Resource> findByType(ResourceType type);

    List<Resource> findByStatus(ResourceStatus status);

    boolean existsByName(String name);
}
