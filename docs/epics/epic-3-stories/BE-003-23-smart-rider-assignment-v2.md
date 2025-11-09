# BE-003-23: Smart Rider Assignment Algorithm

**Story ID:** BE-003-23  
**Story Points:** 8  
**Priority:** High (P1)  
**Sprint:** 17  
**Epic:** BE-003  
**Dependencies:** BE-003-22 (Delivery FSM Implementation)

---

## 📖 User Story

**As a** backend developer  
**I want** to implement a smart rider assignment algorithm that assigns riders during food preparation  
**So that** riders arrive at the restaurant exactly when food is ready, minimizing wait time and ensuring hot food delivery

---

## ✅ Acceptance Criteria

### 1. Smart Timing Algorithm
- [ ] Calculate optimal rider assignment delay based on:
  - Restaurant's estimated prep time
  - Historical average prep time
  - Current restaurant load
  - Average rider travel time
  - Peak hour buffer
- [ ] Minimum assignment delay: 2 minutes
- [ ] Maximum assignment delay: 15 minutes
- [ ] Schedule assignment using Redis TTL

### 2. Rider Ranking Algorithm
- [ ] Find available riders within search radius (default 2km)
- [ ] Rank riders by weighted scoring:
  - Distance (35% weight)
  - Rating (25% weight)
  - Acceptance rate (20% weight)
  - Current load (10% weight)
  - Activity today (10% weight)
- [ ] Select top 3 riders for assignment

### 3. Rider Availability Filtering
- [ ] Filter riders by:
  - Online status
  - Not on break
  - Max 2 concurrent deliveries
  - Within search radius
- [ ] Cache rider locations in Redis

### 4. Assignment Scheduling
- [ ] Schedule assignment using Redis keyspace notifications
- [ ] Listen for `rider_assignment:{orderId}` key expiration
- [ ] Trigger rider search on expiration
- [ ] Handle immediate assignment (delay = 0)

### 5. Retry Logic
- [ ] Retry up to 3 times if no riders available
- [ ] Increase delivery fee by 20% on each retry
- [ ] Expand search radius (2km → 3km → 5km)
- [ ] Notify customer of delay
- [ ] Fail delivery after 3 retries

### 6. Edge Case Handling
- [ ] Food ready before rider assigned → immediate assignment
- [ ] Rider arrives before food ready → compensate rider
- [ ] Restaurant delays preparation → update ETA
- [ ] All riders reject → apply surge pricing

---

## 🔧 Technical Implementation

### **Smart Assignment Service**

