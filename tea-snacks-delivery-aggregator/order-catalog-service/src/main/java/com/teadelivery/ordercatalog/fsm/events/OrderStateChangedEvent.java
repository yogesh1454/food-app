package com.teadelivery.ordercatalog.fsm.events;

import com.teadelivery.ordercatalog.fsm.OrderState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Event published when an order state changes
 * Published to: order-events topic
 * Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStateChangedEvent {
    
    private UUID orderId;
    private String previousState;
    private String newState;
    private String trigger;
    private UUID customerId;
    private UUID restaurantId;
    private Instant timestamp;
    private Map<String, Object> metadata;
    
    // Additional fields for delivery creation
    private String pickupLocation;
    private String deliveryLocation;
    private BigDecimal deliveryFee;
    private UUID idempotencyKey;
    
    /**
     * Event version for schema evolution
     */
    @Builder.Default
    private String version = "1.0";
    
    /**
     * Helper methods for type-safe state access
     */
    public OrderState getFromState() {
        return previousState != null ? OrderState.valueOf(previousState) : null;
    }
    
    public OrderState getToState() {
        return newState != null ? OrderState.valueOf(newState) : null;
    }
}
