package com.teadelivery.ordercatalog.fsm.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * State Cache Service
 * Manages FSM state caching in Redis with support for order and delivery states
 * Key patterns: order:state:{orderId}, delivery:state:{deliveryId}
 */
@Service
@Slf4j
public class StateCacheService<TState extends Enum<TState>> {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Key prefixes as per BE-003-15
    private static final String ORDER_STATE_PREFIX = "order:state:";
    private static final String DELIVERY_STATE_PREFIX = "delivery:state:";
    private static final String STATE_KEY_PREFIX = "fsm:state:";  // Generic fallback
    
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
    
    // ========== Order-specific methods (BE-003-15) ==========
    
    /**
     * Cache order state with pattern: order:state:{orderId}
     */
    public void cacheOrderState(UUID orderId, TState state) {
        String key = ORDER_STATE_PREFIX + orderId;
        try {
            redisTemplate.opsForValue().set(key, state.name(), STATE_TTL);
            log.debug("Cached order state: orderId={}, state={}", orderId, state);
        } catch (Exception e) {
            log.error("Failed to cache order state: orderId={}", orderId, e);
        }
    }
    
    /**
     * Get order state from cache
     */
    public TState getOrderState(UUID orderId) {
        String key = ORDER_STATE_PREFIX + orderId;
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.debug("Order state found in cache: orderId={}, state={}", orderId, cached);
                @SuppressWarnings("unchecked")
                TState state = (TState) cached;
                return state;
            }
        } catch (Exception e) {
            log.error("Failed to get cached order state: orderId={}", orderId, e);
        }
        return null;
    }
    
    /**
     * Invalidate order state cache
     */
    public void invalidateOrderState(UUID orderId) {
        String key = ORDER_STATE_PREFIX + orderId;
        try {
            redisTemplate.delete(key);
            log.debug("Invalidated order state cache: orderId={}", orderId);
        } catch (Exception e) {
            log.error("Failed to invalidate order state cache: orderId={}", orderId, e);
        }
    }
    
    // ========== Delivery-specific methods (BE-003-15) ==========
    
    /**
     * Cache delivery state with pattern: delivery:state:{deliveryId}
     */
    public void cacheDeliveryState(UUID deliveryId, TState state) {
        String key = DELIVERY_STATE_PREFIX + deliveryId;
        try {
            redisTemplate.opsForValue().set(key, state.name(), STATE_TTL);
            log.debug("Cached delivery state: deliveryId={}, state={}", deliveryId, state);
        } catch (Exception e) {
            log.error("Failed to cache delivery state: deliveryId={}", deliveryId, e);
        }
    }
    
    /**
     * Get delivery state from cache
     */
    public TState getDeliveryState(UUID deliveryId) {
        String key = DELIVERY_STATE_PREFIX + deliveryId;
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.debug("Delivery state found in cache: deliveryId={}, state={}", deliveryId, cached);
                @SuppressWarnings("unchecked")
                TState state = (TState) cached;
                return state;
            }
        } catch (Exception e) {
            log.error("Failed to get cached delivery state: deliveryId={}", deliveryId, e);
        }
        return null;
    }
    
    /**
     * Invalidate delivery state cache
     */
    public void invalidateDeliveryState(UUID deliveryId) {
        String key = DELIVERY_STATE_PREFIX + deliveryId;
        try {
            redisTemplate.delete(key);
            log.debug("Invalidated delivery state cache: deliveryId={}", deliveryId);
        } catch (Exception e) {
            log.error("Failed to invalidate delivery state cache: deliveryId={}", deliveryId, e);
        }
    }
}
