package com.teadelivery.ordercatalog.delivery.rider.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/**
 * Rider Entity with PostGIS Location Support
 * As per BE-003-24
 */
@Entity
@Table(name = "riders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rider {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rider_id")
    private UUID riderId;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "phone", nullable = false, unique = true)
    private String phone;
    
    @Column(name = "email")
    private String email;
    
    // Location (PostGIS POINT)
    @Column(name = "current_location", columnDefinition = "geometry(Point,4326)")
    private Point currentLocation;
    
    @Column(name = "last_location_update")
    private Instant lastLocationUpdate;
    
    // Status
    @Column(name = "is_online")
    private Boolean isOnline = false;
    
    @Column(name = "is_on_break")
    private Boolean isOnBreak = false;
    
    @Column(name = "current_deliveries")
    private Integer currentDeliveries = 0;
    
    // Metrics
    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating = new BigDecimal("5.00");
    
    @Column(name = "total_deliveries")
    private Integer totalDeliveries = 0;
    
    @Column(name = "completed_deliveries_today")
    private Integer completedDeliveriesToday = 0;
    
    @Column(name = "acceptance_rate", precision = 5, scale = 2)
    private BigDecimal acceptanceRate = new BigDecimal("100.00");
    
    @Column(name = "total_assignments")
    private Integer totalAssignments = 0;
    
    @Column(name = "accepted_assignments")
    private Integer acceptedAssignments = 0;
    
    // Penalty
    @Column(name = "penalty_until")
    private Instant penaltyUntil;
    
    // Device info
    @Column(name = "device_token")
    private String deviceToken; // For push notifications
    
    @Column(name = "device_platform")
    private String devicePlatform; // iOS, Android
    
    // Audit
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    // Helper methods
    public boolean isAvailable() {
        return Boolean.TRUE.equals(isOnline) && 
               Boolean.FALSE.equals(isOnBreak) && 
               currentDeliveries < 2 &&
               (penaltyUntil == null || Instant.now().isAfter(penaltyUntil));
    }
    
    public void incrementAssignments() {
        this.totalAssignments++;
    }
    
    public void incrementAcceptedAssignments() {
        this.acceptedAssignments++;
        updateAcceptanceRate();
    }
    
    private void updateAcceptanceRate() {
        if (totalAssignments > 0) {
            this.acceptanceRate = new BigDecimal(acceptedAssignments)
                .divide(new BigDecimal(totalAssignments), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        }
    }
}
