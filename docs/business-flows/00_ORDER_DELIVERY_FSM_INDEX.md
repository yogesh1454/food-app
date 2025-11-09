# Order & Delivery FSM - Complete Design Documentation

**Document Version:** 1.0  
**Last Updated:** November 9, 2025  
**Epic:** Epic-4 - Order & Delivery Management System  
**Status:** Ready for Review

---

## 📋 Documentation Overview

This is the master index for the Order & Delivery Finite State Machine (FSM) design documentation. The design has been split into **6 focused documents** to facilitate incremental story creation and implementation.

---

## 📚 Document Structure

### 1. [Architecture Decisions](./01_ARCHITECTURE_DECISIONS.md)
**Purpose:** Foundational architecture decisions and rationale

**Contents:**
- Dual-FSM architecture (Order + Delivery)
- Event-driven integration via Kafka
- Customer status abstraction
- Smart rider assignment timing
- Multi-restaurant order model
- Hybrid storage strategy (Redis + PostgreSQL)
- Stateless FSM instances
- Technology stack
- Implementation sequence (6 phases)

**Key Decisions:**
- ✅ Separate Order & Delivery FSMs
- ✅ Event-driven communication
- ✅ 8 customer-facing statuses (vs 22 internal)
- ✅ Assign rider during preparation
- ✅ Parent-child order model

**Stories to Create:**
- Infrastructure setup (Kafka, Redis, PostgreSQL)
- Base FSM framework with Stateless4j
- Event publishing infrastructure

---

### 2. [Order FSM Design](./02_ORDER_FSM_DESIGN.md)
**Purpose:** Detailed design of the Order state machine

**Contents:**
- 13 states with definitions
- 12 triggers and transitions
- Side effects for each trigger
- Timeout handling (Redis TTL)
- Validation rules
- Stateless4j configuration
- Integration points with Delivery FSM

**States:**
1. CREATED
2. VALIDATED
3. PAYMENT_CONFIRMED
4. PENDING_ACCEPTANCE
5. ACCEPTED
6. PREPARING
7. READY_FOR_PICKUP
8. ASSIGNED_TO_RIDER
9. PICKED_UP
10. DELIVERED
11. CLOSED
12. CANCELLED
13. REJECTED

**Stories to Create:**
- Order FSM implementation
- Pre-acceptance validation
- Timeout handling (2 min restaurant acceptance)
- Order REST APIs
- Unit tests for all transitions

---

### 3. [Delivery FSM Design](./03_DELIVERY_FSM_DESIGN.md)
**Purpose:** Detailed design of the Delivery state machine

**Contents:**
- 9 states with definitions
- 10 triggers and transitions
- Rider assignment algorithm
- Integration with Order FSM
- Kafka event schemas
- Stateless4j configuration

**States:**
1. PENDING
2. SEARCHING_RIDER
3. RIDER_ASSIGNED
4. RIDER_ACCEPTED
5. AT_RESTAURANT
6. PICKED_UP
7. OUT_FOR_DELIVERY
8. DELIVERED
9. FAILED

**Stories to Create:**
- Delivery FSM implementation
- Rider search and notification
- Location tracking
- Delivery REST APIs
- Unit tests for all transitions

---

### 4. [Customer Status Design](./04_CUSTOMER_STATUS_DESIGN.md)
**Purpose:** Customer-facing status abstraction layer

**Contents:**
- 8 customer-facing statuses
- Status mapping logic (22 internal → 8 customer)
- UI design patterns (timeline, progress bar, card)
- Notification strategy (5-7 notifications per order)
- Multi-restaurant status display
- Customer status API design
- Implementation guide

**Customer Statuses:**
1. 🔄 ORDER_PLACED
2. ✅ ORDER_CONFIRMED
3. 👨‍🍳 PREPARING
4. 🏍️ RIDER_ASSIGNED
5. 📦 READY_FOR_PICKUP
6. 🚚 OUT_FOR_DELIVERY
7. ✅ DELIVERED
8. ❌ CANCELLED

**Stories to Create:**
- StatusMapper service
- Customer status API
- Push notification service
- UI components (timeline, progress bar)
- Event listener for status changes

---

### 5. [Multi-Restaurant Design](./05_MULTI_RESTAURANT_DESIGN.md)
**Purpose:** Handling orders from multiple restaurants

**Contents:**
- Parent-child order model
- State aggregation logic
- 3 delivery strategies (sequential, parallel, batching)
- Payment handling and distribution
- Refund handling (full, partial)
- Database schema
- Batching algorithm

**Delivery Strategies:**
1. **Sequential Pickup** - Single rider, multiple restaurants
2. **Parallel Delivery** - Multiple riders, fastest delivery
3. **Intelligent Batching** - Optimized cost/speed balance ✅ RECOMMENDED

**Stories to Create:**
- Parent-child order model
- Sub-order state aggregation
- Delivery batching algorithm
- Payment distribution service
- Multi-restaurant UI

---

### 6. [Smart Assignment Algorithm](./06_SMART_ASSIGNMENT_ALGORITHM.md)
**Purpose:** Intelligent rider assignment during food preparation

