package com.smartqueue.inventory_service.repository;

import com.smartqueue.inventory_service.entity.LockStatus;
import com.smartqueue.inventory_service.entity.ResourceLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResourceLockRepository extends JpaRepository<ResourceLock, UUID> {

    Optional<ResourceLock> findByLockKey(String lockKey);

    List<ResourceLock> findByResourceId(UUID resourceId);

    List<ResourceLock> findByUserId(UUID userId);

    List<ResourceLock> findByStatus(LockStatus status);
}
