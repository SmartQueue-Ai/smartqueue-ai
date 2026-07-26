package com.smartqueue.inventory_service.service;

import com.smartqueue.inventory_service.dto.LockResponse;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConcurrentLockSimulationTest {

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
    void concurrentLockAttempt_OnlyOneThreadSucceeds() throws InterruptedException {
        int numberOfConcurrentThreads = 20;
        String resourceId = "resource-concurrent-500";
        String lockKey = "smartqueue:lock:resource-concurrent-500";

        // Atomic lock holder simulating Redis SETNX behavior under high concurrency
        AtomicBoolean isLockedInRedis = new AtomicBoolean(false);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Mock SETNX behavior: return true for first thread, false for all subsequent threads
        when(valueOperations.setIfAbsent(eq(lockKey), anyString(), eq(300L), eq(TimeUnit.SECONDS)))
                .thenAnswer(invocation -> isLockedInRedis.compareAndSet(false, true));

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfConcurrentThreads);
        CountDownLatch readyLatch = new CountDownLatch(numberOfConcurrentThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numberOfConcurrentThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        List<LockResponse> successfulResponses = Collections.synchronizedList(new ArrayList<>());

        for (int i = 1; i <= numberOfConcurrentThreads; i++) {
            final String userId = "user-" + i;
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await(); // Wait for all threads to be ready to strike simultaneously
                    LockResponse response = resourceLockService.lockResource(resourceId, userId);
                    successCount.incrementAndGet();
                    successfulResponses.add(response);
                } catch (LockAcquisitionException ex) {
                    conflictCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown(); // Release all 20 threads simultaneously
        boolean completed = finishLatch.await(5, TimeUnit.SECONDS);

        executorService.shutdown();

        assertThat(completed).isTrue();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(numberOfConcurrentThreads - 1);
        assertThat(successfulResponses).hasSize(1);
        assertThat(successfulResponses.get(0).getLockKey()).isEqualTo(lockKey);
        assertThat(successfulResponses.get(0).isLocked()).isTrue();
    }
}
