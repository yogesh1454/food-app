# BE-003-24: Rider Search & Notification Service

**Story ID:** BE-003-24  
**Story Points:** 5  
**Priority:** High (P1)  
**Sprint:** 17  
**Epic:** BE-003  
**Dependencies:** BE-003-23 (Smart Rider Assignment Algorithm)

---

## 📖 User Story

**As a** backend developer  
**I want** to implement rider search with geospatial queries and real-time notifications  
**So that** the system can quickly find nearby riders and notify them of delivery requests

---

## ✅ Acceptance Criteria

### 1. Rider Model & Repository
- [ ] Rider entity with location data (PostGIS POINT)
- [ ] RiderRepository with geospatial queries
- [ ] Find riders within radius using ST_DWithin
- [ ] Index on rider location for performance

### 2. Geospatial Search
- [ ] Search riders within radius (2km, 5km, 10km)
- [ ] Calculate distance between rider and restaurant
- [ ] Filter by online status and availability
- [ ] Cache rider locations in Redis (5 second TTL)

### 3. Rider Location Tracking
- [ ] REST API to update rider location
- [ ] Batch update rider locations (every 5 seconds)
- [ ] Store location history for analytics
- [ ] Validate location coordinates

### 4. Notification Service
- [ ] Send push notifications to riders
- [ ] Notify rider of new delivery request
- [ ] Notify rider of assignment cancellation
- [ ] Notify customer of rider assignment
- [ ] Notify restaurant of rider arrival

### 5. Rider Response Handling
- [ ] Accept delivery request
- [ ] Reject delivery request (with reason)
- [ ] Timeout handling (30 seconds)
- [ ] Penalty for rejection (5 min cooldown)

### 6. Analytics & Metrics
- [ ] Track rider acceptance rate
- [ ] Track average response time
- [ ] Track rider availability patterns
- [ ] Track location update frequency

---

## 🔧 Technical Implementation

### **Rider Entity**

```java
package com.teadelivery.ordercatalog.rider.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

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
        return isOnline && 
               !isOnBreak && 
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
```

### **Rider Repository**

```java
package com.teadelivery.ordercatalog.rider.repository;

import com.teadelivery.ordercatalog.rider.model.Rider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RiderRepository extends JpaRepository<Rider, UUID> {
    
    /**
     * Find riders within radius using PostGIS ST_DWithin
     * @param longitude Restaurant longitude
     * @param latitude Restaurant latitude
     * @param radiusMeters Search radius in meters
     * @return List of riders within radius
     */
    @Query(value = """
        SELECT r.* FROM riders r
        WHERE r.is_online = true
        AND r.is_on_break = false
        AND r.current_deliveries < 2
        AND (r.penalty_until IS NULL OR r.penalty_until < NOW())
        AND ST_DWithin(
            r.current_location,
            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
            :radiusMeters
        )
        ORDER BY ST_Distance(
            r.current_location,
            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography
        )
        """, nativeQuery = true)
    List<Rider> findByLocationWithinRadius(
        @Param("longitude") double longitude,
        @Param("latitude") double latitude,
        @Param("radiusMeters") double radiusMeters
    );
    
    /**
     * Count available riders within radius
     */
    @Query(value = """
        SELECT COUNT(*) FROM riders r
        WHERE r.is_online = true
        AND r.is_on_break = false
        AND r.current_deliveries < 2
        AND (r.penalty_until IS NULL OR r.penalty_until < NOW())
        AND ST_DWithin(
            r.current_location,
            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
            :radiusMeters
        )
        """, nativeQuery = true)
    int countAvailableRiders(
        @Param("longitude") double longitude,
        @Param("latitude") double latitude,
        @Param("radiusMeters") double radiusMeters
    );
    
    List<Rider> findByIsOnlineTrue();
    
    List<Rider> findByIsOnlineTrueAndIsOnBreakFalse();
}
```

### **Rider Location Service**

