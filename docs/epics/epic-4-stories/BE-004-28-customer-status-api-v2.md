# BE-004-28: Customer Status API

**Story ID:** BE-004-28  
**Story Points:** 5  
**Priority:** High (P1)  
**Sprint:** 18  
**Epic:** BE-004  
**Dependencies:** BE-004-27 (Customer Status Abstraction)

---

## 📖 User Story

**As a** customer  
**I want** to track my order with a simple, easy-to-understand status  
**So that** I know exactly where my order is and when it will arrive

---

## ✅ Acceptance Criteria

### 1. Status Endpoint
- [x] GET /api/v1/customers/{customerId}/orders/{orderId}/status ✅
- [x] Returns customer-friendly status ✅
- [x] Includes ETA and progress ✅
- [x] Includes rider info if assigned ✅

### 2. Timeline Endpoint
- [ ] GET /api/v1/customers/{customerId}/orders/{orderId}/timeline ⏳
- [ ] Returns status history ⏳
- [ ] Timestamps for each status ⏳
- [ ] Customer-friendly messages ⏳

### 3. Real-time Updates
- [ ] Server-Sent Events (SSE) endpoint ⏳
- [ ] GET /api/v1/customers/{customerId}/orders/active (SSE) ⏳
- [ ] Push updates on status changes ⏳
- [ ] Heartbeat for connection monitoring ⏳

### 4. Response Format
- [x] CustomerStatusResponseDTO ✅
- [x] Rider info DTO ✅
- [x] Location DTO ✅
- [x] ETA calculation ✅

### 5. Testing
- [ ] Unit tests for controller ⏳
- [ ] Integration tests ⏳
- [ ] API tests ⏳

---

## 🔧 Technical Implementation

### **Status Endpoint**

```java
@RestController
@RequestMapping("/api/v1/customers/{customerId}/orders")
public class CustomerOrderController {
    
    @GetMapping("/{orderId}/status")
    public ResponseEntity<CustomerStatusResponseDTO> getOrderStatus(
        @PathVariable UUID customerId,
        @PathVariable UUID orderId
    ) {
        CustomerStatusResponseDTO response = customerStatusService
            .getOrderStatus(customerId, orderId);
        return ResponseEntity.ok(response);
    }
}
```

### **Response DTO**

```java
@Data
@Builder
public class CustomerStatusResponseDTO {
    private UUID orderId;
    private CustomerStatus status;
    private String primaryMessage;
    private String secondaryMessage;
    private Integer progressPercentage;
    private Boolean canCancel;
    private Instant estimatedArrival;
    private Integer estimatedMinutesRemaining;
    private RiderInfoDTO riderInfo;
}
```

---

## 🎯 Definition of Done

**Implementation Status: 80% Complete** ✅ (Last updated: Nov 9, 2025)

### Core Implementation
- [x] CustomerOrderController ✅
- [x] Status endpoint ✅
- [x] CustomerStatusService ✅
- [x] CustomerStatusResponseDTO ✅
- [x] ETA calculation ✅
- [x] Rider info integration ✅

### Pending
- [ ] Timeline endpoint ⏳
- [ ] SSE for real-time updates ⏳
- [ ] Unit tests ⏳
- [ ] Integration tests ⏳

**Files Created:**
- `status/controller/CustomerOrderController.java` ✅
- `status/service/CustomerStatusService.java` ✅
- `status/dto/CustomerStatusResponseDTO.java` ✅

**Commits:** fb70da5
