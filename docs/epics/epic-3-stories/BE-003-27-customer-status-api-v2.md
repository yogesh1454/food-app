# BE-003-27: Customer Status Abstraction Layer

**Story ID:** BE-003-27  
**Story Points:** 5  
**Priority:** High (P1)  
**Sprint:** 19  
**Epic:** BE-003  
**Dependencies:** BE-003-26 (FSM Integration)

---

## 📖 User Story

**As a** mobile app developer  
**I want** a unified customer status API that abstracts Order and Delivery states  
**So that** customers see simple, user-friendly statuses

---

## ✅ Acceptance Criteria

1. **Status Mapper**
   - [ ] Map Order + Delivery states to 8 customer statuses
   - [ ] Handle edge cases (cancelled, failed, etc.)
   - [ ] Include ETA and progress percentage

2. **Customer Status API**
   - [ ] GET /api/v1/orders/{id}/status - Get customer-friendly status
   - [ ] Include rider info when assigned
   - [ ] Include real-time location when out for delivery

3. **Customer Statuses (8)**
   - PLACED, CONFIRMED, PREPARING, READY, ON_THE_WAY, NEARBY, DELIVERED, CANCELLED

---

## 🔧 Implementation

```java
@Service
public class CustomerStatusMapper {
    
    public CustomerStatus mapToCustomerStatus(Order order, Delivery delivery) {
        // Map complex FSM states to simple customer statuses
        if (order.getState() == OrderState.DELIVERED) {
            return CustomerStatus.DELIVERED;
        }
        if (delivery != null && delivery.getState() == DeliveryState.OUT_FOR_DELIVERY) {
            return CustomerStatus.ON_THE_WAY;
        }
        // ... more mappings
    }
}
```

---

## 🎯 Definition of Done

- [ ] StatusMapper implemented
- [ ] Customer Status API working
- [ ] Tests passing
- [ ] Documentation updated
