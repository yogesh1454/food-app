# Design Documents Changelog

**Date:** November 9, 2025  
**Version:** 1.1

---

## Updates Made

### 1. ✅ 02_ORDER_FSM_DESIGN.md - Added Order Management APIs

**Changes:**
- Added new section: "Order Management APIs"
- Updated Table of Contents

**New Content:**
1. **Place Order API** (`POST /api/v1/orders`)
   - Supports single and multi-restaurant orders
   - Complete request/response schemas
   - Error handling examples
   - FSM flow documentation

2. **Get Order Status API** (`GET /api/v1/orders/{orderId}`)
   - Returns current order state and timeline
   - Includes customer-facing status

3. **Cancel Order API** (`POST /api/v1/orders/{orderId}/cancel`)
   - Handles cancellation with refund calculation
   - State validation

4. **Update Order API** (`PATCH /api/v1/orders/{orderId}/restaurant-action`)
   - Restaurant actions: accept, reject, mark ready
   - Estimated prep time updates

**Implementation Notes Added:**
- Order creation flow with Java example
- Validation checklist
- Payment processing reference to Multi-Restaurant design

---

### 2. ✅ 04_CUSTOMER_STATUS_DESIGN.md - Clarified Backend/Frontend Responsibilities

**Changes:**
- Added prominent note before "UI Design Patterns" section

**Clarification Added:**

```
⚠️ Important: Backend/Frontend Responsibility

Backend Responsibility:
- Provide structured data via API
- Calculate progress percentage and current step
- Aggregate multi-restaurant statuses

Frontend Responsibility:
- Choose and implement UI pattern
- Handle responsive design and animations
- Localization and formatting
```

**Impact:**
- Clear separation of concerns
- Backend provides data, frontend chooses presentation
- UI patterns remain as reference examples for frontend team

---

### 3. ✅ 05_MULTI_RESTAURANT_DESIGN.md - Clarified Payment Handling Scope

**Changes:**
- Added scope clarification note at the beginning of "Payment Handling" section

**Clarification Added:**

```
📌 Scope Clarification

This section covers POST-ORDER payment operations:
✓ Payment Distribution (to restaurants, riders, platform)
✓ Partial Refunds (when sub-orders are cancelled)

NOT covered here:
✗ Initial Payment Collection (see Order FSM Design)
✗ Payment Gateway Integration (separate documentation)
```

**Impact:**
- Clear understanding that this section is about distribution, not collection
- Prevents confusion about where initial payment is handled
- Explains why multi-restaurant orders need special payment handling

---

## Summary of Clarifications

| Question | Resolution | Document Updated |
|----------|-----------|------------------|
| **1. Progress Bar UI** | Backend provides data only; frontend chooses UI pattern | 04_CUSTOMER_STATUS_DESIGN.md |
| **2. Place Order API** | Added comprehensive API section to Order FSM document | 02_ORDER_FSM_DESIGN.md |
| **3. Payment Handling** | Clarified scope: distribution/refunds, not initial collection | 05_MULTI_RESTAURANT_DESIGN.md |

---

## Documents Ready for Story Creation

All design documents are now updated and ready for user story creation:

1. ✅ 00_ORDER_DELIVERY_FSM_INDEX.md
2. ✅ 01_ARCHITECTURE_DECISIONS.md
3. ✅ 02_ORDER_FSM_DESIGN.md (Updated)
4. ✅ 03_DELIVERY_FSM_DESIGN.md
5. ✅ 04_CUSTOMER_STATUS_DESIGN.md (Updated)
6. ✅ 05_MULTI_RESTAURANT_DESIGN.md (Updated)
7. ✅ 06_SMART_ASSIGNMENT_ALGORITHM.md

---

## Next Steps

1. ✅ Review updated documents
2. ⏳ Approve for story creation
3. ⏳ Create Epic-4 in `docs/epics/`
4. ⏳ Create user stories in `docs/epics/epic-4-stories/`
5. ⏳ Begin Phase 1 implementation