```java
package com.teadelivery.ordercatalog.delivery.service;

import com.teadelivery.ordercatalog.order.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.UUID;

@Service
@Slf4j
public class SmartRiderAssignmentService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestaurantAnalyticsService restaurantAnalytics;
    private final RiderAnalyticsService riderAnalytics;
    private final DeliveryService deliveryService;
    
    public SmartRiderAssignmentService(
        RedisTemplate<String, Object> redisTemplate,
        RestaurantAnalyticsService restaurantAnalytics,
        RiderAnalyticsService riderAnalytics,
        DeliveryService deliveryService
    ) {
        this.redisTemplate = redisTemplate;
        this.restaurantAnalytics = restaurantAnalytics;
        this.riderAnalytics = riderAnalytics;
        this.deliveryService = deliveryService;
    }
    
    /**
     * Schedule smart rider assignment based on preparation time
     */
    public void scheduleRiderAssignment(Order order) {
        // Factor 1: Restaurant's estimated prep time
        int estimatedPrepTime = order.getEstimatedPrepTimeMinutes();
        
        // Factor 2: Historical prep time for this restaurant
        int avgPrepTime = restaurantAnalytics.getAveragePrepTime(
            order.getRestaurantId(),
            order.getItems()
        );
        
        // Factor 3: Current restaurant load
        int currentOrders = restaurantAnalytics.getCurrentOrderCount(
            order.getRestaurantId()
        );
        int loadFactor = currentOrders > 5 ? 5 : 0; // Add 5 min if busy
        
        // Factor 4: Average rider travel time
        int avgRiderTravelTime = riderAnalytics.getAverageTravelTime(
            order.getRestaurant().getLocation(),
            LocalTime.now()
        );
        
        // Factor 5: Peak hour buffer
        int peakHourBuffer = isPeakHour() ? 3 : 0;
        
        // Calculate optimal assignment delay
        int adjustedPrepTime = Math.max(estimatedPrepTime, avgPrepTime) + loadFactor;
        int assignmentDelay = adjustedPrepTime - avgRiderTravelTime - peakHourBuffer - 2;
        
        // Constraints
        assignmentDelay = Math.max(2, assignmentDelay); // Min 2 min
        assignmentDelay = Math.min(15, assignmentDelay); // Max 15 min
        
        log.info("Scheduling rider assignment: orderId={}, delay={} min, " +
                 "prepTime={}, avgPrepTime={}, riderTravel={}, load={}, peak={}", 
                 order.getOrderId(), assignmentDelay, estimatedPrepTime, 
                 avgPrepTime, avgRiderTravelTime, loadFactor, peakHourBuffer);
        
        scheduleAssignment(order.getOrderId(), assignmentDelay);
    }
    
    /**
     * Schedule assignment using Redis TTL
     */
    private void scheduleAssignment(UUID orderId, int delayMinutes) {
        if (delayMinutes <= 0) {
            // Assign immediately
            log.info("Immediate rider assignment: orderId={}", orderId);
            deliveryService.assignRider(orderId);
        } else {
            // Schedule using Redis keyspace notification
            String key = "rider_assignment:" + orderId;
            redisTemplate.opsForValue().set(
                key,
                orderId.toString(),
                Duration.ofMinutes(delayMinutes)
            );
            
            log.info("Scheduled rider assignment: orderId={}, in {} minutes", 
                     orderId, delayMinutes);
        }
    }
    
    /**
     * Check if current time is peak hour
     */
    private boolean isPeakHour() {
        LocalTime now = LocalTime.now();
        return (now.isAfter(LocalTime.of(12, 0)) && now.isBefore(LocalTime.of(14, 0))) ||
               (now.isAfter(LocalTime.of(19, 0)) && now.isBefore(LocalTime.of(22, 0)));
    }
    
    /**
     * Cancel scheduled assignment (if order cancelled)
     */
    public void cancelScheduledAssignment(UUID orderId) {
        String key = "rider_assignment:" + orderId;
        Boolean deleted = redisTemplate.delete(key);
        
        if (Boolean.TRUE.equals(deleted)) {
            log.info("Cancelled scheduled rider assignment: orderId={}", orderId);
        }
    }
}
```

### **Rider Ranking Service**

```java
package com.teadelivery.ordercatalog.delivery.service;

import com.teadelivery.ordercatalog.rider.model.Rider;
import com.teadelivery.ordercatalog.rider.repository.RiderRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RiderRankingService {
    
    private final RiderRepository riderRepository;
    private final DistanceCalculationService distanceService;
    
    public RiderRankingService(
        RiderRepository riderRepository,
        DistanceCalculationService distanceService
    ) {
        this.riderRepository = riderRepository;
        this.distanceService = distanceService;
    }
    
    /**
     * Find available riders within radius
     */
    public List<Rider> findAvailableRiders(Location restaurant, double radiusKm) {
        // Query riders within radius (using PostGIS)
        List<Rider> nearbyRiders = riderRepository.findByLocationWithinRadius(
            restaurant,
            radiusKm
        );
        
        // Filter by availability
        return nearbyRiders.stream()
            .filter(Rider::isOnline)
            .filter(rider -> !rider.isOnBreak())
            .filter(rider -> rider.getCurrentDeliveries() < 2) // Max 2 concurrent
            .collect(Collectors.toList());
    }
    
    /**
     * Rank riders by weighted scoring
     */
    public List<Rider> rankRiders(List<Rider> availableRiders, Location restaurant) {
        return availableRiders.stream()
            .map(rider -> {
                double distance = distanceService.calculateDistance(
                    rider.getCurrentLocation(), 
                    restaurant
                );
                double rating = rider.getRating();
                double acceptanceRate = rider.getAcceptanceRate();
                int currentLoad = rider.getCurrentDeliveries();
                int completedToday = rider.getCompletedDeliveriesToday();
                
                // Scoring formula (weighted)
                double score = 
                    (1.0 / (distance + 1)) * 0.35 +     // Distance (35%)
                    (rating / 5.0) * 0.25 +              // Rating (25%)
                    acceptanceRate * 0.20 +              // Acceptance (20%)
                    (1.0 / (currentLoad + 1)) * 0.10 +   // Load (10%)
                    (completedToday / 20.0) * 0.10;      // Activity (10%)
                
                log.debug("Rider score: riderId={}, distance={}, rating={}, " +
                         "acceptance={}, load={}, completed={}, score={}", 
                         rider.getRiderId(), distance, rating, acceptanceRate, 
                         currentLoad, completedToday, score);
                
                return new ScoredRider(rider, score, distance);
            })
            .sorted(Comparator.comparing(ScoredRider::getScore).reversed())
            .map(ScoredRider::getRider)
            .collect(Collectors.toList());
    }
    
    @Data
    @AllArgsConstructor
    private static class ScoredRider {
        private Rider rider;
        private double score;
        private double distance;
    }
}
```