**Contents:**
- Assignment timing strategy
- Rider ranking algorithm (5 factors)
- Dynamic pricing & surge
- Edge case handling (4 scenarios)
- Performance optimization
- Redis-based scheduling

**Key Features:**
- Assign rider 5-10 min into preparation
- Rank riders by distance, rating, acceptance rate, load, activity
- Surge pricing (1.0x - 2.5x)
- Handle early food ready, early rider arrival, delays, no riders

**Stories to Create:**
- Smart assignment service
- Rider ranking algorithm
- Surge pricing service
- Redis keyspace notification listener
- Edge case handlers
- Performance monitoring

---

## 🎯 Implementation Roadmap

### Phase 1: Foundation (Weeks 1-2)
**Goal:** Infrastructure ready for FSM implementation

**Stories:**
1. Set up Kafka topics (order-events, delivery-events, assignment-requests)
2. Configure Redis for state caching and TTL
3. Set up PostgreSQL schema (orders, deliveries, sub_orders)
4. Implement base FSM framework with Stateless4j
5. Create event publishing infrastructure

**Deliverable:** Infrastructure ready

---

### Phase 2: Order FSM (Weeks 3-4)
**Goal:** Fully functional Order FSM

**Stories:**
1. Implement Order FSM states and triggers
2. Add pre-acceptance validation logic
3. Implement timeout handling (Redis TTL for 2 min acceptance)
4. Create Order REST APIs (create, get, update, cancel)
5. Add unit tests for all transitions
6. Integration tests for validation flow

**Deliverable:** Order FSM working end-to-end

---

### Phase 3: Delivery FSM (Weeks 5-6)
**Goal:** Fully functional Delivery FSM

**Stories:**
1. Implement Delivery FSM states and triggers
2. Build smart rider assignment algorithm
3. Implement rider ranking service
4. Add rider search and notification
5. Create Delivery REST APIs
6. Add unit tests for all transitions

**Deliverable:** Delivery FSM working end-to-end

---

### Phase 4: Integration (Week 7)
**Goal:** Integrated Order + Delivery system

**Stories:**
1. Implement event-driven integration between FSMs
2. Add customer status abstraction layer
3. Build StatusMapper service
4. Create customer status API
5. Implement push notification service
6. Integration tests for complete flow

**Deliverable:** Integrated system with customer-facing status

---

### Phase 5: Multi-Restaurant Support (Week 8)
**Goal:** Multi-restaurant orders working

**Stories:**
1. Implement parent-child order model
2. Add sub-order state aggregation
3. Build delivery batching algorithm
4. Implement payment distribution
5. Add multi-restaurant UI support
6. Integration tests for multi-restaurant flow

**Deliverable:** Multi-restaurant orders functional

---

### Phase 6: Testing & Optimization (Weeks 9-10)
**Goal:** Production-ready system

**Stories:**
1. End-to-end integration tests
2. Load testing (10K concurrent orders)
3. Performance optimization (caching, indexing)
4. Monitoring dashboards (Prometheus + Grafana)
5. Edge case handling (delays, no riders, cancellations)
6. Documentation and runbooks

**Deliverable:** Production-ready system

---

## 📊 Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| **Order Processing Throughput** | 1,000+ orders/sec | Load test |
| **State Transition Latency** | < 50ms | Prometheus metrics |
| **Event Processing Latency** | < 100ms | Kafka lag monitoring |
| **Average Delivery Time** | < 30 minutes | Analytics dashboard |
| **Rider Wait Time** | < 3 minutes | Delivery metrics |
| **System Uptime** | > 99.9% | Monitoring |
| **Customer Satisfaction** | > 4.5/5 | Post-delivery survey |
| **Hot Food Delivery** | > 95% | Temperature tracking |

---

## 🔗 Quick Links

- [Architecture Decisions](./01_ARCHITECTURE_DECISIONS.md)
- [Order FSM Design](./02_ORDER_FSM_DESIGN.md)
- [Delivery FSM Design](./03_DELIVERY_FSM_DESIGN.md)
- [Customer Status Design](./04_CUSTOMER_STATUS_DESIGN.md)
- [Multi-Restaurant Design](./05_MULTI_RESTAURANT_DESIGN.md)
- [Smart Assignment Algorithm](./06_SMART_ASSIGNMENT_ALGORITHM.md)

---

## 📝 Next Steps

### For Review:
1. ✅ Review all 6 design documents
2. ✅ Validate architecture decisions
3. ✅ Confirm implementation sequence
4. ✅ Approve for story creation

### After Approval:
1. Create Epic-4 in `docs/epics/`
2. Create user stories in `docs/epics/epic-4-stories/`
3. Break down stories into tasks
4. Begin Phase 1 implementation

---

## 📞 Questions & Clarifications

If you have any questions or need clarifications on any aspect of the design, please refer to the specific document or reach out to the team.

---

**Document Status:** ✅ Ready for Review  
**Awaiting:** User approval to proceed with story creation
