# BE-003-29: Multi-Restaurant Order Support

**Story ID:** BE-003-29  
**Story Points:** 13  
**Priority:** Medium (P2)  
**Sprint:** 20  
**Epic:** BE-003  
**Dependencies:** BE-003-26 (FSM Integration)

---

## 📖 User Story

**As a** customer  
**I want** to order from multiple restaurants in one order  
**So that** I can get different items from different places

---

## ✅ Acceptance Criteria

1. **Parent-Child Order Model**
   - [ ] Parent order with multiple sub-orders
   - [ ] Each sub-order for one restaurant
   - [ ] Independent FSM for each sub-order
   - [ ] Aggregate parent order state

2. **State Aggregation**
   - [ ] Parent state = aggregate of sub-order states
   - [ ] Handle partial cancellations
   - [ ] Handle partial failures

3. **Delivery Batching**
   - [ ] Single rider for all sub-orders if possible
   - [ ] Multiple riders if restaurants far apart
   - [ ] Optimize delivery route

4. **Payment Distribution**
   - [ ] Split payment across restaurants
   - [ ] Handle delivery fees
   - [ ] Handle refunds for partial cancellations

---

## 🎯 Definition of Done

- [ ] Parent-child model implemented
- [ ] State aggregation working
- [ ] Delivery batching working
- [ ] Payment distribution working
- [ ] Tests passing