```java
package com.teadelivery.ordercatalog.rider.service;

import com.teadelivery.ordercatalog.rider.model.Rider;
import com.teadelivery.ordercatalog.rider.repository.RiderRepository;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@Slf4j
public class RiderLocationService {
    
    private final RiderRepository riderRepository;
    private final GeometryFactory geometryFactory;
    private final Queue<LocationUpdate> locationUpdateQueue;
    
    public RiderLocationService(RiderRepository riderRepository) {
        this.riderRepository = riderRepository;
        this.geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        this.locationUpdateQueue = new ConcurrentLinkedQueue<>();
    }
    
    /**
     * Get rider location (cached)
     */
    @Cacheable(value = "rider-locations", key = "#riderId")
    public Point getRiderLocation(UUID riderId) {
        return riderRepository.findById(riderId)
            .map(Rider::getCurrentLocation)
            .orElse(null);
    }
    
    /**
     * Update rider location (queued for batch processing)
     */
    @CacheEvict(value = "rider-locations", key = "#riderId")
    public void updateRiderLocation(UUID riderId, double latitude, double longitude) {
        // Validate coordinates
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid coordinates");
        }
        
        locationUpdateQueue.offer(new LocationUpdate(riderId, latitude, longitude));
        
        log.debug("Queued location update: riderId={}, lat={}, lon={}", 
                  riderId, latitude, longitude);
    }
    
    /**
     * Batch update rider locations (every 5 seconds)
     */
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void batchUpdateRiderLocations() {
        List<LocationUpdate> updates = locationUpdateQueue.stream()
            .limit(1000) // Process max 1000 updates per batch
            .toList();
        
        if (updates.isEmpty()) {
            return;
        }
        
        // Remove processed updates from queue
        updates.forEach(locationUpdateQueue::remove);
        
        // Batch update
        for (LocationUpdate update : updates) {
            try {
                Rider rider = riderRepository.findById(update.riderId())
                    .orElse(null);
                
                if (rider != null) {
                    Point point = geometryFactory.createPoint(
                        new Coordinate(update.longitude(), update.latitude())
                    );
                    rider.setCurrentLocation(point);
                    rider.setLastLocationUpdate(Instant.now());
                    riderRepository.save(rider);
                }
            } catch (Exception e) {
                log.error("Failed to update rider location: riderId={}", 
                         update.riderId(), e);
            }
        }
        
        log.info("Batch updated {} rider locations", updates.size());
    }
    
    private record LocationUpdate(UUID riderId, double latitude, double longitude) {}
}
```

### **Notification Service**

```java
package com.teadelivery.ordercatalog.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class NotificationService {
    
    // TODO: Integrate with Firebase Cloud Messaging (FCM) or similar
    
    /**
     * Send push notification to rider
     */
    public void notifyRider(UUID riderId, String title, Map<String, Object> data) {
        // TODO: Implement FCM push notification
        log.info("Sending notification to rider: riderId={}, title={}, data={}", 
                 riderId, title, data);
        
        // For now, just log
        // In production, send via FCM:
        // fcmService.send(rider.getDeviceToken(), title, data);
    }
    
    /**
     * Send notification to customer
     */
    public void notifyCustomer(UUID orderId, String message) {
        log.info("Sending notification to customer: orderId={}, message={}", 
                 orderId, message);
        
        // TODO: Implement customer notification
    }
    
    /**
     * Send notification to restaurant
     */
    public void notifyRestaurant(UUID restaurantId, String message) {
        log.info("Sending notification to restaurant: restaurantId={}, message={}", 
                 restaurantId, message);
        
        // TODO: Implement restaurant notification
    }
    
    /**
     * Notify rider of new delivery request
     */
    public void notifyRiderOfDeliveryRequest(
        UUID riderId,
        UUID deliveryId,
        UUID orderId,
        String pickupAddress,
        String deliveryAddress,
        BigDecimal deliveryFee
    ) {
        Map<String, Object> data = Map.of(
            "type", "DELIVERY_REQUEST",
            "deliveryId", deliveryId.toString(),
            "orderId", orderId.toString(),
            "pickupAddress", pickupAddress,
            "deliveryAddress", deliveryAddress,
            "deliveryFee", deliveryFee.toString(),
            "expiresIn", 30 // seconds
        );
        
        notifyRider(riderId, "New Delivery Request", data);
    }
    
    /**
     * Notify customer of rider assignment
     */
    public void notifyCustomerOfRiderAssignment(
        UUID orderId,
        String riderName,
        String riderPhone,
        double riderRating,
        int estimatedArrivalMinutes
    ) {
        String message = String.format(
            "Delivery partner %s (%.1f★) is on the way to restaurant. " +
            "Estimated arrival in %d minutes.",
            riderName, riderRating, estimatedArrivalMinutes
        );
        
        notifyCustomer(orderId, message);
    }
}
```

### **Rider Response Handler**

