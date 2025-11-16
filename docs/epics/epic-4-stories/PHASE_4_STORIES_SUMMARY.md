# Phase 4: FSM Integration & Customer Status - Stories Summary

**Phase:** 4  
**Epic:** BE-004  
**Total Story Points:** 26  
**Status:** 📝 Ready for Implementation  
**Created:** November 9, 2025

---

## Overview

Phase 4 focuses on integrating Order and Delivery FSMs, creating a customer-facing status abstraction layer, and implementing push notifications.

### Goals
- ✅ Seamless Order ↔ Delivery FSM integration
- ✅ Customer-friendly status API (8 states vs 22 internal states)
- ✅ Real-time push notifications
- ✅ Event-driven architecture

---

## Stories

### **BE-004-26: Event-Driven FSM Integration** (8 pts)

**Objective:** Integrate Order FSM and Delivery FSM through Kafka events

**Key Deliverables:**
- Kafka event listeners for FSM coordination
- Order FSM triggers Delivery FSM creation
- Delivery FSM updates trigger Order FSM transitions
- Event schema validation
- Idempotency handling

**Events:**
```
Order FSM → Delivery FSM:
- order.ready_for_pickup → CREATE delivery, FIND_RIDERS

Delivery FSM → Order FSM:
- delivery.rider_accepted → ASSIGN_RIDER
- delivery.picked_up → RIDER_PICKUP
- delivery.delivered → DELIVER_ORDER
- delivery.failed → CANCEL_ORDER
```

**Acceptance Criteria:**
- [ ] OrderEventConsumer listens to order-events topic
- [ ] DeliveryEventConsumer listens to delivery-events topic
- [ ] Order READY_FOR_PICKUP creates delivery automatically
- [ ] Delivery state changes update order state
- [ ] Idempotency keys prevent duplicate processing
- [ ] Dead letter queue for failed events
- [ ] Integration tests for all event flows

---

### **BE-004-27: Customer Status Abstraction Layer** (8 pts)

**Objective:** Create StatusMapper service to convert 22 internal states to 8 customer-facing states

**Key Deliverables:**
- StatusMapper service
- CustomerStatus enum (8 states)
- Mapping logic for all FSM state combinations
- Status transition notifications
- Multi-language support structure

**Customer States:**
1. ORDER_PLACED
2. ORDER_CONFIRMED
3. PREPARING
4. RIDER_ASSIGNED
5. READY_FOR_PICKUP
6. OUT_FOR_DELIVERY
7. DELIVERED
8. CANCELLED

**Mapping Examples:**
```
Order: PENDING_ACCEPTANCE + Delivery: null → ORDER_PLACED
Order: ACCEPTED + Delivery: null → ORDER_CONFIRMED
Order: PREPARING + Delivery: SEARCHING_RIDER → PREPARING
Order: READY_FOR_PICKUP + Delivery: RIDER_ACCEPTED → RIDER_ASSIGNED
Order: ASSIGNED_TO_RIDER + Delivery: PICKED_UP → OUT_FOR_DELIVERY
Order: DELIVERED + Delivery: DELIVERED → DELIVERED
```

**Acceptance Criteria:**
- [ ] CustomerStatus enum created
- [ ] StatusMapper service with mapping logic
- [ ] Unit tests for all 22 FSM state combinations
- [ ] Customer-friendly messages for each status
- [ ] ETA calculation based on current status
- [ ] Progress percentage (0-100%)

---

### **BE-004-28: Customer Status API** (5 pts)

**Objective:** REST API for customers to track orders with simplified status

**Key Deliverables:**
- CustomerOrderController
- Status tracking endpoints
- Real-time updates via SSE (Server-Sent Events)
- ETA and progress tracking

**Endpoints:**
```
GET /api/v1/customers/{customerId}/orders/{orderId}/status
Response: {
  "orderId": "uuid",
  "customerStatus": "OUT_FOR_DELIVERY",
  "message": "Your order is on the way",
  "estimatedArrival": "2025-11-09T20:30:00Z",
  "progressPercentage": 75,
  "canCancel": false,
  "riderInfo": {
    "name": "Rajesh",
    "phone": "9876543210",
    "rating": 4.8,
    "currentLocation": {...}
  }
}

GET /api/v1/customers/{customerId}/orders/{orderId}/timeline
Response: [
  {
    "status": "ORDER_PLACED",
    "timestamp": "2025-11-09T19:00:00Z",
    "message": "Order placed successfully"
  },
  {
    "status": "ORDER_CONFIRMED",
    "timestamp": "2025-11-09T19:02:00Z",
    "message": "Restaurant confirmed your order"
  },
  ...
]

GET /api/v1/customers/{customerId}/orders/active (SSE)
Stream: Server-Sent Events for real-time updates
```

