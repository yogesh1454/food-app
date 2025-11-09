package com.teadelivery.ordercatalog.fsm.events;

import com.teadelivery.ordercatalog.fsm.DeliveryState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Event published when a delivery state changes
 * Published to: delivery-events topic
 * Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryStateChangedEvent {
    
    private UUID deliveryId;
    private UUID orderId;
    private String previousState;
    private String newState;
    private String trigger;
    private UUID riderId;
    private Instant timestamp;
    private Map<String, Object> metadata;
    
    // Additional fields
    private UUID idempotencyKey;
    private String failureReason;
    
    /**
     * Event version for schema evolution
     */
    @Builder.Default
    private String version = "1.0";
    
    /**
     * Helper methods for type-safe state access
     */
    public DeliveryState getFromState() {
        return previousState != null ? DeliveryState.valueOf(previousState) : null;
    }
    
    public DeliveryState getToState() {
        return newState != null ? DeliveryState.valueOf(newState) : null;
    }
}