### **Rider Assignment Service**

```java
package com.teadelivery.ordercatalog.delivery.service;

import com.teadelivery.ordercatalog.delivery.model.Delivery;
import com.teadelivery.ordercatalog.delivery.repository.DeliveryRepository;
import com.teadelivery.ordercatalog.fsm.DeliveryTrigger;
import com.teadelivery.ordercatalog.fsm.delivery.DeliveryFSM;
import com.teadelivery.ordercatalog.order.model.Order;
import com.teadelivery.ordercatalog.order.repository.OrderRepository;
import com.teadelivery.ordercatalog.rider.model.Rider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class RiderAssignmentService {
    
    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final RiderRankingService rankingService;
    private final DeliveryFSM deliveryFSM;
    private final NotificationService notificationService;
    
    public RiderAssignmentService(
        DeliveryRepository deliveryRepository,
        OrderRepository orderRepository,
        RiderRankingService rankingService,
        DeliveryFSM deliveryFSM,
        NotificationService notificationService
    ) {
        this.deliveryRepository = deliveryRepository;
        this.orderRepository = orderRepository;
        this.rankingService = rankingService;
        this.deliveryFSM = deliveryFSM;
        this.notificationService = notificationService;
    }
    
    /**
     * Find and assign rider to delivery
     */
    public void findAndAssignRider(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new DeliveryNotFoundException(
                "Delivery not found: " + deliveryId));
        
        Order order = orderRepository.findById(delivery.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException(
                "Order not found: " + delivery.getOrderId()));
        
        // Find available riders
        double searchRadius = delivery.getSearchRadiusKm();
        List<Rider> availableRiders = rankingService.findAvailableRiders(
            order.getRestaurant().getLocation(),
            searchRadius
        );
        
        if (availableRiders.isEmpty()) {
            log.warn("No riders available: deliveryId={}, radius={}", 
                     deliveryId, searchRadius);
            handleNoRidersAvailable(delivery);
            return;
        }
        
        // Rank riders
        List<Rider> rankedRiders = rankingService.rankRiders(
            availableRiders,
            order.getRestaurant().getLocation()
        );
        
        // Send assignment to top 3 riders
        List<Rider> topRiders = rankedRiders.stream()
            .limit(3)
            .collect(Collectors.toList());
        
        log.info("Sending assignment to {} riders: deliveryId={}", 
                 topRiders.size(), deliveryId);
        
        for (Rider rider : topRiders) {
            sendAssignmentRequest(delivery, rider);
        }
        
        // Update delivery state
        deliveryFSM.fire(deliveryId, DeliveryTrigger.ASSIGN_RIDER);
    }
    
    /**
     * Handle no riders available scenario
     */
    private void handleNoRidersAvailable(Delivery delivery) {
        int retryCount = delivery.getRetryCount();
        
        if (retryCount >= 3) {
            // Failed after 3 retries
            log.error("No riders available after 3 retries: deliveryId={}", 
                     delivery.getDeliveryId());
            
            delivery.setFailureReason("No riders available");
            deliveryRepository.save(delivery);
            
            // Fail delivery
            deliveryFSM.fire(
                delivery.getDeliveryId(), 
                DeliveryTrigger.NO_RIDERS_AVAILABLE
            );
            
            // Notify customer
            notificationService.notifyCustomer(
                delivery.getOrderId(),
                "Unable to find delivery partner. " +
                "Your order has been cancelled and refund initiated."
            );
            
            return;
        }
        
        // Retry with increased incentives
        delivery.setRetryCount(retryCount + 1);
        
        // Increase delivery fee by 20%
        BigDecimal currentFee = delivery.getDeliveryFee();
        BigDecimal newFee = currentFee.multiply(new BigDecimal("1.2"));
        delivery.setDeliveryFee(newFee);
        
        // Expand search radius
        double currentRadius = delivery.getSearchRadiusKm();
        double newRadius = Math.min(currentRadius * 1.5, 10.0); // Max 10 km
        delivery.setSearchRadiusKm(newRadius);
        
        deliveryRepository.save(delivery);
        
        log.info("Retrying rider assignment (attempt {}): deliveryId={}, " +
                 "fee={}, radius={}", 
                 retryCount + 1, delivery.getDeliveryId(), newFee, newRadius);
        
        // Retry assignment
        findAndAssignRider(delivery.getDeliveryId());
        
        // Notify customer
        notificationService.notifyCustomer(
            delivery.getOrderId(),
            "Finding delivery partner... This may take a few more minutes."
        );
    }
    
    /**
     * Send assignment request to rider
     */
    private void sendAssignmentRequest(Delivery delivery, Rider rider) {
        // Send push notification to rider
        notificationService.notifyRider(
            rider.getRiderId(),
            "New delivery request",
            Map.of(
                "deliveryId", delivery.getDeliveryId(),
                "orderId", delivery.getOrderId(),
                "deliveryFee", delivery.getDeliveryFee(),
                "pickupLocation", delivery.getPickupLocation(),
                "deliveryLocation", delivery.getDeliveryLocation()
            )
        );
        
        log.info("Sent assignment request: deliveryId={}, riderId={}", 
                 delivery.getDeliveryId(), rider.getRiderId());
    }
}
```