**Acceptance Criteria:**
- [ ] CustomerOrderController implemented
- [ ] Status endpoint with full details
- [ ] Timeline endpoint showing history
- [ ] SSE endpoint for real-time updates
- [ ] Swagger documentation
- [ ] Integration tests

---

### **BE-004-29: Push Notification Service** (5 pts)

**Objective:** Implement Firebase Cloud Messaging (FCM) for push notifications

**Key Deliverables:**
- FCM integration
- NotificationService enhancement
- Device token management
- Notification templates
- Delivery tracking

**Notification Events:**
```
1. Order Confirmed → "Restaurant accepted your order"
2. Preparing → "Your food is being prepared"
3. Rider Assigned → "Rajesh is heading to the restaurant"
4. Out for Delivery → "Your order is on the way"
5. Delivered → "Your order has been delivered. Enjoy!"
6. Cancelled → "Your order was cancelled. Refund initiated."
```

**Acceptance Criteria:**
- [ ] FCM SDK integrated
- [ ] Device token registration API
- [ ] NotificationService sends FCM notifications
- [ ] Notification templates for all customer states
- [ ] Notification preferences (enable/disable)
- [ ] Delivery tracking (sent/delivered/failed)
- [ ] Retry logic for failed notifications
- [ ] Integration tests

---

## Implementation Order

### **Week 1: FSM Integration**
1. BE-004-26: Event-Driven FSM Integration (8 pts)
   - Day 1-2: Event consumers
   - Day 3-4: Integration logic
   - Day 5: Testing

### **Week 2: Customer Experience**
2. BE-004-27: Customer Status Abstraction (8 pts)
   - Day 1-2: StatusMapper service
   - Day 3: Mapping logic
   - Day 4-5: Testing

3. BE-004-28: Customer Status API (5 pts)
   - Day 1-2: REST endpoints
   - Day 3: SSE implementation
   - Day 4: Testing

4. BE-004-29: Push Notifications (5 pts)
   - Day 1-2: FCM integration
   - Day 3: Notification templates
   - Day 4: Testing

---

## Dependencies

### **Prerequisites (Must be complete):**
- ✅ Phase 1: Foundation (Kafka, Redis, PostgreSQL)
- ✅ Phase 2: Order FSM
- ✅ Phase 3: Delivery FSM

### **External Dependencies:**
- Firebase Cloud Messaging account
- FCM server key
- Device token registration flow (mobile app)

---

## Technical Stack

**Event Processing:**
- Kafka consumers with manual commit
- Idempotency keys (UUID)
- Dead letter queue (DLQ)

**Status Mapping:**
- Strategy pattern for mapping logic
- Caching for frequently accessed statuses
- Redis for real-time updates

**Push Notifications:**
- Firebase Admin SDK
- FCM HTTP v1 API
- Notification templates with i18n

**Real-time Updates:**
- Server-Sent Events (SSE)
- WebSocket (future enhancement)

---

## Testing Strategy

### **Unit Tests**
- StatusMapper logic (all 22 combinations)
- Event consumer processing
- Notification template rendering

### **Integration Tests**
- End-to-end order flow with status updates
- Event-driven FSM transitions
- FCM notification delivery

### **Performance Tests**
- 1000 concurrent status requests
- Event processing latency < 100ms
- Notification delivery < 2 seconds

---

## Success Metrics

| Metric | Target |
|--------|--------|
| **Event Processing Latency** | < 100ms |
| **Status API Response Time** | < 200ms |
| **Notification Delivery Rate** | > 95% |
| **Customer Confusion Rate** | < 5% (surveys) |
| **Status Update Accuracy** | 100% |

---

## Next Phase

**Phase 5: Multi-Restaurant Support**
- Parent-child order model
- Sub-order state aggregation
- Delivery batching
- Payment distribution

---

**Status:** Ready for implementation  
**Estimated Duration:** 2 weeks  
**Team:** Backend team (2 developers)
