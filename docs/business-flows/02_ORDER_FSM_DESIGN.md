# Order FSM - Detailed Design

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
5. [Side Effects](#side-effects)
6. [Timeout Handling](#timeout-handling)
7. [Validation Rules](#validation-rules)
8. [Order Management APIs](#order-management-apis)
9. [Implementation Guide](#implementation-guide)

---

## Overview

### Purpose

The Order FSM manages the complete lifecycle of a customer order from placement through delivery completion. It handles:

- Order validation and payment
- Restaurant acceptance workflow
- Food preparation tracking
- Integration with Delivery FSM
- Cancellation and error handling

### Responsibilities

| Responsibility | Description |
|----------------|-------------|
| **Validation** | Pre-acceptance checks (restaurant open, delivery zone, pricing) |
| **Payment** | Payment capture/reservation |
| **Restaurant Flow** | Acceptance, rejection, timeout handling |
| **Preparation** | Track food preparation progress |
| **Delivery Trigger** | Initiate delivery assignment when food is ready |
| **Customer Updates** | Publish events for customer notifications |
| **Audit Trail** | Log all state transitions |

---

## State Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      ORDER STATE MACHINE                        │
└─────────────────────────────────────────────────────────────────┘

    [CREATED]
        │
        │ validate_order
        ▼
    [VALIDATED] ──────────────────────────────────┐
        │                                         │
        │ payment_success                         │ validation_failed
        ▼                                         ▼
    [PAYMENT_CONFIRMED]                      [REJECTED]
        │                                    (terminal)
        │ notify_restaurant
        ▼
    [PENDING_ACCEPTANCE] ─────────────────────────┐
        │                                         │
        │ restaurant_accept                       │ restaurant_reject
        │                                         │ timeout (2 min)
        ▼                                         ▼
    [ACCEPTED]                               [REJECTED]
        │                                    (terminal)
        │ start_preparation
        ▼
    [PREPARING]
        │
        │ mark_ready
        ▼
    [READY_FOR_PICKUP]
        │
        │ rider_assigned (from Delivery FSM event)
        ▼
    [ASSIGNED_TO_RIDER]
        │
        │ rider_picked_up (from Delivery FSM event)
        ▼
    [PICKED_UP]
        │
        │ rider_delivered (from Delivery FSM event)
        ▼
    [DELIVERED]
   (terminal)
        │
        │ customer_rated
        ▼
    [CLOSED]
   (terminal)

    [CANCELLED] ◄──── can transition from CREATED, VALIDATED, 
   (terminal)         PENDING_ACCEPTANCE, ACCEPTED states
```

---

## States Definition

### 1. CREATED

**Description:** Order object created in the system, not yet validated.

| Property | Value |
|----------|-------|
| **Can Cancel?** | Yes (free) |
| **Timeout** | 30 seconds |
| **Next States** | VALIDATED, REJECTED |
| **Customer Status** | ORDER_PLACED |

**Entry Actions:**
- Generate order ID
- Create order record in database
- Set created timestamp

**Exit Actions:**
- None

---

### 2. VALIDATED

**Description:** Pre-acceptance validation checks passed.

| Property | Value |
|----------|-------|
| **Can Cancel?** | Yes (free) |
| **Timeout** | 5 minutes |
| **Next States** | PAYMENT_CONFIRMED, REJECTED |
| **Customer Status** | ORDER_PLACED |

**Validation Checks:**
1. Restaurant is open and accepting orders
2. Customer address in delivery zone
3. Meets minimum order value
4. All menu items are active
5. Stock/inventory available
6. Final price matches submitted total
7. User not flagged or rate-limited
8. Prep + travel time fits SLA

**Entry Actions:**
- Run all validation checks
- Calculate final pricing
- Reserve inventory (if tracked)

**Exit Actions:**
- None

---

### 3. PAYMENT_CONFIRMED

**Description:** Payment captured or reserved successfully.

| Property | Value |
|----------|-------|
| **Can Cancel?** | Yes (free) |
| **Timeout** | 5 minutes |
| **Next States** | PENDING_ACCEPTANCE, CANCELLED |
| **Customer Status** | ORDER_PLACED |

**Entry Actions:**
- Capture/reserve payment
- Set Redis TTL for auto-cancel (5 min)
- Publish `order.payment_confirmed` event

**Exit Actions:**
- Clear auto-cancel TTL if transitioning to PENDING_ACCEPTANCE

---

### 4. PENDING_ACCEPTANCE

**Description:** Order sent to restaurant, awaiting acceptance.

| Property | Value |
|----------|-------|
| **Can Cancel?** | Yes (free) |
| **Timeout** | 2 minutes |
| **Next States** | ACCEPTED, REJECTED, CANCELLED |
| **Customer Status** | ORDER_PLACED |

**Entry Actions:**
- Send push notification to restaurant
- Set Redis TTL for acceptance timeout (2 min)
- Publish `order.pending_acceptance` event
- Start acceptance timer

**Exit Actions:**
- Clear acceptance timeout TTL
- Log restaurant response time

---

### 5. ACCEPTED

**Description:** Restaurant accepted the order.

| Property | Value |
|----------|-------|
| **Can Cancel?** | Yes (₹20 fee) |
| **Timeout** | 30 minutes |
| **Next States** | PREPARING, CANCELLED |
| **Customer Status** | ORDER_CONFIRMED |

**Entry Actions:**
- Notify customer (order accepted)
- Publish `order.accepted` event
- Start preparation timer
- Log restaurant acceptance time

**Exit Actions:**
- None

---

### 6. PREPARING

**Description:** Food is being prepared by restaurant.

| Property | Value |
|----------|-------|
| **Can Cancel?** | No |
| **Timeout** | 30 minutes |
| **Next States** | READY_FOR_PICKUP |
| **Customer Status** | PREPARING (then RIDER_ASSIGNED when rider assigned) |

**Entry Actions:**
- Notify customer (food being prepared)
- Start prep timer
- **🎯 Schedule rider assignment** (based on smart algorithm)
- Publish `order.preparing` event

**During State:**
- Monitor prep progress
- Update customer with time remaining
- When scheduled time reached → Trigger delivery assignment

**Exit Actions:**
- Mark food as ready
- Calculate actual prep time vs estimated

---

### 7. READY_FOR_PICKUP

**Description:** Food is ready, waiting for rider pickup.

| Property | Value |
|----------|-------|
| **Can Cancel?** | No |
| **Timeout** | 15 minutes |
| **Next States** | ASSIGNED_TO_RIDER |
| **Customer Status** | READY_FOR_PICKUP |

**Entry Actions:**
- Notify customer (food ready)
- **🎯 Publish `order.ready_for_pickup` event** (triggers Delivery FSM)
- Start rider search (if not already assigned)

**Exit Actions:**
- None

---

### 8. ASSIGNED_TO_RIDER

**Description:** Rider assigned and notified.

| Property | Value |
|----------|-------|
| **Can Cancel?** | No |
| **Timeout** | 10 minutes |
| **Next States** | PICKED_UP |
| **Customer Status** | OUT_FOR_DELIVERY |

**Entry Actions:**
- Notify customer (rider assigned)
- Notify restaurant (rider coming)
- Publish `order.rider_assigned` event

**Exit Actions:**
- None

---

### 9. PICKED_UP

**Description:** Rider picked up order from restaurant.

| Property | Value |
|----------|-------|
| **Can Cancel?** | No |
| **Timeout** | 45 minutes |
| **Next States** | DELIVERED |
| **Customer Status** | OUT_FOR_DELIVERY |

**Entry Actions:**
- Notify customer (order picked up)
- Start delivery timer
- Enable live tracking
- Publish `order.picked_up` event

**Exit Actions:**
- None

---

### 10. DELIVERED

**Description:** Order successfully delivered to customer.

| Property | Value |
|----------|-------|
| **Can Cancel?** | No |
| **Timeout** | None |
| **Next States** | CLOSED |
| **Customer Status** | DELIVERED |

**Entry Actions:**
- Notify customer (order delivered)
- Record delivery timestamp
- Calculate delivery time
- Publish `order.delivered` event
- Trigger rating prompt
- Settle payments (restaurant, rider, platform)

**Exit Actions:**
- None

---

### 11. CLOSED

**Description:** Order completed and archived.

| Property | Value |
|----------|-------|
| **Can Cancel?** | No |
| **Timeout** | None |
| **Next States** | None (terminal) |
| **Customer Status** | DELIVERED |

**Entry Actions:**
- Archive order
- Update analytics
- Publish `order.closed` event

**Exit Actions:**
- None

---

### 12. CANCELLED

**Description:** Order cancelled (by customer, restaurant, or system).

| Property | Value |
|----------|-------|
| **Can Cancel?** | N/A (already cancelled) |
| **Timeout** | None |
| **Next States** | None (terminal) |
| **Customer Status** | CANCELLED |

**Entry Actions:**
- Initiate refund (if paid)
- Notify all parties
- Cancel delivery (if assigned)
- Apply cancellation fee (if applicable)
- Publish `order.cancelled` event
- Log cancellation reason

**Exit Actions:**
- None

---

### 13. REJECTED

**Description:** Order rejected (validation failed or restaurant rejected).

| Property | Value |
|----------|-------|
| **Can Cancel?** | N/A (already rejected) |
| **Timeout** | None |
| **Next States** | None (terminal) |
| **Customer Status** | CANCELLED |

**Entry Actions:**
- Initiate refund
- Notify customer with reason
- Publish `order.rejected` event
- Log rejection reason

**Exit Actions:**
- None

---

## Triggers & Transitions

### Trigger: VALIDATE_ORDER

```java
FROM: CREATED
TO: VALIDATED | REJECTED
```

**Preconditions:**
- Order exists in CREATED state

**Actions:**
1. Run pre-acceptance validation
2. Check restaurant availability
3. Verify delivery zone
4. Validate menu items
5. Recalculate pricing
6. If all checks pass → VALIDATED
7. If any check fails → REJECTED + notify customer + refund

**Events Published:**
- `order.validated` or `order.rejected`

---

### Trigger: CONFIRM_PAYMENT

```java
FROM: VALIDATED
TO: PAYMENT_CONFIRMED
```

**Preconditions:**
- Order is in VALIDATED state
- Payment gateway available

**Actions:**
1. Capture/reserve payment
2. Generate order ID
3. Set Redis TTL for auto-cancel (5 min)
4. Update order state

**Events Published:**
- `order.payment_confirmed`

---

### Trigger: NOTIFY_RESTAURANT

```java
FROM: PAYMENT_CONFIRMED
TO: PENDING_ACCEPTANCE
```

**Preconditions:**
- Payment is confirmed
- Restaurant is online

**Actions:**
1. Send push notification to restaurant
2. Set Redis TTL for acceptance timeout (2 min)
3. Start acceptance timer

**Events Published:**
- `order.pending_acceptance`

---

### Trigger: ACCEPT_ORDER

```java
FROM: PENDING_ACCEPTANCE
TO: ACCEPTED
```

**Preconditions:**
- Restaurant is still online
- Within acceptance timeout

**Actions:**
1. Clear acceptance timeout
2. Notify customer
3. Start preparation timer
4. Log restaurant acceptance time

**Events Published:**
- `order.accepted`

---

### Trigger: REJECT_ORDER

```java
FROM: PENDING_ACCEPTANCE
TO: REJECTED
```

**Preconditions:**
- Restaurant actively rejects
- OR acceptance timeout expires

**Actions:**
1. Notify customer with reason
2. Initiate refund
3. Log rejection reason
4. Update restaurant metrics

**Events Published:**
- `order.rejected`

---

### Trigger: START_PREPARATION

```java
FROM: ACCEPTED
TO: PREPARING
```

**Preconditions:**
- Restaurant confirmed start

**Actions:**
1. Notify customer
2. Start prep timer
3. **Schedule rider assignment** (smart algorithm)

**Events Published:**
- `order.preparing`

---

### Trigger: MARK_READY

```java
FROM: PREPARING
TO: READY_FOR_PICKUP
```

**Preconditions:**
- Restaurant marks food as ready

**Actions:**
1. Notify customer
2. **Publish `order.ready_for_pickup`** (triggers Delivery FSM)
3. Start rider search (if not already assigned)

**Events Published:**
- `order.ready_for_pickup` (critical integration point)

---

### Trigger: ASSIGN_RIDER

```java
FROM: READY_FOR_PICKUP
TO: ASSIGNED_TO_RIDER
```

**Preconditions:**
- Delivery FSM published `delivery.rider_assigned` event

**Actions:**
1. Notify customer
2. Notify restaurant
3. Update order with rider details

**Events Published:**
- `order.rider_assigned`

---

### Trigger: RIDER_PICKUP

```java
FROM: ASSIGNED_TO_RIDER
TO: PICKED_UP
```

**Preconditions:**
- Delivery FSM published `delivery.picked_up` event

**Actions:**
1. Notify customer
2. Start delivery timer
3. Enable live tracking

**Events Published:**
- `order.picked_up`

---

### Trigger: DELIVER_ORDER

```java
FROM: PICKED_UP
TO: DELIVERED
```

**Preconditions:**
- Delivery FSM published `delivery.delivered` event

**Actions:**
1. Notify customer
2. Record delivery timestamp
3. Calculate delivery time
4. Trigger rating prompt
5. Settle payments

**Events Published:**
- `order.delivered`

---

### Trigger: CLOSE_ORDER

```java
FROM: DELIVERED
TO: CLOSED
```

**Preconditions:**
- Customer rated order (optional)
- OR auto-close after 24 hours

**Actions:**
1. Archive order
2. Update analytics

**Events Published:**
- `order.closed`

---

### Trigger: CANCEL_ORDER

```java
FROM: CREATED, VALIDATED, PAYMENT_CONFIRMED, PENDING_ACCEPTANCE, ACCEPTED
TO: CANCELLED
```

**Preconditions:**
- Order is in cancellable state
- Cancellation allowed per business rules

**Actions:**
1. Initiate refund (if paid)
2. Notify all parties
3. Cancel delivery (if assigned)
4. Apply cancellation fee (if applicable)
5. Log cancellation reason

**Events Published:**
- `order.cancelled`

**Cancellation Rules:**

| From State | Cancellation Fee | Refund |
|------------|------------------|--------|
| CREATED | Free | Full |
| VALIDATED | Free | Full |
| PAYMENT_CONFIRMED | Free | Full |
| PENDING_ACCEPTANCE | Free | Full |
| ACCEPTED | ₹20 | Full - ₹20 |
| PREPARING onwards | Not allowed | N/A |

---

## Implementation Guide

### Stateless4j Configuration

```java
@Configuration
public class OrderFSMConfig {
    
    @Bean
    public StateMachineConfig<OrderState, OrderTrigger> orderFSMConfig() {
        StateMachineConfig<OrderState, OrderTrigger> config = 
            new StateMachineConfig<>();
        
        // Configure transitions
        config.configure(OrderState.CREATED)
            .permit(OrderTrigger.VALIDATE_ORDER, OrderState.VALIDATED)
            .permit(OrderTrigger.VALIDATION_FAILED, OrderState.REJECTED);
        
        config.configure(OrderState.VALIDATED)
            .permit(OrderTrigger.CONFIRM_PAYMENT, OrderState.PAYMENT_CONFIRMED);
        
        config.configure(OrderState.PAYMENT_CONFIRMED)
            .permit(OrderTrigger.NOTIFY_RESTAURANT, OrderState.PENDING_ACCEPTANCE)
            .permit(OrderTrigger.CANCEL_ORDER, OrderState.CANCELLED);
        
        config.configure(OrderState.PENDING_ACCEPTANCE)
            .permit(OrderTrigger.ACCEPT_ORDER, OrderState.ACCEPTED)
            .permit(OrderTrigger.REJECT_ORDER, OrderState.REJECTED)
            .permit(OrderTrigger.TIMEOUT_ACCEPTANCE, OrderState.REJECTED)
            .permit(OrderTrigger.CANCEL_ORDER, OrderState.CANCELLED);
        
        config.configure(OrderState.ACCEPTED)
            .permit(OrderTrigger.START_PREPARATION, OrderState.PREPARING)
            .permit(OrderTrigger.CANCEL_ORDER, OrderState.CANCELLED);
        
        config.configure(OrderState.PREPARING)
            .permit(OrderTrigger.MARK_READY, OrderState.READY_FOR_PICKUP);
        
        config.configure(OrderState.READY_FOR_PICKUP)
            .permit(OrderTrigger.ASSIGN_RIDER, OrderState.ASSIGNED_TO_RIDER);
        
        config.configure(OrderState.ASSIGNED_TO_RIDER)
            .permit(OrderTrigger.RIDER_PICKUP, OrderState.PICKED_UP);
        
        config.configure(OrderState.PICKED_UP)
            .permit(OrderTrigger.DELIVER_ORDER, OrderState.DELIVERED);
        
        config.configure(OrderState.DELIVERED)
            .permit(OrderTrigger.CLOSE_ORDER, OrderState.CLOSED);
        
        // Configure entry/exit actions
        config.configure(OrderState.PREPARING)
            .onEntry(() -> scheduleRiderAssignment());
        
        config.configure(OrderState.READY_FOR_PICKUP)
            .onEntry(() -> publishReadyForPickupEvent());
        
        return config;
    }
}
```

---

## Order Management APIs

> **📋 API Implementation Standards**
> 
> All APIs defined in this section MUST follow the standards documented in [REST API Standards](../REST_API_STANDARDS.md).
> 
> **Key Requirements:**
> - Follow RESTful resource modeling principles
> - Use standard HTTP status codes (200, 201, 400, 404, 500, etc.)
> - Implement comprehensive error handling with structured error responses
> - Add input validation with clear error messages
> - Document all endpoints using Swagger/OpenAPI annotations
> - Follow controller and service layer patterns
> - Include unit and integration tests
> 
> Refer to the REST API Standards document for detailed guidelines, code examples, and best practices.

### 1. Place Order API

**Endpoint:** `POST /api/v1/orders`

**Description:** Creates a new order and initiates the Order FSM. Supports both single-restaurant and multi-restaurant orders.

**Request:**

```json
{
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "orderType": "MULTI_RESTAURANT",
  "restaurantOrders": [
    {
      "restaurantId": "660e8400-e29b-41d4-a716-446655440001",
      "branchId": "660e8400-e29b-41d4-a716-446655440002",
      "items": [
        {
          "menuItemId": "770e8400-e29b-41d4-a716-446655440003",
          "quantity": 2,
          "price": 15.00,
          "customizations": [
            {
              "name": "Extra Spicy",
              "price": 0.00
            }
          ]
        }
      ]
    },
    {
      "restaurantId": "660e8400-e29b-41d4-a716-446655440004",
      "branchId": "660e8400-e29b-41d4-a716-446655440005",
      "items": [
        {
          "menuItemId": "770e8400-e29b-41d4-a716-446655440006",
          "quantity": 1,
          "price": 250.00
        }
      ]
    }
  ],
  "deliveryAddress": {
    "addressLine1": "123 Main Street",
    "addressLine2": "Apt 4B",
    "city": "Bangalore",
    "state": "Karnataka",
    "pincode": "560001",
    "latitude": 12.9716,
    "longitude": 77.5946
  },
  "paymentMethod": "CARD",
  "paymentDetails": {
    "cardToken": "tok_visa_4242",
    "saveCard": false
  },
  "specialInstructions": "Ring the doorbell twice"
}
```

**Response (Success - 201 Created):**

```json
{
  "orderId": "880e8400-e29b-41d4-a716-446655440007",
  "orderType": "MULTI_RESTAURANT",
  "state": "CREATED",
  "subOrders": [
    {
      "subOrderId": "880e8400-e29b-41d4-a716-446655440007-1",
      "restaurantId": "660e8400-e29b-41d4-a716-446655440001",
      "restaurantName": "Chai Express",
      "state": "CREATED",
      "itemTotal": 30.00
    },
    {
      "subOrderId": "880e8400-e29b-41d4-a716-446655440007-2",
      "restaurantId": "660e8400-e29b-41d4-a716-446655440004",
      "restaurantName": "Pizza Corner",
      "state": "CREATED",
      "itemTotal": 250.00
    }
  ],
  "pricing": {
    "itemTotal": 280.00,
    "deliveryCharges": 30.00,
    "platformFee": 5.00,
    "gst": 15.75,
    "totalAmount": 330.75
  },
  "estimatedDeliveryTime": "2025-11-09T13:30:00Z",
  "createdAt": "2025-11-09T12:30:00Z"
}
```

**Response (Error - 400 Bad Request):**

```json
{
  "error": "VALIDATION_FAILED",
  "message": "Order validation failed",
  "details": [
    {
      "field": "restaurantOrders[0].restaurantId",
      "issue": "Restaurant is currently closed",
      "restaurantId": "660e8400-e29b-41d4-a716-446655440001"
    },
    {
      "field": "deliveryAddress",
      "issue": "Delivery address is outside service area"
    }
  ]
}
```

**FSM Flow Triggered:**

```
1. Order created → CREATED state
2. Validation triggered → VALIDATED state (if successful)
3. Payment processed → PAYMENT_CONFIRMED state
4. Restaurant notified → PENDING_ACCEPTANCE state
```

---

### 2. Get Order Status API

**Endpoint:** `GET /api/v1/orders/{orderId}`

**Description:** Retrieves current order status and details.

**Response:**

```json
{
  "orderId": "880e8400-e29b-41d4-a716-446655440007",
  "state": "PREPARING",
  "customerStatus": "PREPARING",
  "subOrders": [
    {
      "subOrderId": "880e8400-e29b-41d4-a716-446655440007-1",
      "restaurantName": "Chai Express",
      "state": "PREPARING",
      "estimatedReadyTime": "2025-11-09T12:50:00Z"
    }
  ],
  "timeline": [
    {
      "state": "CREATED",
      "timestamp": "2025-11-09T12:30:00Z"
    },
    {
      "state": "PAYMENT_CONFIRMED",
      "timestamp": "2025-11-09T12:30:30Z"
    },
    {
      "state": "ACCEPTED",
      "timestamp": "2025-11-09T12:32:00Z"
    },
    {
      "state": "PREPARING",
      "timestamp": "2025-11-09T12:35:00Z"
    }
  ]
}
```

---

### 3. Cancel Order API

**Endpoint:** `POST /api/v1/orders/{orderId}/cancel`

**Description:** Cancels an order if it's in a cancellable state.

**Request:**

```json
{
  "reason": "CUSTOMER_REQUEST",
  "comments": "Changed my mind"
}
```

**Response (Success - 200 OK):**

```json
{
  "orderId": "880e8400-e29b-41d4-a716-446655440007",
  "state": "CANCELLED",
  "cancellationFee": 0.00,
  "refundAmount": 330.75,
  "refundStatus": "INITIATED",
  "estimatedRefundTime": "3-5 business days"
}
```

**Response (Error - 400 Bad Request):**

```json
{
  "error": "CANCELLATION_NOT_ALLOWED",
  "message": "Order cannot be cancelled at this stage",
  "currentState": "PICKED_UP",
  "reason": "Food has already been picked up by delivery partner"
}
```

---

### 4. Update Order API (Restaurant)

**Endpoint:** `PATCH /api/v1/orders/{orderId}/restaurant-action`

**Description:** Allows restaurant to accept/reject or update order status.

**Request (Accept Order):**

```json
{
  "action": "ACCEPT",
  "estimatedPrepTime": 18
}
```

**Request (Reject Order):**

```json
{
  "action": "REJECT",
  "reason": "ITEM_UNAVAILABLE",
  "comments": "Pizza dough not available"
}
```

**Request (Mark Ready):**

```json
{
  "action": "MARK_READY"
}
```

**Response:**

```json
{
  "orderId": "880e8400-e29b-41d4-a716-446655440007",
  "state": "ACCEPTED",
  "estimatedReadyTime": "2025-11-09T12:50:00Z"
}
```

---

### Implementation Notes

**Order Creation Flow:**

```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderFSM orderFSM;
    
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
        @RequestBody @Valid CreateOrderRequest request
    ) {
        // 1. Create order entity
        Order order = orderService.createOrder(request);
        
        // 2. Trigger FSM: CREATED → VALIDATED
        orderFSM.fire(order.getOrderId(), OrderTrigger.VALIDATE_ORDER);
        
        // 3. Trigger FSM: VALIDATED → PAYMENT_CONFIRMED
        orderFSM.fire(order.getOrderId(), OrderTrigger.CONFIRM_PAYMENT);
        
        // 4. Trigger FSM: PAYMENT_CONFIRMED → PENDING_ACCEPTANCE
        orderFSM.fire(order.getOrderId(), OrderTrigger.NOTIFY_RESTAURANT);
        
        // 5. Return response
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(OrderResponse.from(order));
    }
}
```

**Validation:**
- Restaurant operational hours
- Delivery zone coverage
- Menu item availability
- Pricing accuracy
- Payment method validity

**Payment Processing:**
- Initial payment capture/authorization happens in `PAYMENT_CONFIRMED` state
- For multi-restaurant orders, single payment is split during settlement
- Refer to [Multi-Restaurant Design](./05_MULTI_RESTAURANT_DESIGN.md) for payment distribution

---

## Next Steps

1. Review this Order FSM design
2. Proceed to [Delivery FSM Design](./03_DELIVERY_FSM_DESIGN.md)
3. Review [Customer Status Design](./04_CUSTOMER_STATUS_DESIGN.md)
4. Create implementation stories
