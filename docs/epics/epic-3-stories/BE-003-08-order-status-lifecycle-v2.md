# BE-003-08: Order Status Lifecycle Management

**Story ID:** BE-003-08  
**Story Points:** 8  
**Priority:** Critical (P0)  
**Sprint:** 7  
**Epic:** BE-003  
**Dependencies:** BE-003-07 (Order Creation)

---

## 📖 User Story

**As a** vendor/delivery partner  
**I want** to update order status through its lifecycle  
**So that** customers can track their orders in real-time

---

## ✅ Acceptance Criteria

1. **Status Transitions**
   - [ ] PENDING → ACCEPTED → PREPARING → READY_FOR_PICKUP → ON_THE_WAY → DELIVERED
   - [ ] PENDING → REJECTED (vendor can reject)
   - [ ] Any status → CANCELLED (customer can cancel before PREPARING)
   - [ ] Invalid transitions prevented
   - [ ] Status change timestamps tracked

2. **Authorization**
   - [ ] Vendor can: ACCEPT, REJECT, mark as PREPARING, READY_FOR_PICKUP
   - [ ] Delivery partner can: mark as ON_THE_WAY, DELIVERED
   - [ ] Customer can: CANCEL (before PREPARING)
   - [ ] Admin can: any status change

3. **Event Publishing**
   - [ ] order.status.changed event on each transition
   - [ ] Consumed by Notification service for alerts
   - [ ] Consumed by Delivery service for assignment

4. **API Endpoints**
   - [ ] PUT /api/v1/orders/{orderId}/status - Update status
   - [ ] GET /api/v1/orders/{orderId}/history - Status history

---

## 🔧 Technical Implementation

### **Status Transition Validation**
```java
@Service
@Slf4j
@Transactional
public class OrderStatusService {
    
    private static final Map<String, List<String>> VALID_TRANSITIONS = Map.ofEntries(
        Map.entry("PENDING", List.of("ACCEPTED", "REJECTED", "CANCELLED")),
        Map.entry("ACCEPTED", List.of("PREPARING", "CANCELLED")),
        Map.entry("PREPARING", List.of("READY_FOR_PICKUP")),
        Map.entry("READY_FOR_PICKUP", List.of("ON_THE_WAY")),
        Map.entry("ON_THE_WAY", List.of("DELIVERED")),
        Map.entry("REJECTED", List.of()),
        Map.entry("CANCELLED", List.of()),
        Map.entry("DELIVERED", List.of())
    );
    
    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;
    
    public OrderResponse updateOrderStatus(UUID orderId, 
                                          String newStatus, 
                                          UUID userId,
                                          String userRole) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        
        // Validate transition
        if (!isValidTransition(order.getOrderStatus(), newStatus)) {
            throw new InvalidStatusTransitionException(
                "Cannot transition from " + order.getOrderStatus() + " to " + newStatus);
        }
        
        // Validate authorization
        validateAuthorization(order, newStatus, userId, userRole);
        
        String oldStatus = order.getOrderStatus();
        order.setOrderStatus(newStatus);
        
        if (newStatus.equals("DELIVERED")) {
            order.setDeliveredAt(LocalDateTime.now());
        }
        
        Order updatedOrder = orderRepository.save(order);
        
        // Publish event
        eventPublisher.publishOrderStatusChanged(updatedOrder, oldStatus, newStatus);
        
        log.info("Order status updated: {} from {} to {}", orderId, oldStatus, newStatus);
        return OrderMapper.toResponse(updatedOrder);
    }
    
    private boolean isValidTransition(String current, String next) {
        return VALID_TRANSITIONS.getOrDefault(current, List.of()).contains(next);
    }
    
    private void validateAuthorization(Order order, String newStatus, UUID userId, String userRole) {
        switch (newStatus) {
            case "ACCEPTED":
            case "REJECTED":
            case "PREPARING":
            case "READY_FOR_PICKUP":
                // Only vendor can perform these
                if (!order.getBranch().getVendor().getUserId().equals(userId)) {
                    throw new UnauthorizedException("Only vendor can perform this action");
                }
                break;
            case "ON_THE_WAY":
            case "DELIVERED":
                // Only delivery partner can perform these
                if (!userRole.equals("DELIVERY_PARTNER")) {
                    throw new UnauthorizedException("Only delivery partner can perform this action");
                }
                break;
            case "CANCELLED":
                // Customer can cancel before PREPARING
                if (!order.getCustomerId().equals(userId)) {
                    throw new UnauthorizedException("Only customer can cancel");
                }
                if (order.getOrderStatus().equals("PREPARING")) {
                    throw new ValidationException("Cannot cancel order being prepared");
                }
                break;
        }
    }
}
```

### **Controller**
```java
@RestController
@RequestMapping("/api/v1/orders")
@Slf4j
public class OrderStatusController {
    
    private final OrderStatusService orderStatusService;
    
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestParam String status,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        OrderResponse response = orderStatusService.updateOrderStatus(
            orderId, status, userPrincipal.getUserId(), userPrincipal.getRole());
        
        return ResponseEntity.ok(ApiResponse.success(response, "Order status updated"));
    }
}
```

---

## 📋 Definition of Done

- [ ] Status lifecycle implemented
- [ ] Transition validation working
- [ ] Authorization checks in place
- [ ] Events published
- [ ] Unit tests passing
- [ ] Integration tests passing
- [ ] Code reviewed

---

## 📚 References

- [Database Schema](/docs/architecture/9-database-schema.md)
