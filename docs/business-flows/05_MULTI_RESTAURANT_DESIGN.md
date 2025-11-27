# Multi-Restaurant Order Handling Design

**Document Version:** 1.0  
**Last Updated:** November 9, 2025  
**Epic:** Epic-4 - Order & Delivery Management  
**Status:** Draft

---

## Table of Contents

1. [Overview](#overview)
2. [Parent-Child Order Model](#parent-child-order-model)
3. [State Aggregation Logic](#state-aggregation-logic)
4. [Delivery Strategies](#delivery-strategies)
5. [Payment Handling](#payment-handling)
6. [Data Model](#data-model)
7. [Implementation Guide](#implementation-guide)

---

## Overview

### Business Scenario

**Customer wants to order from multiple restaurants:**
- 2x Samosa from "Chai Express" (₹30)
- 1x Pizza from "Pizza Corner" (₹250)
- 1x Biryani from "Spice House" (₹180)

**Total:** ₹460 + delivery charges

### Design Challenge

How to handle:
- Single payment transaction
- Independent restaurant workflows
- Flexible delivery strategies
- Unified customer experience

---

## Parent-Child Order Model

### Structure

```
Customer Order (order_id: 12345)
├── Payment: ₹530 (items + delivery + fees)
├── Status: PREPARING (aggregated)
│
├── Sub-Order 1: Chai Express (sub_order_id: 12345-1)
│   ├── State: PREPARING
│   ├── Items: 2x Samosa (₹30)
│   └── Restaurant: Chai Express
│
├── Sub-Order 2: Pizza Corner (sub_order_id: 12345-2)
│   ├── State: READY_FOR_PICKUP
│   ├── Items: 1x Pizza (₹250)
│   └── Restaurant: Pizza Corner
│
└── Sub-Order 3: Spice House (sub_order_id: 12345-3)
    ├── State: ACCEPTED
    ├── Items: 1x Biryani (₹180)
    └── Restaurant: Spice House
```

### Benefits

| Benefit | Description |
|---------|-------------|
| **Single Payment** | Customer pays once for entire order |
| **Unified View** | Single order ID for customer tracking |
| **Independent Workflows** | Each restaurant manages their sub-order |
| **Flexible Delivery** | Support multiple delivery strategies |
| **Partial Cancellation** | Can cancel individual sub-orders |

---

## State Aggregation Logic

### Parent Order Status Calculation

```java
public OrderState calculateParentState(List<SubOrder> subOrders) {
    // Terminal states
    if (subOrders.stream().allMatch(s -> s.getState() == DELIVERED)) {
        return DELIVERED;
    }
    if (subOrders.stream().allMatch(s -> s.getState() == CANCELLED)) {
        return CANCELLED;
    }
    if (subOrders.stream().anyMatch(s -> s.getState() == CANCELLED)) {
        return PARTIALLY_CANCELLED;
    }
    
    // Active states
    if (subOrders.stream().allMatch(s -> s.getState() == READY_FOR_PICKUP)) {
        return READY_FOR_PICKUP; // Trigger delivery assignment
    }
    if (subOrders.stream().anyMatch(s -> s.getState() == PREPARING)) {
        return PREPARING;
    }
    if (subOrders.stream().anyMatch(s -> s.getState() == ACCEPTED)) {
        return ACCEPTED;
    }
    if (subOrders.stream().anyMatch(s -> s.getState() == PENDING_ACCEPTANCE)) {
        return PENDING_ACCEPTANCE;
    }
    
    return CREATED;
}
```

### State Aggregation Matrix

| Sub-Order States | Parent Order Status | Customer Status |
|------------------|---------------------|-----------------|
| All PENDING_ACCEPTANCE | PENDING_ACCEPTANCE | ORDER_PLACED |
| Any ACCEPTED | ACCEPTED | ORDER_CONFIRMED |
| All ACCEPTED | ACCEPTED | ORDER_CONFIRMED |
| Any PREPARING | PREPARING | PREPARING |
| All PREPARING | PREPARING | PREPARING |
| All READY_FOR_PICKUP | READY_FOR_PICKUP | READY_FOR_PICKUP |
| All PICKED_UP | PICKED_UP | OUT_FOR_DELIVERY |
| All DELIVERED | DELIVERED | DELIVERED |
| Any CANCELLED | PARTIALLY_CANCELLED | PREPARING (active ones) |
| All CANCELLED | CANCELLED | CANCELLED |

---

## Delivery Strategies

### Strategy 1: Single Rider, Sequential Pickup

**Description:** One rider picks up from all restaurants sequentially.

```
Rider Route:
1. Go to Chai Express → Pickup Samosas
2. Go to Pizza Corner → Pickup Pizza
3. Go to Spice House → Pickup Biryani
4. Deliver all to customer

Timeline:
12:30 PM - Order placed
12:50 PM - All food ready
12:55 PM - Rider assigned
1:00 PM  - Pickup from Restaurant 1
1:05 PM  - Pickup from Restaurant 2
1:10 PM  - Pickup from Restaurant 3
1:25 PM  - Delivered to customer

Total time: 55 minutes
```

**Pros:**
- ✅ Cost-efficient (single rider)
- ✅ Simple coordination

**Cons:**
- ❌ Longer delivery time
- ❌ Food quality risk (first pickup gets cold)
- ❌ Customer wait time

**When to Use:**
- Restaurants are close (< 1 km apart)
- Customer is price-sensitive
- Non-peak hours

---

### Strategy 2: Multiple Riders, Parallel Delivery

**Description:** Separate rider for each restaurant.

```
Rider 1: Chai Express → Customer (12:50 PM - 1:05 PM)
Rider 2: Pizza Corner → Customer (12:55 PM - 1:10 PM)
Rider 3: Spice House → Customer (1:00 PM - 1:15 PM)

Timeline:
12:30 PM - Order placed
12:50 PM - Restaurant 1 ready, Rider 1 assigned
12:55 PM - Restaurant 2 ready, Rider 2 assigned
1:00 PM  - Restaurant 3 ready, Rider 3 assigned
1:05 PM  - First delivery (Chai Express)
1:10 PM  - Second delivery (Pizza Corner)
1:15 PM  - Third delivery (Spice House)

Total time: 45 minutes (staggered deliveries)
```

**Pros:**
- ✅ Fastest delivery
- ✅ Hot food (immediate pickup)
- ✅ Best food quality

**Cons:**
- ❌ Higher cost (3x delivery fee)
- ❌ Coordination complexity
- ❌ Customer receives items at different times

**When to Use:**
- Premium customers
- Peak hours (rider availability)
- Restaurants far apart (> 3 km)

---

### Strategy 3: Intelligent Batching (RECOMMENDED)

**Description:** Optimize based on proximity, timing, and rider availability.

```
Algorithm:
1. Group restaurants by proximity (< 2 km)
2. Align by preparation time (±10 min)
3. Optimize route for minimal distance

Example:
Rider 1: Chai Express + Pizza Corner → Customer
Rider 2: Spice House → Customer

Timeline:
12:30 PM - Order placed
12:50 PM - Chai Express ready
12:55 PM - Pizza Corner ready
12:55 PM - Rider 1 assigned (batched pickup)
1:00 PM  - Rider 1 picks up from Chai Express
1:05 PM  - Rider 1 picks up from Pizza Corner
1:00 PM  - Spice House ready, Rider 2 assigned
1:05 PM  - Rider 2 picks up from Spice House
1:15 PM  - Both riders deliver

Total time: 45 minutes
Cost: 2x delivery fee (vs 3x)
```

**Pros:**
- ✅ Balanced cost and speed
- ✅ Good food quality
- ✅ Efficient rider utilization

**Cons:**
- ⚠️ Requires sophisticated routing algorithm
- ⚠️ Depends on prep time accuracy

**When to Use:**
- Default strategy for most orders
- Restaurants moderately close (1-3 km)
- Normal delivery hours

---

### Batching Algorithm

```java
@Service
public class DeliveryBatchingService {
    
    public List<DeliveryBatch> createBatches(
        List<SubOrder> subOrders,
        Location customerLocation
    ) {
        List<DeliveryBatch> batches = new ArrayList<>();
        List<SubOrder> remaining = new ArrayList<>(subOrders);
        
        while (!remaining.isEmpty()) {
            SubOrder anchor = remaining.remove(0);
            DeliveryBatch batch = new DeliveryBatch();
            batch.addSubOrder(anchor);
            
            // Find nearby restaurants with similar ready time
            Iterator<SubOrder> iterator = remaining.iterator();
            while (iterator.hasNext()) {
                SubOrder candidate = iterator.next();
                
                // Check proximity (< 2 km)
                double distance = calculateDistance(
                    anchor.getRestaurant().getLocation(),
                    candidate.getRestaurant().getLocation()
                );
                
                // Check time alignment (±10 min)
                int timeDiff = Math.abs(
                    anchor.getEstimatedReadyTime() - 
                    candidate.getEstimatedReadyTime()
                );
                
                // Check max batch size
                if (distance < 2.0 && 
                    timeDiff < 10 && 
                    batch.size() < 3) {
                    batch.addSubOrder(candidate);
                    iterator.remove();
                }
            }
            
            batches.add(batch);
        }
        
        return batches;
    }
    
    public Route optimizeRoute(
        DeliveryBatch batch,
        Location customerLocation
    ) {
        // Use TSP (Traveling Salesman Problem) solver
        // to find optimal pickup sequence
        List<Location> pickupLocations = batch.getRestaurantLocations();
        
        // Simple greedy approach (can be improved with better algorithms)
        Route route = new Route();
        Location current = getCurrentRiderLocation();
        
        while (!pickupLocations.isEmpty()) {
            Location nearest = findNearest(current, pickupLocations);
            route.addStop(nearest);
            pickupLocations.remove(nearest);
            current = nearest;
        }
        
        route.addStop(customerLocation); // Final destination
        return route;
    }
}
```

---

## Payment Handling

> **📌 Scope Clarification**
> 
> This section covers **post-order payment operations** specific to multi-restaurant orders:
> - **Payment Distribution:** How to split collected payment among restaurants, riders, and platform
> - **Partial Refunds:** How to handle refunds when some sub-orders are cancelled
> 
> **Not covered here:**
> - **Initial Payment Collection:** Handled in Order FSM during `PAYMENT_CONFIRMED` state (see [Order FSM Design](./02_ORDER_FSM_DESIGN.md))
> - **Payment Gateway Integration:** Covered in separate payment service documentation
> 
> Multi-restaurant orders require special handling because:
> - Single payment needs to be distributed to multiple restaurants
> - Partial cancellations require complex refund calculations
> - Delivery fees may need adjustment based on active sub-orders

### Payment Breakdown

```
Order Total Calculation:
─────────────────────────

Item Total:
  Chai Express: ₹30
  Pizza Corner: ₹250
  Spice House: ₹180
  Subtotal: ₹460

Delivery Charges:
  Base delivery: ₹20
  Additional restaurant fee: ₹10 × 2 = ₹20
  Delivery subtotal: ₹40

Platform Fee: ₹5

GST (5%): ₹25

Grand Total: ₹530
```

### Payment Distribution

```java
public class PaymentDistributionService {
    
    public void distributePayment(Order parentOrder) {
        BigDecimal totalAmount = parentOrder.getTotalAmount();
        BigDecimal platformFee = parentOrder.getPlatformFee();
        BigDecimal deliveryCharges = parentOrder.getDeliveryCharges();
        BigDecimal gst = parentOrder.getGst();
        
        // Calculate restaurant payouts
        for (SubOrder subOrder : parentOrder.getSubOrders()) {
            BigDecimal itemTotal = subOrder.getItemTotal();
            BigDecimal commission = itemTotal.multiply(
                subOrder.getRestaurant().getCommissionRate()
            );
            BigDecimal restaurantPayout = itemTotal.subtract(commission);
            
            createPayout(
                subOrder.getRestaurant(),
                restaurantPayout,
                "Order: " + parentOrder.getOrderId()
            );
        }
        
        // Calculate rider payouts
        for (Delivery delivery : parentOrder.getDeliveries()) {
            BigDecimal riderFee = calculateRiderFee(delivery);
            
            createPayout(
                delivery.getRider(),
                riderFee,
                "Delivery: " + delivery.getDeliveryId()
            );
        }
        
        // Platform revenue
        BigDecimal platformRevenue = platformFee.add(
            calculateTotalCommission(parentOrder)
        );
    }
    
    private BigDecimal calculateRiderFee(Delivery delivery) {
        BigDecimal baseFee = new BigDecimal("30");
        BigDecimal distanceFee = delivery.getDistanceKm()
            .multiply(new BigDecimal("5")); // ₹5 per km
        BigDecimal waitTimeFee = calculateWaitTimeFee(delivery);
        
        return baseFee.add(distanceFee).add(waitTimeFee);
    }
}
```

### Refund Handling

```java
public class RefundService {
    
    public void processRefund(Order parentOrder, RefundReason reason) {
        switch (reason) {
            case FULL_CANCELLATION:
                // All sub-orders cancelled
                refundFullAmount(parentOrder);
                break;
                
            case PARTIAL_CANCELLATION:
                // Some sub-orders cancelled
                refundPartialAmount(parentOrder);
                break;
                
            case RESTAURANT_REJECTION:
                // Specific restaurant rejected
                refundSubOrderAmount(parentOrder, rejectedSubOrder);
                break;
        }
    }
    
    private void refundPartialAmount(Order parentOrder) {
        BigDecimal refundAmount = BigDecimal.ZERO;
        
        for (SubOrder subOrder : parentOrder.getSubOrders()) {
            if (subOrder.getState() == OrderState.CANCELLED) {
                refundAmount = refundAmount.add(subOrder.getItemTotal());
            }
        }
        
        // Adjust delivery charges if needed
        int activeSubOrders = (int) parentOrder.getSubOrders().stream()
            .filter(s -> s.getState() != OrderState.CANCELLED)
            .count();
        
        if (activeSubOrders == 1) {
            // Refund additional restaurant fees
            refundAmount = refundAmount.add(new BigDecimal("20"));
        }
        
        initiateRefund(parentOrder.getCustomer(), refundAmount);
    }
}
```

---

## Data Model

### Database Schema

```sql
-- Parent order table
CREATE TABLE orders (
    order_id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    order_type VARCHAR(20) DEFAULT 'SINGLE', -- SINGLE or MULTI_RESTAURANT
    parent_order_id UUID REFERENCES orders(order_id),
    
    -- Aggregated state
    state VARCHAR(32) NOT NULL,
    
    -- Payment
    total_amount DECIMAL(10,2) NOT NULL,
    platform_fee DECIMAL(10,2),
    delivery_charges DECIMAL(10,2),
    gst DECIMAL(10,2),
    payment_status VARCHAR(32),
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    
    -- Metadata
    metadata JSONB DEFAULT '{}'::jsonb
);

-- Sub-orders table
CREATE TABLE sub_orders (
    sub_order_id UUID PRIMARY KEY,
    parent_order_id UUID REFERENCES orders(order_id),
    restaurant_id UUID NOT NULL,
    
    -- State (independent FSM)
    state VARCHAR(32) NOT NULL,
    
    -- Items
    items JSONB NOT NULL,
    item_total DECIMAL(10,2) NOT NULL,
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    accepted_at TIMESTAMP WITH TIME ZONE,
    ready_at TIMESTAMP WITH TIME ZONE,
    
    -- Metadata
    metadata JSONB DEFAULT '{}'::jsonb
);

-- Deliveries table (can have multiple for multi-restaurant)
CREATE TABLE deliveries (
    delivery_id UUID PRIMARY KEY,
    order_id UUID REFERENCES orders(order_id),
    sub_order_ids UUID[] NOT NULL, -- Array of sub-orders in this delivery
    rider_id UUID,
    
    state VARCHAR(32) NOT NULL,
    
    -- Route
    pickup_locations JSONB NOT NULL,
    delivery_location JSONB NOT NULL,
    optimized_route JSONB,
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    assigned_at TIMESTAMP WITH TIME ZONE,
    picked_up_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE
);
```

---

## Implementation Guide

### Step 1: Create Parent Order

```java
@Service
public class MultiRestaurantOrderService {
    
    @Transactional
    public Order createMultiRestaurantOrder(
        MultiRestaurantOrderRequest request,
        UUID customerId
    ) {
        // Create parent order
        Order parentOrder = new Order();
        parentOrder.setOrderId(UUID.randomUUID());
        parentOrder.setCustomerId(customerId);
        parentOrder.setOrderType(OrderType.MULTI_RESTAURANT);
        parentOrder.setState(OrderState.CREATED);
        
        // Calculate totals
        BigDecimal itemTotal = BigDecimal.ZERO;
        
        // Create sub-orders
        List<SubOrder> subOrders = new ArrayList<>();
        for (RestaurantItems restaurantItems : request.getRestaurantItems()) {
            SubOrder subOrder = createSubOrder(
                parentOrder,
                restaurantItems
            );
            subOrders.add(subOrder);
            itemTotal = itemTotal.add(subOrder.getItemTotal());
        }
        
        // Calculate delivery charges
        BigDecimal deliveryCharges = calculateDeliveryCharges(
            subOrders.size()
        );
        
        // Calculate platform fee and GST
        BigDecimal platformFee = new BigDecimal("5");
        BigDecimal gst = itemTotal.add(deliveryCharges)
            .multiply(new BigDecimal("0.05"));
        
        // Set totals
        parentOrder.setTotalAmount(
            itemTotal.add(deliveryCharges).add(platformFee).add(gst)
        );
        parentOrder.setPlatformFee(platformFee);
        parentOrder.setDeliveryCharges(deliveryCharges);
        parentOrder.setGst(gst);
        
        // Save
        orderRepository.save(parentOrder);
        subOrders.forEach(subOrderRepository::save);
        
        return parentOrder;
    }
    
    private BigDecimal calculateDeliveryCharges(int restaurantCount) {
        BigDecimal baseCharge = new BigDecimal("20");
        BigDecimal additionalCharge = new BigDecimal("10")
            .multiply(new BigDecimal(restaurantCount - 1));
        return baseCharge.add(additionalCharge);
    }
}
```

### Step 2: Handle Sub-Order State Changes

```java
@Component
public class SubOrderStateChangeListener {
    
    @EventListener
    public void onSubOrderStateChange(SubOrderStateChangedEvent event) {
        SubOrder subOrder = event.getSubOrder();
        Order parentOrder = subOrder.getParentOrder();
        
        // Recalculate parent order state
        OrderState newParentState = calculateParentState(
            parentOrder.getSubOrders()
        );
        
        if (newParentState != parentOrder.getState()) {
            parentOrder.setState(newParentState);
            orderRepository.save(parentOrder);
            
            // Publish parent order state change event
            eventPublisher.publishEvent(
                new OrderStateChangedEvent(parentOrder)
            );
            
            // Trigger delivery assignment if all ready
            if (newParentState == OrderState.READY_FOR_PICKUP) {
                deliveryService.assignDelivery(parentOrder);
            }
        }
    }
}
```

### Step 3: Create Delivery Batches

```java
@Service
public class DeliveryAssignmentService {
    
    @Transactional
    public void assignDelivery(Order parentOrder) {
        List<SubOrder> readySubOrders = parentOrder.getSubOrders().stream()
            .filter(s -> s.getState() == OrderState.READY_FOR_PICKUP)
            .collect(Collectors.toList());
        
        // Create batches
        List<DeliveryBatch> batches = batchingService.createBatches(
            readySubOrders,
            parentOrder.getDeliveryLocation()
        );
        
        // Create delivery for each batch
        for (DeliveryBatch batch : batches) {
            Delivery delivery = new Delivery();
            delivery.setDeliveryId(UUID.randomUUID());
            delivery.setOrderId(parentOrder.getOrderId());
            delivery.setSubOrderIds(batch.getSubOrderIds());
            delivery.setState(DeliveryState.PENDING);
            
            // Optimize route
            Route route = batchingService.optimizeRoute(
                batch,
                parentOrder.getDeliveryLocation()
            );
            delivery.setOptimizedRoute(route);
            
            deliveryRepository.save(delivery);
            
            // Trigger rider assignment
            deliveryFSM.fire(delivery.getDeliveryId(), DeliveryTrigger.FIND_RIDERS);
        }
    }
}
```

---

## Next Steps

1. Review this Multi-Restaurant Design
2. Review [Smart Assignment Algorithm](./06_SMART_ASSIGNMENT_ALGORITHM.md)
3. Create implementation stories
4. Begin Phase 1 implementation
