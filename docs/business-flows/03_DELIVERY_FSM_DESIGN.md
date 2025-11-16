# Delivery FSM - Detailed Design

**Document Version:** 1.0  
**Last Updated:** November 9, 2025  
**Epic:** Epic-4 - Order & Delivery Management  
**Status:** Draft

---

## Table of Contents

1. [Overview](#overview)
2. [State Diagram](#state-diagram)
3. [States Definition](#states-definition)
4. [Triggers & Transitions](#triggers--transitions)
5. [Rider Assignment Algorithm](#rider-assignment-algorithm)
6. [Integration with Order FSM](#integration-with-order-fsm)
7. [Implementation Guide](#implementation-guide)

---

## Overview

### Purpose

The Delivery FSM manages the delivery lifecycle from rider assignment through successful delivery. It handles:

- Rider search and assignment
- Pickup coordination
- Delivery execution
- Location tracking
- Failure handling and reassignment

### Responsibilities

| Responsibility | Description |
|----------------|-------------|
| **Rider Assignment** | Find and assign available riders |
| **Pickup Coordination** | Guide rider to restaurant |
| **Delivery Execution** | Navigate rider to customer |
| **Location Tracking** | Real-time rider location updates |
| **Failure Handling** | Reassignment, escalation, compensation |
| **Performance Tracking** | Rider metrics, delivery times |

---

## State Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    DELIVERY STATE MACHINE                       │
└─────────────────────────────────────────────────────────────────┘

    [PENDING]
        │
        │ find_riders
        ▼
    [SEARCHING_RIDER] ─────────────────────────────┐
        │                                          │
        │ rider_found                              │ no_riders_available
        ▼                                          │ (retry with surge)
    [RIDER_ASSIGNED]                               │
        │                                          │
        │ rider_accepted                           │ rider_rejected
        ▼                                          │ (reassign)
    [RIDER_ACCEPTED]                               │
        │                                          │
        │ rider_reached_restaurant                 │
        ▼                                          │
    [AT_RESTAURANT]                                │
        │                                          │
        │ rider_picked_up                          │
        ▼                                          │
    [PICKED_UP]                                    │
        │                                          │
        │ rider_started_delivery                   │
        ▼                                          │
    [OUT_FOR_DELIVERY]                             │
        │                                          │
        │ rider_delivered                          │
        ▼                                          │
    [DELIVERED]                                    │
   (terminal)                                      │
                                                   │
    [FAILED] ◄─────────────────────────────────────┘
   (terminal)
```

---

## States Definition

### 1. PENDING

**Description:** Delivery record created, ready to find riders.

| Property | Value |
|----------|-------|
| **Timeout** | 30 seconds |
| **Next States** | SEARCHING_RIDER |
| **Customer Impact** | None (internal state) |

**Entry Actions:**
- Create delivery record
- Link to order
- Set pickup location (restaurant)
- Set delivery location (customer)

**Exit Actions:**
- None

---

### 2. SEARCHING_RIDER

**Description:** Actively searching for available riders.

| Property | Value |
|----------|-------|
| **Timeout** | 2 minutes |
| **Next States** | RIDER_ASSIGNED, FAILED |
| **Customer Impact** | "Finding delivery partner..." |

**Entry Actions:**
- Run rider assignment algorithm
- Find riders within search radius (2km initially)
- Rank by distance, rating, acceptance rate
- Send assignment to top 3 riders
- Set timeout for rider response

**During State:**
- Monitor rider responses
- If no response within 30s → expand radius, apply surge
- If all riders reject → FAILED

**Exit Actions:**
- Cancel pending assignments to other riders

---

### 3. RIDER_ASSIGNED

**Description:** Rider selected, awaiting acceptance.

| Property | Value |
|----------|-------|
| **Timeout** | 30 seconds |
| **Next States** | RIDER_ACCEPTED, SEARCHING_RIDER |
| **Customer Impact** | "Delivery partner assigned" |

**Entry Actions:**
- Notify selected rider
- Show delivery details (restaurant, customer, fee)
- Start acceptance timer (30s)
- Publish `delivery.rider_assigned` event

**Exit Actions:**
- If rejected → mark rider unavailable (5 min penalty)

---

### 4. RIDER_ACCEPTED

**Description:** Rider accepted, navigating to restaurant.

| Property | Value |
|----------|-------|
| **Timeout** | 15 minutes |
| **Next States** | AT_RESTAURANT, FAILED |
| **Customer Impact** | "Delivery partner is on the way to restaurant" |

**Entry Actions:**
- Notify customer (rider assigned)
- Notify restaurant (rider coming)
- Provide navigation to rider
- Start location tracking
- Publish `delivery.rider_accepted` event
- **Update Order FSM** (ASSIGN_RIDER trigger)

**During State:**
- Track rider location
- Update ETA to restaurant
- If timeout → contact rider, escalate

**Exit Actions:**
- None

---

### 5. AT_RESTAURANT

**Description:** Rider reached restaurant, picking up order.

| Property | Value |
|----------|-------|
| **Timeout** | 5 minutes |
| **Next States** | PICKED_UP, FAILED |
| **Customer Impact** | "Delivery partner is at the restaurant" |

**Entry Actions:**
- Notify restaurant (rider arrived)
- Notify customer (rider at restaurant)
- Record arrival timestamp
- Publish `delivery.at_restaurant` event

**During State:**
- Monitor pickup time
- If excessive wait (> 5 min) → compensate rider

**Exit Actions:**
- Calculate restaurant wait time

---

### 6. PICKED_UP

**Description:** Rider picked up order, ready to deliver.

| Property | Value |
|----------|-------|
| **Timeout** | 45 minutes |
| **Next States** | OUT_FOR_DELIVERY |
| **Customer Impact** | "Your order is on the way!" |

**Entry Actions:**
- Verify order with OTP/QR code
- Notify customer (order picked up)
- Start delivery timer
- Enable live tracking for customer
- Publish `delivery.picked_up` event
- **Update Order FSM** (RIDER_PICKUP trigger)

**Exit Actions:**
- None

---

### 7. OUT_FOR_DELIVERY

**Description:** Rider en route to customer.

| Property | Value |
|----------|-------|
| **Timeout** | 45 minutes |
| **Next States** | DELIVERED, FAILED |
| **Customer Impact** | "Arriving in X minutes" |

**Entry Actions:**
- Provide navigation to customer
- Update ETA continuously
- Publish `delivery.out_for_delivery` event

**During State:**
- Track rider location in real-time
- Update customer with ETA
- If timeout → contact rider, escalate

**Exit Actions:**
- None

---

### 8. DELIVERED

**Description:** Order successfully delivered.

| Property | Value |
|----------|-------|
| **Timeout** | None |
| **Next States** | None (terminal) |
| **Customer Impact** | "Order delivered. Enjoy your meal!" |

**Entry Actions:**
- Verify delivery with OTP/signature
- Collect COD payment (if applicable)
- Record delivery timestamp
- Calculate delivery time
- Notify customer (order delivered)
- Publish `delivery.delivered` event
- **Update Order FSM** (DELIVER_ORDER trigger)
- Calculate rider earnings
- Release rider for next delivery

**Exit Actions:**
- None

---

### 9. FAILED

**Description:** Delivery failed (terminal state).

| Property | Value |
|----------|-------|
| **Timeout** | None |
| **Next States** | None (terminal) |
| **Customer Impact** | "Delivery failed. Refund initiated." |

**Entry Actions:**
- Log failure reason
- Notify customer
- Initiate refund
- Penalize rider (if rider fault)
- Publish `delivery.failed` event
- **Update Order FSM** (CANCEL_ORDER trigger)

**Exit Actions:**
- None

**Failure Reasons:**
- No riders available (after 3 retry attempts)
- Rider cancelled after pickup
- Customer not reachable
- Address incorrect
- Accident/vehicle breakdown

---

## Triggers & Transitions

### Trigger: FIND_RIDERS

```java
FROM: PENDING
TO: SEARCHING_RIDER
```

**Actions:**
1. Run rider assignment algorithm
2. Find riders within 2km radius
3. Rank by distance, rating, acceptance rate
4. Send assignment to top 3 riders
5. Set timeout for rider response

**Events Published:**
- `delivery.searching_rider`

---

### Trigger: ASSIGN_RIDER

```java
FROM: SEARCHING_RIDER
TO: RIDER_ASSIGNED
```

**Actions:**
1. Notify selected rider
2. Show delivery details
3. Start acceptance timer (30s)

**Events Published:**
- `delivery.rider_assigned`

---

### Trigger: RIDER_ACCEPT

```java
FROM: RIDER_ASSIGNED
TO: RIDER_ACCEPTED
```

**Actions:**
1. Notify customer
2. Notify restaurant
3. Provide navigation to rider
4. Start location tracking
5. **Publish event to update Order FSM**

**Events Published:**
- `delivery.rider_accepted`

**Order FSM Integration:**
- Triggers `ASSIGN_RIDER` in Order FSM

---

### Trigger: RIDER_REJECT

```java
FROM: RIDER_ASSIGNED
TO: SEARCHING_RIDER
```

**Actions:**
1. Mark rider unavailable (5 min penalty)
2. Reassign to next available rider
3. Log rejection for rider metrics
4. If all riders reject → apply surge pricing

**Events Published:**
- `delivery.rider_rejected`

---

### Trigger: NO_RIDERS_AVAILABLE

```java
FROM: SEARCHING_RIDER
TO: SEARCHING_RIDER (retry) OR FAILED
```

**Actions:**
1. Increase delivery fee by 20%
2. Expand search radius to 5km
3. Notify customer of delay
4. Escalate to support team
5. If still no riders after 3 attempts → FAILED

**Events Published:**
- `delivery.no_riders_available`

---

### Trigger: REACH_RESTAURANT

```java
FROM: RIDER_ACCEPTED
TO: AT_RESTAURANT
```

**Actions:**
1. Notify restaurant
2. Notify customer
3. Record arrival timestamp

**Events Published:**
- `delivery.at_restaurant`

---

### Trigger: PICKUP_ORDER

```java
FROM: AT_RESTAURANT
TO: PICKED_UP
```

**Actions:**
1. Verify order with OTP/QR code
2. Notify customer
3. Start delivery timer
4. Enable live tracking
5. **Publish event to update Order FSM**

**Events Published:**
- `delivery.picked_up`

**Order FSM Integration:**
- Triggers `RIDER_PICKUP` in Order FSM

---

### Trigger: START_DELIVERY

```java
FROM: PICKED_UP
TO: OUT_FOR_DELIVERY
```

**Actions:**
1. Provide navigation to customer
2. Update ETA continuously

**Events Published:**
- `delivery.out_for_delivery`

---

### Trigger: DELIVER_ORDER

```java
FROM: OUT_FOR_DELIVERY
TO: DELIVERED
```

**Actions:**
1. Verify delivery with OTP/signature
2. Collect COD payment (if applicable)
3. Record delivery timestamp
4. Calculate delivery time
5. Notify customer
6. **Publish event to update Order FSM**
7. Calculate rider earnings
8. Release rider

**Events Published:**
- `delivery.delivered`

**Order FSM Integration:**
- Triggers `DELIVER_ORDER` in Order FSM

---

### Trigger: FAIL_DELIVERY

```java
FROM: SEARCHING_RIDER, RIDER_ASSIGNED, RIDER_ACCEPTED, AT_RESTAURANT, PICKED_UP, OUT_FOR_DELIVERY
TO: FAILED
```

**Actions:**
1. Log failure reason
2. Notify customer
3. Initiate refund
4. Penalize rider (if rider fault)
5. **Publish event to update Order FSM**

**Events Published:**
- `delivery.failed`

**Order FSM Integration:**
- Triggers `CANCEL_ORDER` in Order FSM

---

## Rider Assignment Algorithm

### Smart Assignment Strategy

```java
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
        int loadFactor = currentOrders > 5 ? 5 : 0;
        
        // Factor 4: Average rider travel time
        int avgRiderTravelTime = riderAnalytics.getAverageTravelTime(
            order.getRestaurantLocation(),
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
        
        scheduleAssignment(order.getOrderId(), assignmentDelay);
    }
}
```

### Rider Ranking Algorithm

```java
public List<Rider> rankRiders(List<Rider> availableRiders, Location restaurant) {
    return availableRiders.stream()
        .map(rider -> {
            double distance = calculateDistance(rider.getLocation(), restaurant);
            double rating = rider.getRating();
            double acceptanceRate = rider.getAcceptanceRate();
            int currentLoad = rider.getCurrentDeliveries();
            
            // Scoring formula
            double score = 
                (1.0 / (distance + 1)) * 0.4 +  // Distance (40% weight)
                (rating / 5.0) * 0.3 +           // Rating (30% weight)
                acceptanceRate * 0.2 +           // Acceptance rate (20% weight)
                (1.0 / (currentLoad + 1)) * 0.1; // Load (10% weight)
            
            return new ScoredRider(rider, score);
        })
        .sorted(Comparator.comparing(ScoredRider::getScore).reversed())
        .map(ScoredRider::getRider)
        .collect(Collectors.toList());
}
```

---

## Integration with Order FSM

### Event Flow

```
Order FSM                    Event                    Delivery FSM
─────────                    ─────                    ────────────

PREPARING ─────────► order.ready_for_pickup ─────► CREATE delivery
                                                    FIND_RIDERS

READY_FOR_PICKUP ◄── delivery.rider_accepted ◄──── RIDER_ACCEPT
(ASSIGN_RIDER)

ASSIGNED_TO_RIDER ◄─ delivery.picked_up ◄────────── PICKUP_ORDER
(RIDER_PICKUP)

PICKED_UP ◄───────── delivery.delivered ◄─────────── DELIVER_ORDER
(DELIVER_ORDER)

CANCELLED ◄───────── delivery.failed ◄──────────────  FAIL_DELIVERY
(CANCEL_ORDER)
```

### Kafka Event Schemas

```json
// delivery.rider_accepted
{
  "deliveryId": "uuid",
  "orderId": "uuid",
  "riderId": "uuid",
  "riderName": "Rajesh Kumar",
  "riderPhone": "+91-98765-43210",
  "riderRating": 4.8,
  "riderLocation": {
    "latitude": 12.9716,
    "longitude": 77.5946
  },
  "estimatedArrivalAtRestaurant": "2025-11-09T12:42:00Z",
  "timestamp": "2025-11-09T12:37:00Z"
}
```

```json
// delivery.picked_up
{
  "deliveryId": "uuid",
  "orderId": "uuid",
  "riderId": "uuid",
  "pickupTimestamp": "2025-11-09T12:47:00Z",
  "restaurantWaitTime": 5,
  "estimatedDeliveryTime": "2025-11-09T13:05:00Z"
}
```

```json
// delivery.delivered
{
  "deliveryId": "uuid",
  "orderId": "uuid",
  "riderId": "uuid",
  "deliveryTimestamp": "2025-11-09T13:05:00Z",
  "totalDeliveryTime": 18,
  "codAmount": 460.00,
  "customerSignature": "base64_encoded_signature"
}
```

---

## Implementation Guide

### Stateless4j Configuration

```java
@Configuration
public class DeliveryFSMConfig {
    
    @Bean
    public StateMachineConfig<DeliveryState, DeliveryTrigger> deliveryFSMConfig() {
        StateMachineConfig<DeliveryState, DeliveryTrigger> config = 
            new StateMachineConfig<>();
        
        config.configure(DeliveryState.PENDING)
            .permit(DeliveryTrigger.FIND_RIDERS, DeliveryState.SEARCHING_RIDER);
        
        config.configure(DeliveryState.SEARCHING_RIDER)
            .permit(DeliveryTrigger.ASSIGN_RIDER, DeliveryState.RIDER_ASSIGNED)
            .permit(DeliveryTrigger.NO_RIDERS_AVAILABLE, DeliveryState.FAILED);
        
        config.configure(DeliveryState.RIDER_ASSIGNED)
            .permit(DeliveryTrigger.RIDER_ACCEPT, DeliveryState.RIDER_ACCEPTED)
            .permit(DeliveryTrigger.RIDER_REJECT, DeliveryState.SEARCHING_RIDER);
        
        config.configure(DeliveryState.RIDER_ACCEPTED)
            .permit(DeliveryTrigger.REACH_RESTAURANT, DeliveryState.AT_RESTAURANT)
            .permit(DeliveryTrigger.FAIL_DELIVERY, DeliveryState.FAILED);
        
        config.configure(DeliveryState.AT_RESTAURANT)
            .permit(DeliveryTrigger.PICKUP_ORDER, DeliveryState.PICKED_UP)
            .permit(DeliveryTrigger.FAIL_DELIVERY, DeliveryState.FAILED);
        
        config.configure(DeliveryState.PICKED_UP)
            .permit(DeliveryTrigger.START_DELIVERY, DeliveryState.OUT_FOR_DELIVERY);
        
        config.configure(DeliveryState.OUT_FOR_DELIVERY)
            .permit(DeliveryTrigger.DELIVER_ORDER, DeliveryState.DELIVERED)
            .permit(DeliveryTrigger.FAIL_DELIVERY, DeliveryState.FAILED);
        
        return config;
    }
}
```

---

## Next Steps

1. Review this Delivery FSM design
2. Review [Customer Status Design](./04_CUSTOMER_STATUS_DESIGN.md)
3. Review [Smart Assignment Algorithm](./06_SMART_ASSIGNMENT_ALGORITHM.md)
4. Create implementation stories
