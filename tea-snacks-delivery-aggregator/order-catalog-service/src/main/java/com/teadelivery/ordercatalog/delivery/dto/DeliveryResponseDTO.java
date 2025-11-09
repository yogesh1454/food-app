package com.teadelivery.ordercatalog.delivery.dto;

import com.teadelivery.ordercatalog.fsm.DeliveryState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Delivery Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponseDTO {
    
    private UUID deliveryId;
    private UUID orderId;
    private UUID riderId;
    private DeliveryState state;
    private BigDecimal deliveryFee;
    
    // Locations
    private LocationDTO pickupLocation;
    private LocationDTO deliveryLocation;
    private LocationDTO riderLocation;
    
    // Timestamps
    private Instant riderAssignedAt;
    private Instant riderAcceptedAt;
    private Instant reachedRestaurantAt;
    private Instant pickedUpAt;
    private Instant deliveredAt;
    private Instant failedAt;
    private String failureReason;
    
    // Metrics
    private Integer restaurantWaitTimeMinutes;
    private Integer totalDeliveryTimeMinutes;
    
    private Instant createdAt;
    private Instant updatedAt;
}
