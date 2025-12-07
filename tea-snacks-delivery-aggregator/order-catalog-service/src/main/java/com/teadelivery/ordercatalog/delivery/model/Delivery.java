package com.teadelivery.ordercatalog.delivery.model;

import com.teadelivery.ordercatalog.delivery.fsm.DeliveryState;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

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

    @Builder.Default
    @Column(name = "search_radius_km")
    private Double searchRadiusKm = 2.0;

    @Builder.Default
    @Column(name = "retry_count")
    private Integer retryCount = 0;

    // Location data (JSONB) - for API responses and backward compatibility
    @Type(JsonType.class)
    @Column(name = "pickup_location", columnDefinition = "jsonb")
    private String pickupLocation;

    @Type(JsonType.class)
    @Column(name = "delivery_location", columnDefinition = "jsonb")
    private String deliveryLocation;

    @Type(JsonType.class)
    @Column(name = "rider_location", columnDefinition = "jsonb")
    private String riderLocation;

    // PostGIS geometry fields for spatial queries (same approach as Rider)
    @Column(name = "pickup_geom", columnDefinition = "geometry(Point,4326)")
    private Point pickupGeom;

    @Column(name = "delivery_geom", columnDefinition = "geometry(Point,4326)")
    private Point deliveryGeom;

    @Column(name = "rider_geom", columnDefinition = "geometry(Point,4326)")
    private Point riderGeom;

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
