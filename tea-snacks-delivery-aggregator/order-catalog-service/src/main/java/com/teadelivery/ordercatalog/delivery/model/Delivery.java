package com.teadelivery.ordercatalog.delivery.model;

import com.teadelivery.ordercatalog.fsm.DeliveryState;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Delivery Entity with FSM Support
 * Represents a delivery with complete state machine lifecycle
 */
@Entity
@Table(name = "deliveries", indexes = {
    @Index(name = "idx_deliveries_order_id", columnList = "order_id"),
    @Index(name = "idx_deliveries_rider_id", columnList = "rider_id"),
    @Index(name = "idx_deliveries_state", columnList = "state"),
    @Index(name = "idx_deliveries_created_at", columnList = "created_at DESC")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Delivery {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "delivery_id")
    private UUID deliveryId;
    
    @Column(name = "order_id", nullable = false)
    private UUID orderId;
    
    // ========== Rider Info ==========
    @Column(name = "rider_id")
    private UUID riderId;
    
    // ========== Delivery State ==========
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 32)
    private DeliveryState state = DeliveryState.PENDING_ASSIGNMENT;
    
    // ========== Pickup Location ==========
    @Column(name = "pickup_latitude", nullable = false, precision = 10, scale = 8)
    private BigDecimal pickupLatitude;
    
    @Column(name = "pickup_longitude", nullable = false, precision = 11, scale = 8)
    private BigDecimal pickupLongitude;
    
    @Type(JsonBinaryType.class)
    @Column(name = "pickup_address", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> pickupAddress;
    
    // ========== Delivery Location ==========
    @Column(name = "delivery_latitude", nullable = false, precision = 10, scale = 8)
    private BigDecimal deliveryLatitude;
    
    @Column(name = "delivery_longitude", nullable = false, precision = 11, scale = 8)
    private BigDecimal deliveryLongitude;
    
    @Type(JsonBinaryType.class)
    @Column(name = "delivery_address", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> deliveryAddress;
    
    // ========== Distance and Time ==========
    @Column(name = "distance_km", precision = 6, scale = 2)
    private BigDecimal distanceKm;
    
    @Column(name = "estimated_time_minutes")
    private Integer estimatedTimeMinutes;
    
    // ========== Timestamps ==========
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    
    @Column(name = "rider_accepted_at")
    private LocalDateTime riderAcceptedAt;
    
    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;
    
    @Column(name = "in_transit_at")
    private LocalDateTime inTransitAt;
    
    @Column(name = "arrived_at")
    private LocalDateTime arrivedAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    // ========== Cancellation ==========
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;
    
    // ========== Metadata ==========
    @Type(JsonBinaryType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata = new HashMap<>();
    
    // ========== Helper Methods ==========
    
    /**
     * Update state timestamp based on new state
     */
    public void updateStateTimestamp(DeliveryState newState) {
        LocalDateTime now = LocalDateTime.now();
        switch (newState) {
            case ASSIGNED -> this.assignedAt = now;
            case RIDER_ACCEPTED -> this.riderAcceptedAt = now;
            case PICKED_UP -> this.pickedUpAt = now;
            case IN_TRANSIT -> this.inTransitAt = now;
            case ARRIVED_AT_CUSTOMER -> this.arrivedAt = now;
            case DELIVERED -> this.deliveredAt = now;
            case CANCELLED -> this.cancelledAt = now;
        }
    }
    
    /**
     * Check if delivery is in a terminal state
     */
    public boolean isTerminal() {
        return state != null && state.isTerminal();
    }
    
    /**
     * Check if delivery is in progress
     */
    public boolean isInProgress() {
        return state != null && state.isInProgress();
    }
}
