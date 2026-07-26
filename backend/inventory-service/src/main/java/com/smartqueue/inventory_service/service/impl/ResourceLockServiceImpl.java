package com.smartqueue.inventory_service.service.impl;

import com.smartqueue.inventory_service.dto.LockResponse;
import com.smartqueue.inventory_service.dto.LockStatusResponse;
import com.smartqueue.inventory_service.exception.LockAcquisitionException;
import com.smartqueue.inventory_service.service.ResourceLockService;
import com.smartqueue.inventory_service.util.RedisKeyStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceLockServiceImpl implements ResourceLockService {

    private final StringRedisTemplate redisTemplate;

    @Value("${inventory.lock.default-ttl-seconds:300}")
    private long defaultTtlSeconds;

    @Override
    public LockResponse lockResource(String resourceId, String userId) {
        return lockResource(resourceId, userId, defaultTtlSeconds);
    }

    @Override
    public LockResponse lockResource(String resourceId, String userId, Long ttlSeconds) {
        long effectiveTtl = (ttlSeconds != null && ttlSeconds > 0) ? ttlSeconds : defaultTtlSeconds;
        String lockKey = RedisKeyStrategy.buildResourceLockKey(resourceId);

        log.info("Acquire Lock Attempt: Requesting lock on resourceId [{}] for userId [{}] with key [{}] and TTL [{}s]",
                resourceId, userId, lockKey, effectiveTtl);

        // SETNX semantics with expiration
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, userId, effectiveTtl, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(acquired)) {
            Instant expiresAt = Instant.now().plusSeconds(effectiveTtl);
            log.info("Acquire Lock Success: Lock acquired on resourceId [{}] for userId [{}] with lockKey [{}], expires at [{}]",
                    resourceId, userId, lockKey, expiresAt);

            return LockResponse.builder()
                    .resourceId(resourceId)
                    .userId(userId)
                    .lockKey(lockKey)
                    .isLocked(true)
                    .expiresAt(expiresAt)
                    .ttlRemainingSeconds(effectiveTtl)
                    .build();
        } else {
            String currentHolder = redisTemplate.opsForValue().get(lockKey);
            log.warn("Acquire Lock Failure / Conflict: ResourceId [{}] is already locked in Redis by userId [{}] with key [{}]",
                    resourceId, currentHolder, lockKey);

            throw new LockAcquisitionException("Resource [" + resourceId + "] is already locked by another user");
        }
    }

    @Override
    public boolean unlockResource(String resourceId) {
        String lockKey = RedisKeyStrategy.buildResourceLockKey(resourceId);
        log.info("Release Lock Attempt: Unlocking resourceId [{}] with key [{}]", resourceId, lockKey);

        Boolean deleted = redisTemplate.delete(lockKey);
        boolean wasReleased = Boolean.TRUE.equals(deleted);

        if (wasReleased) {
            log.info("Release Lock Success: Successfully released lock on resourceId [{}] with key [{}]", resourceId, lockKey);
        } else {
            log.info("Release Lock Note: Lock key [{}] was not present in Redis (already expired or released)", lockKey);
        }

        return true;
    }

    @Override
    public boolean isLocked(String resourceId) {
        String lockKey = RedisKeyStrategy.buildResourceLockKey(resourceId);
        Boolean hasKey = redisTemplate.hasKey(lockKey);

        boolean locked = Boolean.TRUE.equals(hasKey);
        if (!locked) {
            log.info("Expire Lock / Automatically Available: Lock for resourceId [{}] is absent or expired in Redis", resourceId);
        }

        return locked;
    }

    @Override
    public LockStatusResponse getLockStatus(String resourceId) {
        String lockKey = RedisKeyStrategy.buildResourceLockKey(resourceId);
        String lockHolder = redisTemplate.opsForValue().get(lockKey);
        Long expireSeconds = redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);

        if (lockHolder != null && expireSeconds != null && expireSeconds > 0) {
            return LockStatusResponse.builder()
                    .resourceId(resourceId)
                    .isLocked(true)
                    .userId(lockHolder)
                    .ttlRemainingSeconds(expireSeconds)
                    .build();
        } else {
            log.info("Expire Lock / Automatically Available: Lock for resourceId [{}] is expired or available", resourceId);
            return LockStatusResponse.builder()
                    .resourceId(resourceId)
                    .isLocked(false)
                    .userId(null)
                    .ttlRemainingSeconds(0L)
                    .build();
        }
    }
}
