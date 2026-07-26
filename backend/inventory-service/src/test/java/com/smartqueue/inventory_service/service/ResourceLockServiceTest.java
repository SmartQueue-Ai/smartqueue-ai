package com.smartqueue.inventory_service.service;

import com.smartqueue.inventory_service.dto.LockResponse;
import com.smartqueue.inventory_service.dto.LockStatusResponse;
import com.smartqueue.inventory_service.exception.LockAcquisitionException;
import com.smartqueue.inventory_service.service.impl.ResourceLockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceLockServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ResourceLockServiceImpl resourceLockService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(resourceLockService, "defaultTtlSeconds", 300L);
    }

    @Test
    void lockResource_WhenAvailable_ShouldAcquireLockSuccessfully() {
        String resourceId = "res-101";
        String userId = "user-77";
        String expectedLockKey = "smartqueue:lock:res-101";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq(expectedLockKey), eq(userId), eq(300L), eq(TimeUnit.SECONDS))).thenReturn(true);

        LockResponse response = resourceLockService.lockResource(resourceId, userId);

        assertThat(response).isNotNull();
        assertThat(response.getResourceId()).isEqualTo(resourceId);
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getLockKey()).isEqualTo(expectedLockKey);
        assertThat(response.isLocked()).isTrue();
    }

    @Test
    void lockResource_WhenAlreadyLocked_ShouldThrowLockAcquisitionException() {
        String resourceId = "res-101";
        String userId = "user-88";
        String expectedLockKey = "smartqueue:lock:res-101";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq(expectedLockKey), eq(userId), eq(300L), eq(TimeUnit.SECONDS))).thenReturn(false);
        when(valueOperations.get(expectedLockKey)).thenReturn("user-77");

        assertThatThrownBy(() -> resourceLockService.lockResource(resourceId, userId))
                .isInstanceOf(LockAcquisitionException.class)
                .hasMessageContaining("already locked");
    }

    @Test
    void unlockResource_ShouldDeleteKeyFromRedis() {
        String resourceId = "res-101";
        String expectedLockKey = "smartqueue:lock:res-101";

        when(redisTemplate.delete(expectedLockKey)).thenReturn(true);

        boolean result = resourceLockService.unlockResource(resourceId);

        assertThat(result).isTrue();
        verify(redisTemplate, times(1)).delete(expectedLockKey);
    }

    @Test
    void isLocked_WhenKeyExists_ShouldReturnTrue() {
        String resourceId = "res-101";
        String expectedLockKey = "smartqueue:lock:res-101";

        when(redisTemplate.hasKey(expectedLockKey)).thenReturn(true);

        boolean locked = resourceLockService.isLocked(resourceId);

        assertThat(locked).isTrue();
    }

    @Test
    void isLocked_WhenKeyAbsentOrExpired_ShouldReturnFalse() {
        String resourceId = "res-101";
        String expectedLockKey = "smartqueue:lock:res-101";

        when(redisTemplate.hasKey(expectedLockKey)).thenReturn(false);

        boolean locked = resourceLockService.isLocked(resourceId);

        assertThat(locked).isFalse();
    }

    @Test
    void getLockStatus_WhenActive_ShouldReturnStatus() {
        String resourceId = "res-101";
        String expectedLockKey = "smartqueue:lock:res-101";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(expectedLockKey)).thenReturn("user-77");
        when(redisTemplate.getExpire(expectedLockKey, TimeUnit.SECONDS)).thenReturn(240L);

        LockStatusResponse status = resourceLockService.getLockStatus(resourceId);

        assertThat(status.isLocked()).isTrue();
        assertThat(status.getUserId()).isEqualTo("user-77");
        assertThat(status.getTtlRemainingSeconds()).isEqualTo(240L);
    }
}
