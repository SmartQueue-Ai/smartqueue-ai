package com.smartqueue.inventory_service.util;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RedisKeyStrategyTest {

    @Test
    void buildResourceLockKey_WithUUID_ShouldFormatCorrectly() {
        UUID resourceId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String lockKey = RedisKeyStrategy.buildResourceLockKey(resourceId);

        assertThat(lockKey).isEqualTo("smartqueue:lock:123e4567-e89b-12d3-a456-426614174000");
    }

    @Test
    void buildResourceLockKey_WithString_ShouldFormatCorrectly() {
        String resourceId = "res-9901";
        String lockKey = RedisKeyStrategy.buildResourceLockKey(resourceId);

        assertThat(lockKey).isEqualTo("smartqueue:lock:res-9901");
    }

    @Test
    void buildResourceLockKey_WithNull_ShouldThrowException() {
        UUID nullId = null;
        assertThatThrownBy(() -> RedisKeyStrategy.buildResourceLockKey(nullId))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
