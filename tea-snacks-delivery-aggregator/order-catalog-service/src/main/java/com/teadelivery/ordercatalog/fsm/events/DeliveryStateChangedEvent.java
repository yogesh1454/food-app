package com.teadelivery.ordercatalog.fsm.events;

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
    
    /**
     * Event version for schema evolution
     */
    @Builder.Default
    private String version = "1.0";
}
