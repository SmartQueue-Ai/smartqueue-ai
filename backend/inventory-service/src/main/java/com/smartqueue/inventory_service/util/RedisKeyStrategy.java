package com.smartqueue.inventory_service.util;

import java.util.UUID;

public final class RedisKeyStrategy {

    private static final String LOCK_KEY_PREFIX = "smartqueue:lock:";

    private RedisKeyStrategy() {
        // Private constructor for utility class
    }

    public static String buildResourceLockKey(UUID resourceId) {
        if (resourceId == null) {
            throw new IllegalArgumentException("Resource ID cannot be null");
        }
        return LOCK_KEY_PREFIX + resourceId;
    }

    public static String buildResourceLockKey(String resourceId) {
        if (resourceId == null || resourceId.isBlank()) {
            throw new IllegalArgumentException("Resource ID cannot be blank");
        }
        return LOCK_KEY_PREFIX + resourceId;
    }
}
