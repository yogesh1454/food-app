# BE-004-26: Event-Driven FSM Integration

**Story ID:** BE-004-26  
**Story Points:** 8  
**Priority:** Critical (P0)  
**Sprint:** 18  
**Epic:** BE-004  
**Dependencies:** BE-003-22 (Delivery FSM), BE-003-18 (Order FSM)

---

## 📖 User Story

**As a** backend developer  
**I want** to integrate Order FSM and Delivery FSM through Kafka events  
**So that** both FSMs can coordinate seamlessly without tight coupling

---

## ✅ Acceptance Criteria

### 1. Event Consumers
- [ ] OrderEventConsumer listens to `order-events` topic
- [ ] DeliveryEventConsumer listens to `delivery-events` topic
- [ ] Manual commit for exactly-once processing
- [ ] Error handling with DLQ

### 2. Order → Delivery Integration
- [ ] `order.ready_for_pickup` event creates delivery
- [ ] Delivery FSM triggered with FIND_RIDERS
- [ ] Idempotency key prevents duplicate deliveries

### 3. Delivery → Order Integration
- [ ] `delivery.rider_accepted` → Order FSM: ASSIGN_RIDER
- [ ] `delivery.picked_up` → Order FSM: RIDER_PICKUP
- [ ] `delivery.delivered` → Order FSM: DELIVER_ORDER
- [ ] `delivery.failed` → Order FSM: CANCEL_ORDER

### 4. Event Schema
- [ ] DeliveryCreatedEvent
- [ ] DeliveryStateChangedEvent
- [ ] OrderStateChangedEvent (enhanced)
- [ ] Schema validation

### 5. Idempotency
- [ ] Idempotency keys in events
- [ ] Duplicate detection
- [ ] Idempotent event processing

### 6. Testing
- [ ] Unit tests for event consumers
- [ ] Integration tests for FSM coordination
- [ ] DLQ handling tests

---

## 🔧 Technical Implementation

### **Event Schemas**

```java
@Data
@Builder
public class DeliveryCreatedEvent {
    private UUID deliveryId;
    private UUID orderId;
    private UUID idempotencyKey;
    private Instant timestamp;
}

@Data
@Builder
public class DeliveryStateChangedEvent {
    private UUID deliveryId;
    private UUID orderId;
    private DeliveryState fromState;
    private DeliveryState toState;
    private UUID riderId;
    private UUID idempotencyKey;
    private Instant timestamp;
}
```

### **Order Event Consumer**

```java
@Service
@Slf4j
public class OrderEventConsumer {
    
    @KafkaListener(topics = "order-events", groupId = "delivery-service")
    public void handleOrderEvent(OrderStateChangedEvent event) {
        if (event.getToState() == OrderState.READY_FOR_PICKUP) {
            // Create delivery
            deliveryService.createDeliveryForOrder(event.getOrderId());
        }
    }
}
```

### **Delivery Event Consumer**

```java
@Service
@Slf4j
public class DeliveryEventConsumer {
    
    @KafkaListener(topics = "delivery-events", groupId = "order-service")
    public void handleDeliveryEvent(DeliveryStateChangedEvent event) {
        switch (event.getToState()) {
            case RIDER_ACCEPTED:
                orderFSM.fire(event.getOrderId(), OrderTrigger.ASSIGN_RIDER);
                break;
            case PICKED_UP:
                orderFSM.fire(event.getOrderId(), OrderTrigger.RIDER_PICKUP);
                break;
            case DELIVERED:
                orderFSM.fire(event.getOrderId(), OrderTrigger.DELIVER_ORDER);
                break;
            case FAILED:
                orderFSM.fire(event.getOrderId(), OrderTrigger.CANCEL);
                break;
        }
    }
}
```

---

## 🎯 Definition of Done

**Implementation Status: 0% Complete** ⏳ (Last updated: Nov 9, 2025)

### Pending Implementation
- [ ] OrderEventConsumer ⏳
- [ ] DeliveryEventConsumer ⏳
- [ ] Event schema classes ⏳
- [ ] Idempotency handling ⏳
- [ ] DLQ configuration ⏳
- [ ] Integration tests ⏳

**Dependencies:**
- Requires BE-003-22 (Delivery FSM) ✅
- Requires BE-003-18 (Order FSM) ✅
- Requires BE-003-14 (Kafka Topics) ✅