### **Redis Key Expiration Listener**

```java
package com.teadelivery.ordercatalog.delivery.listener;

import com.teadelivery.ordercatalog.delivery.service.DeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class RiderAssignmentKeyExpirationListener implements MessageListener {
    
    private final DeliveryService deliveryService;
    
    public RiderAssignmentKeyExpirationListener(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }
    
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = new String(message.getBody());
        
        if (expiredKey.startsWith("rider_assignment:")) {
            UUID orderId = UUID.fromString(
                expiredKey.substring("rider_assignment:".length())
            );
            
            log.info("Rider assignment scheduled time reached: orderId={}", orderId);
            
            try {
                deliveryService.assignRider(orderId);
            } catch (Exception e) {
                log.error("Failed to assign rider: orderId={}", orderId, e);
            }
        }
    }
}
```

---

## 📋 Testing Requirements

### **Unit Tests**
- [ ] Test assignment delay calculation with various inputs
- [ ] Test peak hour detection
- [ ] Test rider ranking algorithm
- [ ] Test rider availability filtering
- [ ] Test retry logic
- [ ] Test edge case handling

### **Integration Tests**
- [ ] Test scheduled assignment via Redis
- [ ] Test immediate assignment (delay = 0)
- [ ] Test no riders available scenario
- [ ] Test rider rejection and reassignment
- [ ] Test search radius expansion

### **Performance Tests**
- [ ] Test with 100+ concurrent assignments
- [ ] Test rider location caching
- [ ] Test database query performance

---

## 📚 References

- [Smart Assignment Algorithm](../../business-flows/06_SMART_ASSIGNMENT_ALGORITHM.md)
- [Delivery FSM Design](../../business-flows/03_DELIVERY_FSM_DESIGN.md)
- [BE-003-22: Delivery FSM Implementation](./BE-003-22-delivery-fsm-implementation-v2.md)

---

## 🎯 Definition of Done

- [ ] SmartRiderAssignmentService implemented
- [ ] RiderRankingService implemented
- [ ] RiderAssignmentService implemented
- [ ] Redis key expiration listener configured
- [ ] Assignment delay calculation working
- [ ] Rider ranking algorithm working
- [ ] Retry logic working
- [ ] Edge case handling implemented
- [ ] Unit tests passing with > 80% coverage
- [ ] Integration tests passing
- [ ] Performance tests passing
- [ ] Code reviewed and approved
- [ ] Documentation updated
