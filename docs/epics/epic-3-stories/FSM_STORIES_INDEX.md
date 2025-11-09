# Order & Delivery FSM - User Stories Index

**Epic:** BE-003 - Order & Delivery Management  
**Total Stories:** 20 (Planned)  
**Status:** In Progress

---

## 📋 Story Creation Progress

### ✅ Phase 1: Foundation (Weeks 1-2) - CREATED

| Story ID | Title | Points | Status | Notes |
|----------|-------|--------|--------|-------|
| BE-003-14 | Kafka Topics Setup for Order & Delivery FSM | 5 | ✅ Created | Event-driven integration |
| BE-003-15 | Redis State Cache for FSM | 5 | ✅ Created | State caching & timeouts |
| BE-003-16 | PostgreSQL Schema for FSM | 8 | ✅ Created | **Drop & recreate - clean slate** |
| BE-003-17 | Base FSM Framework with Stateless4j | 8 | ✅ Created | Reusable FSM base classes |

**Total Points:** 26

**⚠️ Important Note on BE-003-16:**
- Uses **DROP and CREATE** approach (clean slate)
- Existing tables are NOT in use, safe to drop
- Creates new FSM-ready schema with UUID keys
- Single migration script: V7__drop_and_recreate_orders_for_fsm.sql
- No backward compatibility concerns

---

### ✅ Phase 2: Order FSM (Weeks 3-4) - CREATED

| Story ID | Title | Points | Status | Notes |
|----------|-------|--------|--------|-------|
| BE-003-18 | Order FSM Implementation | 13 | ✅ Created | 13 states, 12 triggers, Stateless4j |
| BE-003-19 | Order Validation & Pre-Acceptance Logic | 8 | ✅ Created | Restaurant, menu, delivery zone validation |
| BE-003-20 | Order Timeout Handling (Restaurant Acceptance) | 5 | ✅ Created | Redis TTL, 2-minute timeout |
| BE-003-21 | Order Management APIs | 8 | ✅ Created | Customer, restaurant, rider APIs |

**Total Points:** 34

---

### ⏳ Phase 3: Delivery FSM (Weeks 5-6) - PENDING

| Story ID | Title | Points | Status |
|----------|-------|--------|--------|
| BE-003-22 | Delivery FSM Implementation | 13 | 🔄 To Create |
| BE-003-23 | Smart Rider Assignment Algorithm | 13 | 🔄 To Create |
| BE-003-24 | Rider Ranking & Search Service | 8 | 🔄 To Create |
| BE-003-25 | Delivery Management APIs | 8 | 🔄 To Create |

**Total Points:** 42

---

### ⏳ Phase 4: Integration (Week 7) - PENDING

| Story ID | Title | Points | Status |
|----------|-------|--------|--------|
| BE-003-26 | Event-Driven Integration (Order ↔ Delivery) | 8 | 🔄 To Create |
| BE-003-27 | Customer Status Abstraction Layer | 8 | 🔄 To Create |
| BE-003-28 | Customer Status API | 5 | 🔄 To Create |
| BE-003-29 | Push Notification Service Integration | 5 | 🔄 To Create |

**Total Points:** 26

---

### ⏳ Phase 5: Multi-Restaurant Support (Week 8) - PENDING

| Story ID | Title | Points | Status |
|----------|-------|--------|--------|
| BE-003-30 | Parent-Child Order Model | 8 | 🔄 To Create |
| BE-003-31 | Sub-Order State Aggregation | 5 | 🔄 To Create |
| BE-003-32 | Delivery Batching Algorithm | 13 | 🔄 To Create |
| BE-003-33 | Payment Distribution Service | 8 | 🔄 To Create |

**Total Points:** 34

---

### ⏳ Phase 6: Testing & Optimization (Weeks 9-10) - PENDING

| Story ID | Title | Points | Status |
|----------|-------|--------|--------|
| BE-003-34 | End-to-End Integration Tests | 8 | 🔄 To Create |
| BE-003-35 | Load Testing & Performance Optimization | 13 | 🔄 To Create |
| BE-003-36 | Monitoring & Alerting Setup | 5 | 🔄 To Create |
| BE-003-37 | Edge Case Handling & Resilience | 8 | 🔄 To Create |

**Total Points:** 34

---

## 📊 Summary

| Phase | Stories | Points | Status |
|-------|---------|--------|--------|
| Phase 1: Foundation | 4 | 26 | ✅ Created |
| Phase 2: Order FSM | 4 | 34 | ✅ Created |
| Phase 3: Delivery FSM | 4 | 42 | ⏳ Pending |
| Phase 4: Integration | 4 | 26 | ⏳ Pending |
| Phase 5: Multi-Restaurant | 4 | 34 | ⏳ Pending |
| Phase 6: Testing | 4 | 34 | ⏳ Pending |
| **TOTAL** | **24** | **196** | **33% Complete** |

---

## 🎯 Next Steps

1. ✅ Complete Phase 1 story creation (DONE)
2. ✅ Create Phase 2 stories (Order FSM) (DONE)
3. ⏳ Create Phase 3 stories (Delivery FSM) - NEXT
4. ⏳ Create Phase 4 stories (Integration)
5. ⏳ Create Phase 5 stories (Multi-Restaurant)
6. ⏳ Create Phase 6 stories (Testing & Optimization)

---

## 📚 Reference Documents

- [FSM Index](../../business-flows/00_ORDER_DELIVERY_FSM_INDEX.md)
- [Architecture Decisions](../../business-flows/01_ARCHITECTURE_DECISIONS.md)
- [Order FSM Design](../../business-flows/02_ORDER_FSM_DESIGN.md)
- [Delivery FSM Design](../../business-flows/03_DELIVERY_FSM_DESIGN.md)
- [Customer Status Design](../../business-flows/04_CUSTOMER_STATUS_DESIGN.md)
- [Multi-Restaurant Design](../../business-flows/05_MULTI_RESTAURANT_DESIGN.md)
- [Smart Assignment Algorithm](../../business-flows/06_SMART_ASSIGNMENT_ALGORITHM.md)

---

## 📝 Notes

- Each story follows the established template from existing Epic 3 stories
- All stories reference REST API Standards for implementation
- Stories are sized based on complexity and dependencies
- Total epic size: ~196 story points (~10 weeks for a team of 3-4 developers)
