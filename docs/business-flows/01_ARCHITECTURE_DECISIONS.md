# Architecture Decisions - Order & Delivery System

**Document Version:** 1.0  
**Last Updated:** November 9, 2025  
**Epic:** Epic-4 - Order & Delivery Management  
**Status:** Approved

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Key Architecture Decisions](#key-architecture-decisions)
3. [Dual-FSM Architecture](#dual-fsm-architecture)
4. [Integration Pattern](#integration-pattern)
5. [Technology Stack](#technology-stack)
6. [Implementation Sequence](#implementation-sequence)

---

## Executive Summary

### Purpose

This document captures the foundational architecture decisions for the Order and Delivery management system. These decisions will guide all subsequent implementation stories.

### Key Outcomes

- **Separate Order & Delivery FSMs** for independent scaling
- **Event-driven integration** via Kafka for loose coupling
- **Customer status abstraction** to simplify UX
- **Smart rider assignment** during food preparation
- **Parent-child order model** for multi-restaurant support

---

## Key Architecture Decisions

### Decision 1: Separate Order & Delivery FSMs

**Status:** ✅ APPROVED

**Decision:** Use TWO independent Finite State Machines instead of one combined FSM.

**Rationale:**

| Aspect | Single FSM | Dual FSM (Selected) |
|--------|-----------|---------------------|
| Scalability | Bottleneck | Independent scaling |
| Complexity | 20+ mixed states | 13 + 9 separated states |
| Team Ownership | Single team | Order team + Delivery team |
| Failure Isolation | Coupled failures | Independent failures |
| Flexibility | Hard to change | Easy to swap delivery providers |
| Testing | Complex integration | Unit test each FSM |

**Consequences:**

- ✅ Order processing can scale independently from delivery
- ✅ Can support multiple delivery models (own fleet, 3rd party, hybrid)
- ✅ Order can exist without delivery assignment (resilient)
- ⚠️ Need event-driven integration between FSMs
- ⚠️ Eventual consistency between order and delivery states

---

### Decision 2: Event-Driven Integration

**Status:** ✅ APPROVED

**Decision:** Use Apache Kafka for asynchronous communication between Order and Delivery FSMs.

**Architecture:**

```
ORDER FSM                    Kafka Topics              DELIVERY FSM
─────────                    ────────────              ────────────

MARK_READY ──────► order.ready_for_pickup ──────► CREATE delivery
                                                   FIND_RIDERS

RIDER_ACCEPT ◄──── delivery.rider_accepted ◄───── RIDER_ACCEPT

RIDER_PICKUP ◄──── delivery.picked_up ◄────────── PICKUP_ORDER

DELIVER_ORDER ◄─── delivery.delivered ◄────────── DELIVER_ORDER

CANCEL_ORDER ◄──── delivery.failed ◄───────────── FAIL_DELIVERY
```

**Benefits:**

1. **Loose Coupling:** FSMs don't directly call each other
2. **Asynchronous:** Non-blocking operations
3. **Resilient:** Automatic retry on failures
4. **Auditable:** All events logged in Kafka
5. **Scalable:** Multiple consumers can process events
6. **Extensible:** Add new consumers without changing FSMs

**Kafka Topics:**

| Topic | Partitions | Key | Producers | Consumers |
|-------|-----------|-----|-----------|-----------|
| `order-events` | 32 | order_id | Order Service | Delivery, Notification, Analytics |
| `delivery-events` | 32 | delivery_id | Delivery Service | Order, Notification, Analytics |
| `assignment-requests` | 16 | restaurant_id | Delivery Service | Assignment Service |

---

### Decision 3: Customer Status Abstraction

**Status:** ✅ APPROVED

**Decision:** Map 22 internal FSM states to 8 customer-facing statuses.

**Problem:**

```
❌ BAD: Show raw FSM states to customer
"Your order is in PENDING_ACCEPTANCE state"
"Delivery is in SEARCHING_RIDER state"

Customer thinks: "What does that even mean?"
```

**Solution:**

```
✅ GOOD: Show simplified customer statuses
"Restaurant is preparing your food"
"Your delivery partner is on the way"

Customer thinks: "I know exactly what's happening!"
```

**Customer-Facing Statuses (8 states):**

1. 🔄 **ORDER_PLACED** - "Your order has been placed"
2. ✅ **ORDER_CONFIRMED** - "Restaurant is preparing your food"
3. 👨‍🍳 **PREPARING** - "Your food is being prepared"
4. 🏍️ **RIDER_ASSIGNED** - "Delivery partner assigned"
5. 📦 **READY_FOR_PICKUP** - "Food is ready, waiting for pickup"
6. 🚚 **OUT_FOR_DELIVERY** - "Your order is on the way!"
7. ✅ **DELIVERED** - "Order delivered. Enjoy your meal!"
8. ❌ **CANCELLED** - "Order cancelled"

**Benefits:**

- Reduced customer confusion (8 vs 22 states)
- Consistent experience across single/multi-restaurant orders
- Fewer push notifications (5-7 vs 20+)
- Easier to localize and customize messaging

---

### Decision 4: Smart Rider Assignment Timing

**Status:** ✅ APPROVED

**Decision:** Assign rider DURING food preparation (not after food is ready).

**Timing Strategy:**

```
Timeline Example:
─────────────────

12:30 PM - Order Confirmed
12:32 PM - Restaurant starts preparing (15 min estimated)
12:37 PM - 🎯 ASSIGN RIDER (5 min into prep, 10 min remaining)
           Rider is 5 min away from restaurant
12:42 PM - Rider reaches restaurant (food still cooking)
12:47 PM - Food ready, rider picks up immediately
12:47 PM - Out for delivery
1:00 PM  - Delivered

Result: Hot food, minimal rider wait, fast delivery
```

**Algorithm:**

```
Assignment Delay = Estimated Prep Time - Rider Travel Time - Buffer (2 min)

Constraints:
- Minimum delay: 2 minutes (don't assign too early)
- Maximum delay: 15 minutes (don't wait too long)
```

**Benefits:**

- ⚡ 5-10 minutes faster delivery
- 🔥 Hot food (picked up within 2 min of being ready)
- 📊 Better ETA accuracy
- 😊 Higher customer satisfaction
- ⏱️ Minimal rider wait time (< 3 min avg)

**Trade-offs:**

- ⚠️ Rider may wait if restaurant delays preparation
- ⚠️ Need to compensate rider for excessive waits (₹10 per 5 min)
- ⚠️ Requires accurate prep time estimation

---

### Decision 5: Multi-Restaurant Order Model

**Status:** ✅ APPROVED

**Decision:** Use Parent Order + Sub-Orders pattern.

**Structure:**

```
Customer Order (order_id: 12345)
├── Sub-Order 1: Restaurant A (sub_order_id: 12345-1)
│   ├── State: PREPARING
│   └── Items: 2x Samosa
├── Sub-Order 2: Restaurant B (sub_order_id: 12345-2)
│   ├── State: READY_FOR_PICKUP
│   └── Items: 1x Pizza
└── Sub-Order 3: Restaurant C (sub_order_id: 12345-3)
    ├── State: ACCEPTED
    └── Items: 1x Biryani

Parent Order State = f(Sub-Order States)
```

**State Aggregation:**

| Sub-Order States | Parent Order Status |
|------------------|---------------------|
| All PENDING | PENDING |
| Any ACCEPTED | ACCEPTED |
| All PREPARING | PREPARING |
| All READY_FOR_PICKUP | READY_FOR_PICKUP |
| All DELIVERED | DELIVERED |
| Any CANCELLED | PARTIALLY_CANCELLED |
| All CANCELLED | CANCELLED |

**Benefits:**

- ✅ Single payment transaction
- ✅ Unified customer view
- ✅ Independent restaurant workflows
- ✅ Flexible delivery strategies (single rider, multiple riders, batching)

---

### Decision 6: Hybrid Storage Strategy

**Status:** ✅ APPROVED

**Decision:** Use Redis for active state + PostgreSQL for persistence.

**Architecture:**

```
┌─────────────────────────────────────────────────────────────┐
│                 HYBRID STORAGE STRATEGY                     │
└─────────────────────────────────────────────────────────────┘

In-Memory (Redis)              Persistent (PostgreSQL)
─────────────────              ───────────────────────
• Current state                • Full state history
• Active orders only           • All orders (archive)
• TTL-based cleanup            • Audit trail
• Fast reads (<1ms)            • Compliance
• State transitions            • Analytics

Write Pattern: Dual-write (Redis + PostgreSQL)
Read Pattern: Redis first, fallback to PostgreSQL
```

**Benefits:**

- ⚡ Fast state reads (< 1ms from Redis)
- 💾 Durable persistence (PostgreSQL)
- 🔄 TTL-based auto-cleanup (Redis)
- 📊 Full audit trail (PostgreSQL)
- 🎯 Scalable (Redis cluster)

---

### Decision 7: Stateless FSM Instances

**Status:** ✅ APPROVED

**Decision:** Don't keep FSM instances in memory. Load state per transition.

**Pattern:**

```java
public void fireTransition(UUID orderId, OrderTrigger trigger) {
    // 1. Load current state from Redis/DB
    OrderState currentState = orderRepository.getState(orderId);
    
    // 2. Create FSM instance with current state
    StateMachine<OrderState, OrderTrigger> fsm = 
        new StateMachine<>(currentState, config);
    
    // 3. Fire trigger
    fsm.fire(trigger);
    
    // 4. Persist new state (dual-write)
    OrderState newState = fsm.getState();
    orderRepository.updateState(orderId, newState);
    redisCache.setState(orderId, newState);
    
    // 5. Publish event
    kafkaProducer.send("order-events", event);
}
```

**Benefits:**

- 🚀 Horizontal scalability (no session affinity)
- 💾 No memory overhead for inactive orders
- 🔄 Stateless services (easy to deploy/scale)
- 🎯 Load balancing across instances

---

## Technology Stack

| Component | Technology | Purpose | Justification |
|-----------|-----------|---------|---------------|
| **FSM Library** | Stateless4j | State machine implementation | Lightweight, Java-native, proven |
| **Message Bus** | Apache Kafka | Event streaming | High throughput, durable, scalable |
| **Cache** | Redis | Active state storage | Fast, TTL support, pub/sub |
| **Database** | PostgreSQL | Persistent storage | ACID, JSONB support, reliable |
| **API Framework** | Spring Boot | REST APIs | Standard, well-supported |
| **Monitoring** | Prometheus + Grafana | Metrics & dashboards | Industry standard |

---

## Implementation Sequence

### Phase 1: Foundation (Weeks 1-2)

**Stories:**
1. Set up Kafka topics and consumers
2. Implement hybrid storage (Redis + PostgreSQL)
3. Create base FSM framework with Stateless4j
4. Implement event publishing infrastructure

**Deliverable:** Infrastructure ready for FSM implementation

---

### Phase 2: Order FSM (Weeks 3-4)

**Stories:**
1. Implement Order FSM states and triggers
2. Add pre-acceptance validation logic
3. Implement timeout handling (Redis TTL)
4. Create Order REST APIs
5. Add unit tests for all transitions

**Deliverable:** Fully functional Order FSM

---

### Phase 3: Delivery FSM (Weeks 5-6)

**Stories:**
1. Implement Delivery FSM states and triggers
2. Build smart rider assignment algorithm
3. Add rider search and notification
4. Create Delivery REST APIs
5. Add unit tests for all transitions

**Deliverable:** Fully functional Delivery FSM

---

### Phase 4: Integration (Week 7)

**Stories:**
1. Implement event-driven integration between FSMs
2. Add customer status abstraction layer
3. Build status mapper service
4. Create customer status API

**Deliverable:** Integrated Order + Delivery system

---

### Phase 5: Multi-Restaurant Support (Week 8)

**Stories:**
1. Implement parent-child order model
2. Add sub-order state aggregation
3. Build delivery batching logic
4. Add multi-restaurant UI support

**Deliverable:** Multi-restaurant orders working

---

### Phase 6: Testing & Optimization (Weeks 9-10)

**Stories:**
1. End-to-end integration tests
2. Load testing (10K concurrent orders)
3. Performance optimization
4. Monitoring dashboards

**Deliverable:** Production-ready system

---

## Success Criteria

| Metric | Target | Measurement |
|--------|--------|-------------|
| **Order Processing Throughput** | 1,000+ orders/sec | Load test |
| **State Transition Latency** | < 50ms | Prometheus metrics |
| **Event Processing Latency** | < 100ms | Kafka lag monitoring |
| **Average Delivery Time** | < 30 minutes | Analytics dashboard |
| **Rider Wait Time** | < 3 minutes | Delivery metrics |
| **System Uptime** | > 99.9% | Monitoring |
| **Customer Satisfaction** | > 4.5/5 | Post-delivery survey |

---

## Next Steps

1. ✅ Review and approve this architecture document
2. 📝 Create detailed design documents for each FSM
3. 📋 Break down into user stories
4. 🏗️ Begin Phase 1 implementation

---

## References

- [Order FSM Design](./02_ORDER_FSM_DESIGN.md)
- [Delivery FSM Design](./03_DELIVERY_FSM_DESIGN.md)
- [Customer Status Design](./04_CUSTOMER_STATUS_DESIGN.md)
- [Multi-Restaurant Design](./05_MULTI_RESTAURANT_DESIGN.md)
- [Smart Assignment Algorithm](./06_SMART_ASSIGNMENT_ALGORITHM.md)
