package com.smartqueue.inventory_service.service;

import com.smartqueue.inventory_service.dto.LockResponse;
import com.smartqueue.inventory_service.dto.LockStatusResponse;

public interface ResourceLockService {

    LockResponse lockResource(String resourceId, String userId);

    LockResponse lockResource(String resourceId, String userId, Long ttlSeconds);

    boolean unlockResource(String resourceId);

    boolean isLocked(String resourceId);

    LockStatusResponse getLockStatus(String resourceId);
}
