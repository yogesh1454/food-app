# Smart Rider Assignment Algorithm

**Document Version:** 1.0  
**Last Updated:** November 9, 2025  
**Epic:** Epic-4 - Order & Delivery Management  
**Status:** Draft

---

## Table of Contents

1. [Overview](#overview)
2. [Assignment Timing Strategy](#assignment-timing-strategy)
3. [Rider Ranking Algorithm](#rider-ranking-algorithm)
4. [Dynamic Pricing & Surge](#dynamic-pricing--surge)
5. [Edge Case Handling](#edge-case-handling)
6. [Performance Optimization](#performance-optimization)
7. [Implementation Guide](#implementation-guide)

---

## Overview

### Problem Statement

**Traditional Approach:** Assign rider AFTER food is ready
```
12:30 PM - Order placed
12:50 PM - Food ready
12:50 PM - Start looking for rider
12:55 PM - Rider assigned
1:00 PM  - Rider reaches restaurant
1:00 PM  - Pickup
1:15 PM  - Delivered

Total time: 45 minutes
Issue: Food sits for 10 minutes waiting for rider
```

**Smart Approach:** Assign rider DURING preparation
```
12:30 PM - Order placed
12:32 PM - Preparing starts (18 min estimated)
12:42 PM - Rider assigned (10 min into prep)
12:50 PM - Food ready
12:50 PM - Rider reaches restaurant (perfect timing!)
12:50 PM - Immediate pickup
1:05 PM  - Delivered

Total time: 35 minutes
Benefit: Hot food, 10 minutes faster
```

### Key Benefits

| Benefit | Impact |
|---------|--------|
| **Faster Delivery** | 5-10 minutes faster on average |
| **Hot Food** | Picked up within 2 minutes of being ready |
| **Better ETA** | More accurate delivery time prediction |
| **Customer Satisfaction** | Transparency (rider assigned early) |
| **Rider Efficiency** | Minimal wait time at restaurant (< 3 min) |
| **Restaurant Efficiency** | Food doesn't sit waiting |

---

## Assignment Timing Strategy

### Core Algorithm

```java
@Service
public class SmartRiderAssignmentService {
    
    public void scheduleRiderAssignment(Order order) {
        // Factor 1: Restaurant's estimated prep time
        int estimatedPrepTime = order.getEstimatedPrepTimeMinutes();
        
        // Factor 2: Historical prep time for this restaurant
        int avgPrepTime = restaurantAnalytics.getAveragePrepTime(
            order.getRestaurantId(),
            order.getOrderItems()
        );
        
        // Factor 3: Current restaurant load
        int currentOrders = restaurantService.getCurrentOrderCount(
            order.getRestaurantId()
        );
        int loadFactor = currentOrders > 5 ? 5 : 0; // Add 5 min if busy
        
        // Factor 4: Average rider travel time to this restaurant
        int avgRiderTravelTime = riderAnalytics.getAverageTravelTime(
            order.getRestaurantLocation(),
            LocalTime.now()
        );
        
        // Factor 5: Time of day (peak hours = more buffer)
        int peakHourBuffer = isPeakHour() ? 3 : 0;
        
        // Calculate optimal assignment delay
        int adjustedPrepTime = Math.max(estimatedPrepTime, avgPrepTime) + loadFactor;
        int assignmentDelay = adjustedPrepTime - avgRiderTravelTime - peakHourBuffer - 2;
        
        // Constraints
        assignmentDelay = Math.max(2, assignmentDelay); // Min 2 min delay
        assignmentDelay = Math.min(15, assignmentDelay); // Max 15 min delay
        
        log.info("Scheduling rider assignment for order {} in {} minutes", 
                 order.getOrderId(), assignmentDelay);
        
        scheduleAssignment(order.getOrderId(), assignmentDelay);
    }
    
    private boolean isPeakHour() {
        LocalTime now = LocalTime.now();
        return (now.isAfter(LocalTime.of(12, 0)) && now.isBefore(LocalTime.of(14, 0))) ||
               (now.isAfter(LocalTime.of(19, 0)) && now.isBefore(LocalTime.of(22, 0)));
    }
    
    private void scheduleAssignment(UUID orderId, int delayMinutes) {
        if (delayMinutes <= 0) {
            // Assign immediately
            deliveryService.assignRider(orderId);
        } else {
            // Schedule using Redis TTL
            String key = "rider_assignment:" + orderId;
            redisTemplate.opsForValue().set(
                key,
                orderId.toString(),
                Duration.ofMinutes(delayMinutes)
            );
            
            // Redis keyspace notification will trigger assignment
        }
    }
}
```

### Timing Examples

#### Example 1: Normal Order

```
Restaurant: Chai Express
Estimated prep time: 15 minutes
Historical avg prep time: 18 minutes
Current load: 3 orders (normal)
Avg rider travel time: 5 minutes
Time: 3:00 PM (non-peak)

Calculation:
adjustedPrepTime = max(15, 18) + 0 = 18 min
assignmentDelay = 18 - 5 - 0 - 2 = 11 min

Timeline:
3:00 PM - Order placed, preparing starts
3:11 PM - Rider assigned (11 min into prep)
3:16 PM - Rider reaches restaurant
3:18 PM - Food ready (perfect timing!)
3:18 PM - Immediate pickup
```

#### Example 2: Peak Hour, Busy Restaurant

```
Restaurant: Pizza Corner
Estimated prep time: 20 minutes
Historical avg prep time: 22 minutes
Current load: 8 orders (busy)
Avg rider travel time: 7 minutes
Time: 7:30 PM (peak hour)

Calculation:
adjustedPrepTime = max(20, 22) + 5 = 27 min
peakHourBuffer = 3 min
assignmentDelay = 27 - 7 - 3 - 2 = 15 min

Timeline:
7:30 PM - Order placed, preparing starts
7:45 PM - Rider assigned (15 min into prep, max delay)
7:52 PM - Rider reaches restaurant
7:57 PM - Food ready (5 min wait for rider)
```

#### Example 3: Fast Food, Close Rider

```
Restaurant: Quick Bites
Estimated prep time: 8 minutes
Historical avg prep time: 10 minutes
Current load: 2 orders (light)
Avg rider travel time: 3 minutes
Time: 11:00 AM (non-peak)

Calculation:
adjustedPrepTime = max(8, 10) + 0 = 10 min
assignmentDelay = 10 - 3 - 0 - 2 = 5 min

Timeline:
11:00 AM - Order placed, preparing starts
11:05 AM - Rider assigned (5 min into prep)
11:08 AM - Rider reaches restaurant
11:10 AM - Food ready (2 min wait for rider)
```

---

## Rider Ranking Algorithm

### Scoring Formula

```java
public class RiderRankingService {
    
    public List<Rider> rankRiders(
        List<Rider> availableRiders, 
        Location restaurant
    ) {
        return availableRiders.stream()
            .map(rider -> {
                double distance = calculateDistance(
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
                    acceptanceRate * 0.20 +              // Acceptance rate (20%)
                    (1.0 / (currentLoad + 1)) * 0.10 +   // Current load (10%)
                    (completedToday / 20.0) * 0.10;      // Activity (10%)
                
                return new ScoredRider(rider, score);
            })
            .sorted(Comparator.comparing(ScoredRider::getScore).reversed())
            .map(ScoredRider::getRider)
            .collect(Collectors.toList());
    }
    
    public List<Rider> findAvailableRiders(
        Location restaurant,
        double radiusKm
    ) {
        // Query riders within radius
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
}
```

### Ranking Factors

| Factor | Weight | Description | Example |
|--------|--------|-------------|---------|
| **Distance** | 35% | Proximity to restaurant | 1 km = 0.50, 5 km = 0.17 |
| **Rating** | 25% | Customer rating | 4.8/5 = 0.96 |
| **Acceptance Rate** | 20% | % of accepted assignments | 90% = 0.90 |
| **Current Load** | 10% | Active deliveries | 0 = 1.00, 1 = 0.50 |
| **Activity** | 10% | Deliveries completed today | 10/20 = 0.50 |

### Example Ranking

```
Available Riders:

Rider A:
  Distance: 1.2 km
  Rating: 4.9/5
  Acceptance: 95%
  Load: 0
  Completed: 12
  Score: 0.29 + 0.25 + 0.19 + 0.10 + 0.06 = 0.89

Rider B:
  Distance: 0.5 km
  Rating: 4.5/5
  Acceptance: 80%
  Load: 1
  Completed: 8
  Score: 0.35 + 0.23 + 0.16 + 0.05 + 0.04 = 0.83

Rider C:
  Distance: 2.5 km
  Rating: 5.0/5
  Acceptance: 100%
  Load: 0
  Completed: 15
  Score: 0.14 + 0.25 + 0.20 + 0.10 + 0.08 = 0.77

Ranking: A > B > C
Selected: Rider A
```

---

## Dynamic Pricing & Surge

### Surge Pricing Algorithm

```java
@Service
public class SurgePricingService {
    
    public BigDecimal calculateDeliveryFee(
        Location restaurant,
        Location customer,
        LocalTime orderTime
    ) {
        // Base fee
        BigDecimal baseFee = new BigDecimal("30");
        
        // Distance fee
        double distance = calculateDistance(restaurant, customer);
        BigDecimal distanceFee = new BigDecimal(distance)
            .multiply(new BigDecimal("5")); // ₹5 per km
        
        // Surge multiplier
        double surgeMultiplier = calculateSurgeMultiplier(
            restaurant,
            orderTime
        );
        
        BigDecimal totalFee = baseFee.add(distanceFee)
            .multiply(new BigDecimal(surgeMultiplier));
        
        return totalFee.setScale(2, RoundingMode.HALF_UP);
    }
    
    private double calculateSurgeMultiplier(
        Location restaurant,
        LocalTime orderTime
    ) {
        // Factor 1: Demand (orders in last 15 min)
        int recentOrders = orderRepository.countRecentOrders(
            restaurant,
            Duration.ofMinutes(15)
        );
        
        // Factor 2: Supply (available riders)
        int availableRiders = riderRepository.countAvailableRiders(
            restaurant,
            2.0 // within 2 km
        );
        
        // Factor 3: Time of day
        boolean isPeakHour = isPeakHour(orderTime);
        
        // Calculate demand/supply ratio
        double demandSupplyRatio = availableRiders > 0 
            ? (double) recentOrders / availableRiders 
            : 5.0;
        
        // Base surge
        double surge = 1.0;
        
        // Apply surge based on demand/supply
        if (demandSupplyRatio > 3.0) {
            surge = 2.0; // 2x surge
        } else if (demandSupplyRatio > 2.0) {
            surge = 1.5; // 1.5x surge
        } else if (demandSupplyRatio > 1.5) {
            surge = 1.3; // 1.3x surge
        } else if (demandSupplyRatio > 1.0) {
            surge = 1.2; // 1.2x surge
        }
        
        // Peak hour boost
        if (isPeakHour && surge < 1.2) {
            surge = 1.2;
        }
        
        // Cap at 2.5x
        return Math.min(surge, 2.5);
    }
}
```

### Surge Pricing Matrix

| Demand/Supply Ratio | Peak Hour | Surge Multiplier | Example Fee |
|---------------------|-----------|------------------|-------------|
| < 1.0 | No | 1.0x | ₹40 |
| < 1.0 | Yes | 1.2x | ₹48 |
| 1.0 - 1.5 | No | 1.2x | ₹48 |
| 1.5 - 2.0 | No | 1.3x | ₹52 |
| 2.0 - 3.0 | No | 1.5x | ₹60 |
| > 3.0 | No | 2.0x | ₹80 |
| > 3.0 | Yes | 2.0x | ₹80 |

---

## Edge Case Handling

### Case 1: Food Ready Before Rider Arrives

```java
@Component
public class EarlyFoodReadyHandler {
    
    @EventListener
    public void onFoodReady(OrderReadyEvent event) {
        Order order = event.getOrder();
        Delivery delivery = deliveryRepository.findByOrderId(order.getOrderId());
        
        if (delivery == null || 
            delivery.getState() == DeliveryState.SEARCHING_RIDER) {
            // Food ready but no rider yet
            log.warn("Food ready before rider assigned: {}", order.getOrderId());
            
            // Immediate assignment
            deliveryService.assignRider(order.getOrderId());
            
            // Notify customer
            notificationService.send(
                order.getCustomerId(),
                "Food is ready! Finding delivery partner..."
            );
        } else if (delivery.getState() == DeliveryState.RIDER_ACCEPTED) {
            // Rider on the way
            int etaMinutes = calculateRiderETA(delivery);
            
            // Notify customer
            notificationService.send(
                order.getCustomerId(),
                "Food is ready! Delivery partner will arrive in " + 
                etaMinutes + " minutes"
            );
        }
    }
}
```

### Case 2: Rider Arrives Before Food Ready

```java
@Component
public class EarlyRiderArrivalHandler {
    
    @EventListener
    public void onRiderAtRestaurant(DeliveryAtRestaurantEvent event) {
        Delivery delivery = event.getDelivery();
        Order order = orderRepository.findById(delivery.getOrderId());
        
        if (order.getState() != OrderState.READY_FOR_PICKUP) {
            // Rider arrived but food not ready
            int remainingTime = calculateRemainingPrepTime(order);
            
            log.info("Rider arrived early: {} minutes wait expected", 
                     remainingTime);
            
            // Notify rider
            notificationService.send(
                delivery.getRiderId(),
                "Food will be ready in " + remainingTime + " minutes"
            );
            
            // Notify customer
            notificationService.send(
                order.getCustomerId(),
                "Delivery partner is at the restaurant. " +
                "Food will be ready in " + remainingTime + " minutes"
            );
            
            // Compensate rider if wait > 5 min
            if (remainingTime > 5) {
                compensateRider(delivery, remainingTime);
            }
        }
    }
    
    private void compensateRider(Delivery delivery, int waitMinutes) {
        // ₹10 per 5 minutes of wait
        BigDecimal compensation = new BigDecimal(waitMinutes / 5 * 10);
        
        riderCompensationService.addWaitTimeCompensation(
            delivery.getRiderId(),
            delivery.getDeliveryId(),
            compensation
        );
        
        log.info("Compensating rider {} with ₹{} for {} min wait",
                 delivery.getRiderId(), compensation, waitMinutes);
    }
}
```

### Case 3: Restaurant Delays Preparation

```java
@Component
public class RestaurantDelayHandler {
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void checkForDelays() {
        List<Order> preparingOrders = orderRepository.findByState(
            OrderState.PREPARING
        );
        
        for (Order order : preparingOrders) {
            int elapsedTime = calculateElapsedTime(order);
            int estimatedTime = order.getEstimatedPrepTimeMinutes();
            
            if (elapsedTime > estimatedTime + 5) {
                // Delay detected
                handleDelay(order, elapsedTime - estimatedTime);
            }
        }
    }
    
    private void handleDelay(Order order, int delayMinutes) {
        log.warn("Restaurant delay detected: order {}, delay {} min",
                 order.getOrderId(), delayMinutes);
        
        // Update customer ETA
        notificationService.send(
            order.getCustomerId(),
            "Your order is taking a bit longer. " +
            "Updated delivery time: " + calculateNewETA(order)
        );
        
        // Check if rider is waiting
        Delivery delivery = deliveryRepository.findByOrderId(order.getOrderId());
        if (delivery != null && 
            delivery.getState() == DeliveryState.AT_RESTAURANT) {
            
            // Offer rider option to cancel
            notificationService.send(
                delivery.getRiderId(),
                "Food is delayed by " + delayMinutes + " minutes. " +
                "You can cancel without penalty or wait for compensation."
            );
            
            // Compensate rider
            compensateRider(delivery, delayMinutes);
        }
        
        // Log for restaurant performance tracking
        restaurantAnalytics.recordDelay(
            order.getRestaurantId(),
            delayMinutes
        );
    }
}
```

### Case 4: No Riders Available

```java
@Component
public class NoRidersAvailableHandler {
    
    public void handleNoRidersAvailable(Delivery delivery) {
        int retryCount = delivery.getRetryCount();
        
        if (retryCount >= 3) {
            // Failed after 3 retries
            log.error("No riders available after 3 retries: {}", 
                     delivery.getDeliveryId());
            
            // Cancel delivery
            deliveryFSM.fire(
                delivery.getDeliveryId(), 
                DeliveryTrigger.FAIL_DELIVERY
            );
            
            // Notify customer
            notificationService.send(
                delivery.getOrder().getCustomerId(),
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
        double currentRadius = delivery.getSearchRadius();
        double newRadius = Math.min(currentRadius * 1.5, 10.0); // Max 10 km
        delivery.setSearchRadius(newRadius);
        
        deliveryRepository.save(delivery);
        
        log.info("Retrying rider assignment (attempt {}): fee={}, radius={}",
                 retryCount + 1, newFee, newRadius);
        
        // Retry assignment
        deliveryFSM.fire(
            delivery.getDeliveryId(),
            DeliveryTrigger.FIND_RIDERS
        );
        
        // Notify customer
        notificationService.send(
            delivery.getOrder().getCustomerId(),
            "Finding delivery partner... This may take a few more minutes."
        );
    }
}
```

---

## Performance Optimization

### Caching Strategy

```java
@Service
public class RiderLocationCacheService {
    
    @Cacheable(value = "rider-locations", key = "#riderId")
    public Location getRiderLocation(UUID riderId) {
        return riderRepository.findById(riderId)
            .map(Rider::getCurrentLocation)
            .orElse(null);
    }
    
    @CacheEvict(value = "rider-locations", key = "#riderId")
    public void updateRiderLocation(UUID riderId, Location location) {
        Rider rider = riderRepository.findById(riderId).orElseThrow();
        rider.setCurrentLocation(location);
        riderRepository.save(rider);
    }
    
    // Bulk update for efficiency
    @Scheduled(fixedRate = 5000) // Every 5 seconds
    public void batchUpdateRiderLocations() {
        List<RiderLocationUpdate> updates = 
            locationUpdateQueue.drainAll();
        
        if (!updates.isEmpty()) {
            riderRepository.batchUpdateLocations(updates);
            
            // Evict cache for updated riders
            updates.forEach(update -> 
                cacheManager.getCache("rider-locations")
                    .evict(update.getRiderId())
            );
        }
    }
}
```

### Database Indexing

```sql
-- Optimize rider queries
CREATE INDEX idx_riders_location ON riders USING GIST(current_location);
CREATE INDEX idx_riders_online ON riders(is_online, is_on_break) 
    WHERE is_online = true AND is_on_break = false;
CREATE INDEX idx_riders_rating ON riders(rating DESC);

-- Optimize order queries
CREATE INDEX idx_orders_state_created ON orders(state, created_at);
CREATE INDEX idx_orders_restaurant_state ON orders(restaurant_id, state);
```

---

## Implementation Guide

### Step 1: Set up Redis for Scheduled Assignment

```java
@Configuration
public class RedisConfig {
    
    @Bean
    public RedisMessageListenerContainer redisContainer(
        RedisConnectionFactory connectionFactory
    ) {
        RedisMessageListenerContainer container = 
            new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        // Listen for keyspace notifications
        container.addMessageListener(
            new RiderAssignmentKeyExpirationListener(),
            new PatternTopic("__keyevent@0__:expired")
        );
        
        return container;
    }
}

@Component
public class RiderAssignmentKeyExpirationListener 
    implements MessageListener {
    
    @Autowired
    private DeliveryService deliveryService;
    
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = new String(message.getBody());
        
        if (expiredKey.startsWith("rider_assignment:")) {
            UUID orderId = UUID.fromString(
                expiredKey.substring("rider_assignment:".length())
            );
            
            log.info("Triggering rider assignment for order: {}", orderId);
            deliveryService.assignRider(orderId);
        }
    }
}
```

### Step 2: Implement Assignment Service

```java
@Service
public class DeliveryAssignmentService {
    
    @Autowired
    private RiderRankingService rankingService;
    
    @Autowired
    private DeliveryFSM deliveryFSM;
    
    @Transactional
    public void assignRider(UUID orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        
        // Create delivery if not exists
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
            .orElseGet(() -> createDelivery(order));
        
        // Find available riders
        List<Rider> availableRiders = rankingService.findAvailableRiders(
            order.getRestaurant().getLocation(),
            2.0 // 2 km radius
        );
        
        if (availableRiders.isEmpty()) {
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
        
        for (Rider rider : topRiders) {
            sendAssignmentRequest(delivery, rider);
        }
        
        // Update delivery state
        deliveryFSM.fire(
            delivery.getDeliveryId(),
            DeliveryTrigger.ASSIGN_RIDER
        );
    }
}
```

---

## Next Steps

1. Review all design documents
2. Create implementation stories
3. Begin Phase 1 implementation
4. Monitor and optimize based on real-world data
