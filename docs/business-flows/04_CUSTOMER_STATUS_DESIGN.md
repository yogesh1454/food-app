# Customer-Facing Status Design

**Document Version:** 1.0  
**Last Updated:** November 9, 2025  
**Epic:** Epic-4 - Order & Delivery Management  
**Status:** Draft

---

## Table of Contents

1. [Overview](#overview)
2. [Customer Status States](#customer-status-states)
3. [Status Mapping Logic](#status-mapping-logic)
4. [UI Design Patterns](#ui-design-patterns)
5. [Notification Strategy](#notification-strategy)
6. [Multi-Restaurant Status Display](#multi-restaurant-status-display)
7. [API Design](#api-design)
8. [Implementation Guide](#implementation-guide)

---

## Overview

### Problem Statement

**Internal FSM Complexity:**
- Order FSM: 13 states
- Delivery FSM: 9 states
- Total: 22 internal states

**Customer Confusion:**
```
❌ BAD: Show raw FSM states
"Your order is in PENDING_ACCEPTANCE state"
"Delivery is in SEARCHING_RIDER state"

Customer thinks: "What does that even mean?"
```

### Solution: Status Abstraction Layer

```
┌──────────────────────────────────────────────────────────────┐
│              STATUS ABSTRACTION ARCHITECTURE                 │
└──────────────────────────────────────────────────────────────┘

┌──────────────────┐         ┌──────────────────┐
│   Order FSM      │         │  Delivery FSM    │
│  (13 states)     │         │   (9 states)     │
└────────┬─────────┘         └────────┬─────────┘
         │                            │
         │                            │
         ▼                            ▼
    ┌────────────────────────────────────────┐
    │   STATUS MAPPER SERVICE                │
    │   (Aggregates both FSMs)               │
    └────────────────┬───────────────────────┘
                     │
                     ▼
         ┌───────────────────────┐
         │ CUSTOMER-FACING STATUS│
         │    (8 states)         │
         └───────────────────────┘
                     │
                     ▼
              ┌──────────────┐
              │  Customer UI │
              └──────────────┘
```

**Benefits:**
- ✅ Reduced confusion (8 vs 22 states)
- ✅ Consistent experience
- ✅ Fewer notifications (5-7 vs 20+)
- ✅ Easier localization

---

## Customer Status States

### 8 Customer-Facing States

```
┌─────────────────────────────────────────────────────────────┐
│           CUSTOMER STATUS JOURNEY (8 STATES)                │
└─────────────────────────────────────────────────────────────┘

1. 🔄 ORDER_PLACED
   "Your order has been placed"
   "We're confirming with the restaurant"

2. ✅ ORDER_CONFIRMED
   "Restaurant is preparing your food"
   "Estimated prep time: 15-20 minutes"

3. 👨‍🍳 PREPARING
   "Your food is being prepared"
   "Almost ready for pickup"

4. 🏍️ RIDER_ASSIGNED
   "Delivery partner assigned"
   "Rajesh is on the way to the restaurant"

5. 📦 READY_FOR_PICKUP
   "Food is ready, waiting for pickup"
   "Delivery partner arriving soon"

6. 🚚 OUT_FOR_DELIVERY
   "Your order is on the way"
   "Track your delivery partner"

7. ✅ DELIVERED
   "Your order has been delivered"
   "Enjoy your meal!"

8. ❌ CANCELLED
   "Your order was cancelled"
   "Refund will be processed in 3-5 days"
```

### State Definitions

| Customer Status | Description | Typical Duration | Can Cancel? |
|----------------|-------------|------------------|-------------|
| **ORDER_PLACED** | Order created, awaiting confirmation | 1-2 min | Yes (free) |
| **ORDER_CONFIRMED** | Restaurant accepted | < 1 min | Yes (₹20 fee) |
| **PREPARING** | Food being prepared | 15-20 min | No |
| **RIDER_ASSIGNED** | Rider assigned, heading to restaurant | 5-10 min | No |
| **READY_FOR_PICKUP** | Food ready, rider arriving | 2-5 min | No |
| **OUT_FOR_DELIVERY** | Rider delivering to customer | 10-15 min | No |
| **DELIVERED** | Successfully delivered | - | No |
| **CANCELLED** | Order cancelled | - | No |

---

## Status Mapping Logic

### Mapping Table: FSM States → Customer Status

| Order FSM State | Delivery FSM State | Customer Status | Customer Message |
|-----------------|-------------------|-----------------|------------------|
| CREATED | null | ORDER_PLACED | "Placing your order..." |
| VALIDATED | null | ORDER_PLACED | "Confirming with restaurant..." |
| PAYMENT_CONFIRMED | null | ORDER_PLACED | "Payment confirmed, notifying restaurant..." |
| PENDING_ACCEPTANCE | null | ORDER_PLACED | "Waiting for restaurant confirmation..." |
| ACCEPTED | null | ORDER_CONFIRMED | "Restaurant accepted! Preparing your food..." |
| PREPARING | null | PREPARING | "Your food is being prepared" |
| PREPARING | PENDING | PREPARING | "Food being prepared, finding delivery partner..." |
| PREPARING | SEARCHING_RIDER | PREPARING | "Food being prepared, finding delivery partner..." |
| PREPARING | RIDER_ASSIGNED | RIDER_ASSIGNED | "Delivery partner assigned" |
| PREPARING | RIDER_ACCEPTED | RIDER_ASSIGNED | "Delivery partner is on the way to restaurant" |
| READY_FOR_PICKUP | RIDER_ACCEPTED | RIDER_ASSIGNED | "Food ready, delivery partner arriving" |
| READY_FOR_PICKUP | AT_RESTAURANT | READY_FOR_PICKUP | "Delivery partner is at the restaurant" |
| ASSIGNED_TO_RIDER | AT_RESTAURANT | READY_FOR_PICKUP | "Waiting for pickup" |
| PICKED_UP | PICKED_UP | OUT_FOR_DELIVERY | "Your order is on the way!" |
| PICKED_UP | OUT_FOR_DELIVERY | OUT_FOR_DELIVERY | "Arriving in X minutes" |
| DELIVERED | DELIVERED | DELIVERED | "Order delivered. Enjoy your meal!" |
| CANCELLED | any | CANCELLED | "Order cancelled" |
| REJECTED | any | CANCELLED | "Restaurant couldn't accept your order" |

### Java Implementation

```java
@Service
public class CustomerStatusMapper {
    
    public CustomerOrderStatus mapToCustomerStatus(
        OrderState orderState, 
        DeliveryState deliveryState,
        OrderContext context
    ) {
        // Priority 1: Terminal states
        if (orderState == OrderState.CANCELLED || 
            orderState == OrderState.REJECTED) {
            return CustomerOrderStatus.CANCELLED;
        }
        if (orderState == OrderState.DELIVERED) {
            return CustomerOrderStatus.DELIVERED;
        }
        
        // Priority 2: Delivery in progress
        if (deliveryState == DeliveryState.OUT_FOR_DELIVERY || 
            deliveryState == DeliveryState.PICKED_UP) {
            return CustomerOrderStatus.OUT_FOR_DELIVERY;
        }
        
        // Priority 3: Rider at restaurant, food ready
        if (orderState == OrderState.READY_FOR_PICKUP && 
            deliveryState == DeliveryState.AT_RESTAURANT) {
            return CustomerOrderStatus.READY_FOR_PICKUP;
        }
        
        // Priority 4: Rider assigned during preparation
        if (orderState == OrderState.PREPARING && 
            (deliveryState == DeliveryState.RIDER_ASSIGNED || 
             deliveryState == DeliveryState.RIDER_ACCEPTED)) {
            return CustomerOrderStatus.RIDER_ASSIGNED;
        }
        
        // Priority 5: Food being prepared (no rider yet)
        if (orderState == OrderState.PREPARING) {
            return CustomerOrderStatus.PREPARING;
        }
        
        // Priority 6: Restaurant accepted
        if (orderState == OrderState.ACCEPTED) {
            return CustomerOrderStatus.ORDER_CONFIRMED;
        }
        
        // Priority 7: Order placed, waiting for acceptance
        if (orderState == OrderState.CREATED || 
            orderState == OrderState.VALIDATED || 
            orderState == OrderState.PAYMENT_CONFIRMED || 
            orderState == OrderState.PENDING_ACCEPTANCE) {
            return CustomerOrderStatus.ORDER_PLACED;
        }
        
        return CustomerOrderStatus.ORDER_PLACED;
    }
    
    public StatusMessage getStatusMessage(
        CustomerOrderStatus status,
        OrderContext context
    ) {
        switch (status) {
            case ORDER_PLACED:
                return new StatusMessage(
                    "Order Placed",
                    "Waiting for restaurant confirmation",
                    "Usually takes less than 2 minutes"
                );
                
            case ORDER_CONFIRMED:
                return new StatusMessage(
                    "Order Confirmed!",
                    context.getRestaurantName() + " is preparing your food",
                    "Estimated prep time: " + context.getEstimatedPrepTime() + " min"
                );
                
            case PREPARING:
                int timeRemaining = context.getEstimatedPrepTime() - 
                                  context.getElapsedPrepTime();
                return new StatusMessage(
                    "Preparing Your Food",
                    "Almost ready for pickup",
                    "About " + timeRemaining + " minutes remaining"
                );
                
            case RIDER_ASSIGNED:
                return new StatusMessage(
                    "Delivery Partner Assigned",
                    context.getRiderName() + " is heading to the restaurant",
                    "Food will be ready soon"
                );
                
            case READY_FOR_PICKUP:
                return new StatusMessage(
                    "Ready for Pickup",
                    "Food is ready, delivery partner arriving",
                    "Pickup in progress"
                );
                
            case OUT_FOR_DELIVERY:
                if (context.getRiderLocation() != null) {
                    double distance = calculateDistance(
                        context.getRiderLocation(), 
                        context.getCustomerLocation()
                    );
                    int eta = calculateETA(distance);
                    
                    return new StatusMessage(
                        "On the Way!",
                        context.getRiderName() + " is " + 
                            String.format("%.1f", distance) + " km away",
                        "Arriving in " + eta + " minutes"
                    );
                }
                return new StatusMessage(
                    "Out for Delivery",
                    "Your delivery partner is on the way",
                    "Track live location"
                );
                
            case DELIVERED:
                return new StatusMessage(
                    "Delivered!",
                    "Your order has been delivered",
                    "Enjoy your meal! 🎉"
                );
                
            case CANCELLED:
                String reason = context.getCancellationReason();
                String refundMessage = context.isPaid() 
                    ? "Refund will be processed in 3-5 days"
                    : "";
                return new StatusMessage(
                    "Order Cancelled",
                    reason,
                    refundMessage
                );
        }
    }
}
```

---

## UI Design Patterns

> **⚠️ Important: Backend/Frontend Responsibility**
> 
> The patterns shown below are **reference examples for the frontend team**. 
> 
> **Backend Responsibility:**
> - Provide structured data via API (status, timeline, progress, delivery info)
> - Calculate progress percentage and current step
> - Aggregate multi-restaurant statuses
> 
> **Frontend Responsibility:**
> - Choose and implement UI pattern (timeline, progress bar, card, etc.)
> - Handle responsive design and animations
> - Localization and formatting
> 
> The backend API response includes all necessary data fields to support any UI pattern the frontend chooses to implement.

### Pattern 1: Timeline View

```
┌─────────────────────────────────────────────────────────────┐
│                     ORDER #12345                            │
│                                                             │
│  ✅ Order Placed                              12:30 PM      │
│      Payment confirmed                                      │
│                                                             │
│  ✅ Order Confirmed                           12:32 PM      │
│      Restaurant accepted your order                         │
│                                                             │
│  ✅ Preparing                                 12:35 PM      │
│      Your food is being prepared                            │
│      Estimated time: 15 minutes                             │
│                                                             │
│  🔄 Rider Assigned                            12:40 PM      │
│      Rajesh Kumar is on the way to restaurant               │
│      📍 5 minutes away from restaurant                      │
│                                                             │
│  ⏳ Ready for Pickup                          Expected 12:50│
│      Food will be ready soon                                │
│                                                             │
│  ⏳ Out for Delivery                          Expected 12:55│
│                                                             │
│  ⏳ Delivered                                 Expected 1:10 │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Pattern 2: Progress Bar View

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│   ●━━━━●━━━━●━━━━●━━━━○━━━━○━━━━○                          │
│   │    │    │    │    │    │                               │
│ Placed Conf Prep Rider Ready Delivery Delivered            │
│                    Assigned                                 │
│                                                             │
│   Current Status: Rider Assigned                           │
│   Rajesh is heading to the restaurant                      │
│   Food will be ready in 10 minutes                         │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Pattern 3: Card View with Dual Progress

```
┌─────────────────────────────────────────────────────────────┐
│  🏍️ RIDER ASSIGNED                                         │
│                                                             │
│  Your delivery partner is on the way to the restaurant     │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │  👨‍🍳 Food Preparation                                  │ │
│  │  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │ │
│  │  ████████████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  │ │
│  │  60% complete • 10 minutes remaining                   │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │  🏍️ Delivery Partner                                  │ │
│  │  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │ │
│  │  [Map showing rider location]                         │ │
│  │                                                        │ │
│  │  📍 Rajesh Kumar                                      │ │
│  │  ⭐ 4.8 rating • 2.5 km from restaurant               │ │
│  │  🕐 Arriving at restaurant in 5 minutes               │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
│  Estimated delivery: 1:05 PM                               │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Notification Strategy

### When to Notify Customer

```java
@Service
public class CustomerNotificationService {
    
    @EventListener
    public void onOrderStateChange(OrderStateChangedEvent event) {
        CustomerOrderStatus customerStatus = 
            statusMapper.mapToCustomerStatus(
                event.getNewState(), 
                event.getDeliveryState()
            );
        
        // Only notify on customer-facing status changes
        CustomerOrderStatus previousCustomerStatus = 
            statusMapper.mapToCustomerStatus(
                event.getOldState(), 
                event.getPreviousDeliveryState()
            );
        
        if (customerStatus != previousCustomerStatus) {
            sendPushNotification(event.getOrderId(), customerStatus);
        }
    }
    
    private void sendPushNotification(
        UUID orderId, 
        CustomerOrderStatus status
    ) {
        switch (status) {
            case ORDER_PLACED:
                // Don't notify - too early
                break;
                
            case ORDER_CONFIRMED:
                send("✅ Order Confirmed!", 
                     "Restaurant is preparing your food");
                break;
                
            case PREPARING:
                send("👨‍🍳 Food is being prepared", 
                     "Estimated time: 15 minutes");
                break;
                
            case RIDER_ASSIGNED:
                send("🏍️ Delivery partner assigned", 
                     "Rajesh is on the way to the restaurant");
                break;
                
            case READY_FOR_PICKUP:
                send("📦 Food is ready", 
                     "Delivery partner is picking up your order");
                break;
                
            case OUT_FOR_DELIVERY:
                send("🚚 Your order is on the way!", 
                     "Track your delivery partner");
                break;
                
            case DELIVERED:
                send("✅ Order Delivered!", 
                     "Enjoy your meal! Rate your experience");
                break;
                
            case CANCELLED:
                send("❌ Order Cancelled", 
                     "Refund will be processed in 3-5 days");
                break;
        }
    }
}
```

### Notification Frequency

| Customer Status | Notification Triggers | Total Notifications |
|----------------|----------------------|---------------------|
| ORDER_PLACED | Order created | 0 (silent) |
| ORDER_CONFIRMED | Restaurant accepted | 1 |
| PREPARING | Food preparation started | 1 |
| RIDER_ASSIGNED | Rider assigned | 1 |
| READY_FOR_PICKUP | Food ready | 1 (optional) |
| OUT_FOR_DELIVERY | Rider picked up | 1 |
| OUT_FOR_DELIVERY | Rider nearby (< 500m) | 1 |
| DELIVERED | Order delivered | 1 |
| CANCELLED | Order cancelled | 1 |

**Total: 5-7 notifications per order** (not 20+ from raw FSM states)

---

## Multi-Restaurant Status Display

### Aggregate Status Calculation

```java
public CustomerOrderStatus calculateAggregateStatus(
    List<SubOrder> subOrders,
    Delivery delivery
) {
    // If delivery in progress, show delivery status
    if (delivery != null && 
        delivery.getState() == DeliveryState.OUT_FOR_DELIVERY) {
        return CustomerOrderStatus.OUT_FOR_DELIVERY;
    }
    
    // Check if all sub-orders are delivered
    if (subOrders.stream().allMatch(
        s -> s.getState() == OrderState.DELIVERED)) {
        return CustomerOrderStatus.DELIVERED;
    }
    
    // Check if any sub-order is cancelled
    if (subOrders.stream().anyMatch(
        s -> s.getState() == OrderState.CANCELLED)) {
        // If all cancelled, show cancelled
        if (subOrders.stream().allMatch(
            s -> s.getState() == OrderState.CANCELLED)) {
            return CustomerOrderStatus.CANCELLED;
        }
        // Otherwise, still show progress of active orders
    }
    
    // Check if all sub-orders are ready
    if (subOrders.stream().allMatch(s -> 
        s.getState() == OrderState.READY_FOR_PICKUP || 
        s.getState() == OrderState.PICKED_UP
    )) {
        return CustomerOrderStatus.OUT_FOR_DELIVERY;
    }
    
    // Check if any sub-order is preparing
    if (subOrders.stream().anyMatch(
        s -> s.getState() == OrderState.PREPARING)) {
        return CustomerOrderStatus.PREPARING;
    }
    
    // Check if all sub-orders are confirmed
    if (subOrders.stream().allMatch(s -> 
        s.getState() == OrderState.ACCEPTED || 
        s.getState() == OrderState.PREPARING
    )) {
        return CustomerOrderStatus.ORDER_CONFIRMED;
    }
    
    // Default: order placed
    return CustomerOrderStatus.ORDER_PLACED;
}
```

### UI for Multi-Restaurant Orders

```
┌─────────────────────────────────────────────────────────────┐
│  ORDER #12345                                               │
│                                                             │
│  🔄 Overall Status: PREPARING                               │
│  Your food is being prepared at multiple restaurants        │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  ✅ Chai Express                                    │   │
│  │     👨‍🍳 Preparing (5 min remaining)                  │   │
│  │     • 2x Masala Chai                                │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  ✅ Pizza Corner                                    │   │
│  │     📦 Ready for pickup                             │   │
│  │     • 1x Margherita Pizza                           │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  ✅ Spice House                                     │   │
│  │     ⏳ Confirmed (starting soon)                    │   │
│  │     • 1x Biryani                                    │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  Estimated delivery: 1:15 PM - 1:25 PM                     │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## API Design

### Customer Status API

```
GET /api/v1/orders/{orderId}/status
```

**Response:**

```json
{
  "orderId": "12345",
  "customerStatus": "OUT_FOR_DELIVERY",
  "statusMessage": {
    "title": "On the Way!",
    "subtitle": "Rajesh Kumar is 2.5 km away",
    "description": "Arriving in 8 minutes"
  },
  "progress": {
    "currentStep": 6,
    "totalSteps": 8,
    "percentage": 75
  },
  "timeline": [
    {
      "status": "ORDER_PLACED",
      "timestamp": "2025-11-09T12:30:00Z",
      "message": "Order placed successfully"
    },
    {
      "status": "ORDER_CONFIRMED",
      "timestamp": "2025-11-09T12:32:00Z",
      "message": "Restaurant accepted your order"
    },
    {
      "status": "PREPARING",
      "timestamp": "2025-11-09T12:35:00Z",
      "message": "Food is being prepared"
    },
    {
      "status": "RIDER_ASSIGNED",
      "timestamp": "2025-11-09T12:40:00Z",
      "message": "Delivery partner assigned"
    },
    {
      "status": "OUT_FOR_DELIVERY",
      "timestamp": "2025-11-09T12:50:00Z",
      "message": "Your order is on the way",
      "current": true
    }
  ],
  "delivery": {
    "riderName": "Rajesh Kumar",
    "riderPhone": "+91-98765-43210",
    "riderRating": 4.8,
    "currentLocation": {
      "latitude": 12.9716,
      "longitude": 77.5946
    },
    "estimatedArrival": "2025-11-09T13:05:00Z",
    "distanceKm": 2.5
  },
  "canCancel": false,
  "canTrack": true,
  "canContact": true,
  
  "internal": {
    "orderFsmState": "PICKED_UP",
    "deliveryFsmState": "OUT_FOR_DELIVERY"
  }
}
```

---

## Implementation Guide

### Step 1: Create CustomerOrderStatus Enum

```java
public enum CustomerOrderStatus {
    ORDER_PLACED,
    ORDER_CONFIRMED,
    PREPARING,
    RIDER_ASSIGNED,
    READY_FOR_PICKUP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}
```

### Step 2: Implement StatusMapper Service

```java
@Service
public class CustomerStatusMapper {
    
    public CustomerOrderStatus mapToCustomerStatus(
        OrderState orderState, 
        DeliveryState deliveryState
    ) {
        // Implementation as shown above
    }
    
    public StatusMessage getStatusMessage(
        CustomerOrderStatus status,
        OrderContext context
    ) {
        // Implementation as shown above
    }
}
```

### Step 3: Create Customer Status API

```java
@RestController
@RequestMapping("/api/v1/orders")
public class CustomerOrderStatusController {
    
    @Autowired
    private CustomerStatusMapper statusMapper;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private DeliveryService deliveryService;
    
    @GetMapping("/{orderId}/status")
    public ResponseEntity<CustomerOrderStatusResponse> getOrderStatus(
        @PathVariable UUID orderId
    ) {
        Order order = orderService.getOrder(orderId);
        Delivery delivery = deliveryService.getDeliveryByOrderId(orderId);
        
        CustomerOrderStatus status = statusMapper.mapToCustomerStatus(
            order.getState(),
            delivery != null ? delivery.getState() : null
        );
        
        OrderContext context = buildContext(order, delivery);
        StatusMessage message = statusMapper.getStatusMessage(status, context);
        
        return ResponseEntity.ok(
            CustomerOrderStatusResponse.builder()
                .orderId(orderId)
                .customerStatus(status)
                .statusMessage(message)
                .timeline(buildTimeline(order))
                .delivery(buildDeliveryInfo(delivery))
                .build()
        );
    }
}
```

### Step 4: Add Event Listener for Status Changes

```java
@Component
public class CustomerStatusEventListener {
    
    @Autowired
    private CustomerStatusMapper statusMapper;
    
    @Autowired
    private CustomerNotificationService notificationService;
    
    @EventListener
    public void onOrderStateChange(OrderStateChangedEvent event) {
        CustomerOrderStatus newStatus = statusMapper.mapToCustomerStatus(
            event.getNewState(),
            event.getDeliveryState()
        );
        
        CustomerOrderStatus oldStatus = statusMapper.mapToCustomerStatus(
            event.getOldState(),
            event.getPreviousDeliveryState()
        );
        
        // Only notify if customer-facing status changed
        if (newStatus != oldStatus) {
            notificationService.sendPushNotification(
                event.getOrderId(),
                newStatus
            );
        }
    }
}
```

---

## Next Steps

1. Review this Customer Status Design
2. Review [Multi-Restaurant Design](./05_MULTI_RESTAURANT_DESIGN.md)
3. Review [Smart Assignment Algorithm](./06_SMART_ASSIGNMENT_ALGORITHM.md)
4. Create implementation stories
