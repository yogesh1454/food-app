package com.teadelivery.ordercatalog.delivery.fsm.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Event published to request rider assignment
 * Published to: assignment-requests topic
 * Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiderAssignmentRequestEvent {
    
    private UUID requestId;
    private UUID orderId;
    private UUID deliveryId;
    private Location restaurantLocation;
    private Location customerLocation;
    private Integer estimatedPrepTime;
    private BigDecimal deliveryFee;
    private Instant timestamp;
    
    /**
     * Event version for schema evolution
     */
    @Builder.Default
    private String version = "1.0";
    
    /**
     * Location data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        private Double latitude;
        private Double longitude;
        private String address;
    }
}
