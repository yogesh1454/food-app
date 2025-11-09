package com.teadelivery.ordercatalog.delivery.model;

import com.teadelivery.ordercatalog.fsm.DeliveryState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Delivery Entity with FSM Support
 * Represents a delivery with complete state machine lifecycle
 * As per BE-003-22
 */
@Entity
@Table(name = "deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "delivery_id")
    private UUID deliveryId;
    
    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;
    
    @Column(name = "rider_id")
    private UUID riderId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private DeliveryState state;
    
    @Column(name = "delivery_fee", precision = 10, scale = 2)
    private BigDecimal deliveryFee;
    
    @Column(name = "search_radius_km")
    private Double searchRadiusKm = 2.0;
    
    @Column(name = "retry_count")
    private Integer retryCount = 0;
    
    // Location data (JSONB)
    @Column(name = "pickup_location", columnDefinition = "jsonb")
    private String pickupLocation;
    
    @Column(name = "delivery_location", columnDefinition = "jsonb")
    private String deliveryLocation;
    
    @Column(name = "rider_location", columnDefinition = "jsonb")
    private String riderLocation;
    
    // Timestamps
    @Column(name = "rider_assigned_at")
    private Instant riderAssignedAt;
    
    @Column(name = "rider_accepted_at")
    private Instant riderAcceptedAt;
    
    @Column(name = "reached_restaurant_at")
    private Instant reachedRestaurantAt;
    
    @Column(name = "picked_up_at")
    private Instant pickedUpAt;
    
    @Column(name = "delivered_at")
    private Instant deliveredAt;
    
    @Column(name = "failed_at")
    private Instant failedAt;
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    // Metrics
    @Column(name = "restaurant_wait_time_minutes")
    private Integer restaurantWaitTimeMinutes;
    
    @Column(name = "total_delivery_time_minutes")
    private Integer totalDeliveryTimeMinutes;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
    
}
