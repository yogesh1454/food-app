package com.teadelivery.ordercatalog.timeout;

import com.teadelivery.ordercatalog.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.UUID;

/**
 * Redis Key Expiration Listener
 * Listens for Redis keyspace notifications when timeout keys expire
 * and triggers appropriate timeout handling logic
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RedisKeyExpirationListener implements MessageListener {
    
    private final RedisMessageListenerContainer listenerContainer;
    private final OrderService orderService;
    
    private static final String EXPIRATION_PATTERN = "__keyevent@*__:expired";
    private static final String TIMEOUT_PREFIX = "timeout:";
    
    @PostConstruct
    public void init() {
        // Subscribe to expired key events
        listenerContainer.addMessageListener(this, new PatternTopic(EXPIRATION_PATTERN));
        log.info("Redis key expiration listener initialized for pattern: {}", EXPIRATION_PATTERN);
    }
    
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        
        // Only process timeout keys
        if (!expiredKey.startsWith(TIMEOUT_PREFIX)) {
            return;
        }
        
        log.info("Received key expiration event: {}", expiredKey);
        
        try {
            handleTimeoutExpiration(expiredKey);
        } catch (Exception e) {
            log.error("Error handling timeout expiration for key: {}", expiredKey, e);
        }
    }
    
    /**
     * Handle timeout expiration based on timeout type
     */
    private void handleTimeoutExpiration(String expiredKey) {
        try {
            String timeoutType = OrderTimeoutService.parseTimeoutTypeFromKey(expiredKey);
            UUID orderId = OrderTimeoutService.parseOrderIdFromKey(expiredKey);
            
            log.info("Processing timeout: type={}, orderId={}", timeoutType, orderId);
            
            switch (timeoutType) {
                case "restaurant_acceptance":
                    handleRestaurantAcceptanceTimeout(orderId);
                    break;
                    
                case "payment_processing":
                    handlePaymentProcessingTimeout(orderId);
                    break;
                    
                case "rider_assignment":
                    handleRiderAssignmentTimeout(orderId);
                    break;
                    
                default:
                    log.warn("Unknown timeout type: {}", timeoutType);
            }
            
        } catch (Exception e) {
            log.error("Failed to parse timeout key: {}", expiredKey, e);
        }
    }
    
    /**
     * Handle restaurant acceptance timeout
     * Auto-reject order after 2 minutes
     */
    private void handleRestaurantAcceptanceTimeout(UUID orderId) {
        log.warn("Restaurant acceptance timeout for order: {}", orderId);
        
        try {
            // Auto-reject the order
            orderService.handleTimeout(orderId);
            
            log.info("Order auto-rejected due to restaurant timeout: {}", orderId);
            
            // TODO: Send notification to customer
            // TODO: Initiate refund if payment was captured
            
        } catch (Exception e) {
            log.error("Failed to handle restaurant acceptance timeout for order: {}", orderId, e);
            // TODO: Add to DLQ for manual intervention
        }
    }
    
    /**
     * Handle payment processing timeout
     */
    private void handlePaymentProcessingTimeout(UUID orderId) {
        log.warn("Payment processing timeout for order: {}", orderId);
        
        try {
            // Cancel the order due to payment timeout
            orderService.cancelOrder(orderId, null, "SYSTEM", "Payment processing timeout");
            
            log.info("Order cancelled due to payment timeout: {}", orderId);
            
        } catch (Exception e) {
            log.error("Failed to handle payment processing timeout for order: {}", orderId, e);
        }
    }
    
    /**
     * Handle rider assignment timeout
     */
    private void handleRiderAssignmentTimeout(UUID orderId) {
        log.warn("Rider assignment timeout for order: {}", orderId);
        
        try {
            // TODO: Implement retry logic or cancel order
            log.info("Rider assignment timeout - retry logic needed for order: {}", orderId);
            
            // For now, just log - actual implementation would:
            // 1. Retry rider assignment with expanded search radius
            // 2. Increase delivery fee to attract riders
            // 3. Cancel order if no rider found after multiple retries
            
        } catch (Exception e) {
            log.error("Failed to handle rider assignment timeout for order: {}", orderId, e);
        }
    }
}