```java
package com.teadelivery.ordercatalog.delivery.service;

import com.teadelivery.ordercatalog.delivery.model.Delivery;
import com.teadelivery.ordercatalog.delivery.repository.DeliveryRepository;
import com.teadelivery.ordercatalog.fsm.DeliveryTrigger;
import com.teadelivery.ordercatalog.fsm.delivery.DeliveryFSM;
import com.teadelivery.ordercatalog.rider.model.Rider;
import com.teadelivery.ordercatalog.rider.repository.RiderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class RiderResponseHandler {
    
    private final DeliveryRepository deliveryRepository;
    private final RiderRepository riderRepository;
    private final DeliveryFSM deliveryFSM;
    
    public RiderResponseHandler(
        DeliveryRepository deliveryRepository,
        RiderRepository riderRepository,
        DeliveryFSM deliveryFSM
    ) {
        this.deliveryRepository = deliveryRepository;
        this.riderRepository = riderRepository;
        this.deliveryFSM = deliveryFSM;
    }
    
    /**
     * Handle rider acceptance
     */
    public void handleRiderAcceptance(UUID deliveryId, UUID riderId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new DeliveryNotFoundException(
                "Delivery not found: " + deliveryId));
        
        Rider rider = riderRepository.findById(riderId)
            .orElseThrow(() -> new RiderNotFoundException(
                "Rider not found: " + riderId));
        
        // Update delivery
        delivery.setRiderId(riderId);
        deliveryRepository.save(delivery);
        
        // Update rider metrics
        rider.incrementAcceptedAssignments();
        rider.setCurrentDeliveries(rider.getCurrentDeliveries() + 1);
        riderRepository.save(rider);
        
        // Trigger FSM
        deliveryFSM.fire(deliveryId, DeliveryTrigger.RIDER_ACCEPT);
        
        log.info("Rider accepted delivery: deliveryId={}, riderId={}", 
                 deliveryId, riderId);
    }
    
    /**
     * Handle rider rejection
     */
    public void handleRiderRejection(UUID deliveryId, UUID riderId, String reason) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new DeliveryNotFoundException(
                "Delivery not found: " + deliveryId));
        
        Rider rider = riderRepository.findById(riderId)
            .orElseThrow(() -> new RiderNotFoundException(
                "Rider not found: " + riderId));
        
        // Update rider metrics
        rider.incrementAssignments();
        
        // Apply penalty (5 min cooldown)
        rider.setPenaltyUntil(Instant.now().plus(Duration.ofMinutes(5)));
        riderRepository.save(rider);
        
        // Trigger FSM (reassign)
        deliveryFSM.fire(deliveryId, DeliveryTrigger.RIDER_REJECT);
        
        log.info("Rider rejected delivery: deliveryId={}, riderId={}, reason={}", 
                 deliveryId, riderId, reason);
    }
}
```

### **Database Migration**

```sql
-- V1.9__create_riders_table.sql

-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE riders (
    rider_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(255),
    
    -- Location (PostGIS POINT)
    current_location geometry(Point, 4326),
    last_location_update TIMESTAMP,
    
    -- Status
    is_online BOOLEAN DEFAULT false,
    is_on_break BOOLEAN DEFAULT false,
    current_deliveries INTEGER DEFAULT 0,
    
    -- Metrics
    rating DECIMAL(3, 2) DEFAULT 5.00,
    total_deliveries INTEGER DEFAULT 0,
    completed_deliveries_today INTEGER DEFAULT 0,
    acceptance_rate DECIMAL(5, 2) DEFAULT 100.00,
    total_assignments INTEGER DEFAULT 0,
    accepted_assignments INTEGER DEFAULT 0,
    
    -- Penalty
    penalty_until TIMESTAMP,
    
    -- Device info
    device_token TEXT,
    device_platform VARCHAR(20),
    
    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT chk_rating CHECK (rating >= 0 AND rating <= 5),
    CONSTRAINT chk_acceptance_rate CHECK (acceptance_rate >= 0 AND acceptance_rate <= 100)
);

-- Spatial index for location queries
CREATE INDEX idx_riders_location ON riders USING GIST(current_location);

-- Other indexes
CREATE INDEX idx_riders_online ON riders(is_online, is_on_break) 
    WHERE is_online = true AND is_on_break = false;
CREATE INDEX idx_riders_rating ON riders(rating DESC);
CREATE INDEX idx_riders_acceptance_rate ON riders(acceptance_rate DESC);

-- Trigger for updated_at
CREATE TRIGGER update_riders_updated_at
    BEFORE UPDATE ON riders
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
```

---

## 📋 Testing Requirements

### **Unit Tests**
- [ ] Test geospatial queries
- [ ] Test rider availability filtering
- [ ] Test location update queuing
- [ ] Test batch location updates
- [ ] Test notification sending
- [ ] Test rider response handling

### **Integration Tests**
- [ ] Test rider search within radius
- [ ] Test location caching
- [ ] Test rider acceptance flow
- [ ] Test rider rejection with penalty
- [ ] Test concurrent location updates

---

## 📚 References

- [Smart Assignment Algorithm](../../business-flows/06_SMART_ASSIGNMENT_ALGORITHM.md)
- [Delivery FSM Design](../../business-flows/03_DELIVERY_FSM_DESIGN.md)
- [BE-003-23: Smart Rider Assignment](./BE-003-23-smart-rider-assignment-v2.md)

---

## 🎯 Definition of Done

- [ ] Rider entity created with PostGIS location
- [ ] RiderRepository with geospatial queries
- [ ] RiderLocationService implemented
- [ ] NotificationService implemented
- [ ] RiderResponseHandler implemented
- [ ] Database migration created
- [ ] Spatial indexes created
- [ ] Location caching working
- [ ] Batch updates working
- [ ] Unit tests passing with > 80% coverage
- [ ] Integration tests passing
- [ ] Code reviewed and approved
- [ ] Documentation updated
