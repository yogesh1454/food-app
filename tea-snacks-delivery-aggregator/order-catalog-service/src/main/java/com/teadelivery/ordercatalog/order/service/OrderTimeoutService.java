package com.teadelivery.ordercatalog.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Order Timeout Service
 * Manages timeout scheduling for order state transitions using Redis TTL
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderTimeoutService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Value("${order.timeout.restaurant-acceptance:2m}")
    private Duration restaurantAcceptanceTimeout;
    
    @Value("${order.timeout.payment-processing:5m}")
    private Duration paymentProcessingTimeout;
    
    @Value("${order.timeout.rider-assignment:5m}")
    private Duration riderAssignmentTimeout;
    
    private static final String TIMEOUT_KEY_PREFIX = "timeout:";
    private static final String RESTAURANT_ACCEPTANCE_TYPE = "restaurant_acceptance";
    private static final String PAYMENT_PROCESSING_TYPE = "payment_processing";
    private static final String RIDER_ASSIGNMENT_TYPE = "rider_assignment";
    
    /**
     * Schedule restaurant acceptance timeout
     * Key expires after 2 minutes, triggering auto-rejection
     */
    public void scheduleRestaurantAcceptanceTimeout(UUID orderId) {
        String key = buildTimeoutKey(RESTAURANT_ACCEPTANCE_TYPE, orderId);
        
        try {
            // Store orderId as value with TTL
            redisTemplate.opsForValue().set(
                key,
                orderId.toString(),
                restaurantAcceptanceTimeout.toMillis(),
                TimeUnit.MILLISECONDS
            );
            
            log.info("Scheduled restaurant acceptance timeout for order: {} ({}ms)", 
                orderId, restaurantAcceptanceTimeout.toMillis());
                
        } catch (Exception e) {
            log.error("Failed to schedule restaurant acceptance timeout for order: {}", orderId, e);
            // Don't throw - timeout scheduling is not critical for order creation
        }
    }
    
    /**
     * Cancel restaurant acceptance timeout
     * Called when restaurant accepts or rejects the order
     */
    public void cancelRestaurantAcceptanceTimeout(UUID orderId) {
        String key = buildTimeoutKey(RESTAURANT_ACCEPTANCE_TYPE, orderId);
        
        try {
            Boolean deleted = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                log.info("Cancelled restaurant acceptance timeout for order: {}", orderId);
            }
        } catch (Exception e) {
            log.error("Failed to cancel restaurant acceptance timeout for order: {}", orderId, e);
        }
    }
    
    /**
     * Schedule payment processing timeout
     */
    public void schedulePaymentProcessingTimeout(UUID orderId) {
        String key = buildTimeoutKey(PAYMENT_PROCESSING_TYPE, orderId);
        
        try {
            redisTemplate.opsForValue().set(
                key,
                orderId.toString(),
                paymentProcessingTimeout.toMillis(),
                TimeUnit.MILLISECONDS
            );
            
            log.info("Scheduled payment processing timeout for order: {} ({}ms)", 
                orderId, paymentProcessingTimeout.toMillis());
                
        } catch (Exception e) {
            log.error("Failed to schedule payment processing timeout for order: {}", orderId, e);
        }
    }
    
    /**
     * Cancel payment processing timeout
     */
    public void cancelPaymentProcessingTimeout(UUID orderId) {
        String key = buildTimeoutKey(PAYMENT_PROCESSING_TYPE, orderId);
        
        try {
            Boolean deleted = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                log.info("Cancelled payment processing timeout for order: {}", orderId);
            }
        } catch (Exception e) {
            log.error("Failed to cancel payment processing timeout for order: {}", orderId, e);
        }
    }
    
    /**
     * Schedule rider assignment timeout
     */
    public void scheduleRiderAssignmentTimeout(UUID orderId) {
        String key = buildTimeoutKey(RIDER_ASSIGNMENT_TYPE, orderId);
        
        try {
            redisTemplate.opsForValue().set(
                key,
                orderId.toString(),
                riderAssignmentTimeout.toMillis(),
                TimeUnit.MILLISECONDS
            );
            
            log.info("Scheduled rider assignment timeout for order: {} ({}ms)", 
                orderId, riderAssignmentTimeout.toMillis());
                
        } catch (Exception e) {
            log.error("Failed to schedule rider assignment timeout for order: {}", orderId, e);
        }
    }
    
    /**
     * Cancel rider assignment timeout
     */
    public void cancelRiderAssignmentTimeout(UUID orderId) {
        String key = buildTimeoutKey(RIDER_ASSIGNMENT_TYPE, orderId);
        
        try {
            Boolean deleted = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                log.info("Cancelled rider assignment timeout for order: {}", orderId);
            }
        } catch (Exception e) {
            log.error("Failed to cancel rider assignment timeout for order: {}", orderId, e);
        }
    }
    
    /**
     * Build timeout key with pattern: timeout:{type}:{orderId}
     */
    private String buildTimeoutKey(String type, UUID orderId) {
        return TIMEOUT_KEY_PREFIX + type + ":" + orderId;
    }
    
    /**
     * Parse orderId from timeout key
     */
    public static UUID parseOrderIdFromKey(String key) {
        String[] parts = key.split(":");
        if (parts.length >= 3) {
            return UUID.fromString(parts[2]);
        }
        throw new IllegalArgumentException("Invalid timeout key format: " + key);
    }
    
    /**
     * Parse timeout type from key
     */
    public static String parseTimeoutTypeFromKey(String key) {
        String[] parts = key.split(":");
        if (parts.length >= 2) {
            return parts[1];
        }
        throw new IllegalArgumentException("Invalid timeout key format: " + key);
    }
}
