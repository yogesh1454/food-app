package com.teadelivery.ordercatalog.fsm.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * State Cache Service
 * Manages FSM state caching in Redis
 */
@Service
@Slf4j
public class StateCacheService<TState extends Enum<TState>> {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String STATE_KEY_PREFIX = "fsm:state:";
    private static final Duration STATE_TTL = Duration.ofHours(24);
    
    public StateCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * Cache state in Redis
     */
    public void cacheState(UUID entityId, TState state) {
        String key = STATE_KEY_PREFIX + entityId;
        try {
            redisTemplate.opsForValue().set(key, state.name(), STATE_TTL);
            log.debug("Cached state: entityId={}, state={}", entityId, state);
        } catch (Exception e) {
            log.error("Failed to cache state: entityId={}", entityId, e);
            // Don't throw - caching is not critical
        }
    }
    
    /**
     * Get state from cache
     */
    @SuppressWarnings("unchecked")
    public TState getState(UUID entityId) {
        String key = STATE_KEY_PREFIX + entityId;
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.debug("State found in cache: entityId={}, state={}", entityId, cached);
                // Note: This assumes the state enum is available in the context
                // In practice, you might need to pass the enum class
                return (TState) cached;
            }
        } catch (Exception e) {
            log.error("Failed to get cached state: entityId={}", entityId, e);
        }
        return null;
    }
    
    /**
     * Invalidate cached state
     */
    public void invalidate(UUID entityId) {
        String key = STATE_KEY_PREFIX + entityId;
        try {
            redisTemplate.delete(key);
            log.debug("Invalidated cached state: entityId={}", entityId);
        } catch (Exception e) {
            log.error("Failed to invalidate cached state: entityId={}", entityId, e);
        }
    }
}
