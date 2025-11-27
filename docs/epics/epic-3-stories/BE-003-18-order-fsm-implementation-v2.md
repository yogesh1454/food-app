# BE-003-18: Order FSM Implementation

**Story ID:** BE-003-18  
**Story Points:** 13  
**Priority:** Critical (P0)  
**Sprint:** 15  
**Epic:** BE-003  
**Dependencies:** BE-003-16 (PostgreSQL Schema), BE-003-17 (Base FSM Framework)

---

## 📖 User Story

**As a** backend developer  
**I want** to implement the Order FSM with all 13 states and transitions  
**So that** the system can manage the complete order lifecycle from creation to delivery

---

## ✅ Acceptance Criteria

1. **Order FSM States (13)**
   - [ ] All 13 states implemented (CREATED, VALIDATED, PAYMENT_CONFIRMED, etc.)
   - [ ] State enum defined with proper naming
   - [ ] State descriptions documented

2. **Order FSM Triggers (12)**
   - [ ] All 12 triggers implemented
   - [ ] Trigger validation logic
   - [ ] Invalid trigger handling

3. **State Transitions**
   - [ ] All valid transitions configured
   - [ ] Invalid transitions blocked
   - [ ] Transition guards implemented
   - [ ] Side effects executed on transitions

4. **FSM Configuration**
   - [ ] Stateless4j configuration complete
   - [ ] Entry/exit actions defined
   - [ ] Transition callbacks implemented

5. **State Persistence**
   - [ ] State saved to database on transitions
   - [ ] State cached in Redis
   - [ ] Audit trail recorded
   - [ ] Events published to Kafka

6. **Order Service**
   - [ ] OrderFSM service implemented
   - [ ] State transition methods
   - [ ] State query methods
   - [ ] Error handling

---

## 🔧 Technical Implementation

### **Order State Enum**

```java
public enum OrderState {
    CREATED("Order created, awaiting validation"),
    VALIDATED("Order validated, awaiting payment"),
    PAYMENT_CONFIRMED("Payment confirmed, notifying restaurant"),
    PENDING_ACCEPTANCE("Waiting for restaurant acceptance"),
    ACCEPTED("Restaurant accepted, preparing to start"),
    PREPARING("Food being prepared"),
    READY_FOR_PICKUP("Food ready, awaiting rider pickup"),
    ASSIGNED_TO_RIDER("Assigned to delivery rider"),
    PICKED_UP("Rider picked up the order"),
    DELIVERED("Order delivered to customer"),
    CLOSED("Order closed successfully"),
    CANCELLED("Order cancelled"),
    REJECTED("Order rejected by restaurant");
    
    private final String description;
    
    OrderState(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
```

### **Order Trigger Enum**

```java
public enum OrderTrigger {
    VALIDATE_ORDER,
    CONFIRM_PAYMENT,
    NOTIFY_RESTAURANT,
    ACCEPT_ORDER,
    REJECT_ORDER,
    TIMEOUT_ACCEPTANCE,
    START_PREPARATION,
    MARK_READY,
    ASSIGN_RIDER,
    RIDER_PICKUP,
    DELIVER_ORDER,
    CLOSE_ORDER,
    CANCEL_ORDER
}
```

### **Order FSM Implementation**

```java
@Service
@Slf4j
public class OrderFSM extends BaseStateMachine<OrderState, OrderTrigger> {
    
    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;
    private final TimeoutService timeoutService;
    private final DeliveryService deliveryService;
    
    public OrderFSM(
        StateCacheService<OrderState> cacheService,
        StateAuditService auditService,
        EventPublisher eventPublisher,
        OrderRepository orderRepository,
        TimeoutService timeoutService,
        DeliveryService deliveryService
    ) {
        super(cacheService, auditService, eventPublisher);
        this.orderRepository = orderRepository;
        this.timeoutService = timeoutService;
        this.deliveryService = deliveryService;
    }
    
    @Override
    protected StateMachineConfig<OrderState, OrderTrigger> configure() {
        StateMachineConfig<OrderState, OrderTrigger> config = 
            new StateMachineConfig<>();
        
        // CREATED state transitions
        config.configure(OrderState.CREATED)
            .permit(OrderTrigger.VALIDATE_ORDER, OrderState.VALIDATED)
            .permit(OrderTrigger.CANCEL_ORDER, OrderState.CANCELLED);
        
        // VALIDATED state transitions
        config.configure(OrderState.VALIDATED)
            .permit(OrderTrigger.CONFIRM_PAYMENT, OrderState.PAYMENT_CONFIRMED)
            .permit(OrderTrigger.CANCEL_ORDER, OrderState.CANCELLED);
        
        // PAYMENT_CONFIRMED state transitions
        config.configure(OrderState.PAYMENT_CONFIRMED)
            .permit(OrderTrigger.NOTIFY_RESTAURANT, OrderState.PENDING_ACCEPTANCE)
            .permit(OrderTrigger.CANCEL_ORDER, OrderState.CANCELLED);
        
        // PENDING_ACCEPTANCE state transitions
        config.configure(OrderState.PENDING_ACCEPTANCE)
            .permit(OrderTrigger.ACCEPT_ORDER, OrderState.ACCEPTED)
            .permit(OrderTrigger.REJECT_ORDER, OrderState.REJECTED)
            .permit(OrderTrigger.TIMEOUT_ACCEPTANCE, OrderState.REJECTED)
            .permit(OrderTrigger.CANCEL_ORDER, OrderState.CANCELLED);
        
        // ACCEPTED state transitions
        config.configure(OrderState.ACCEPTED)
            .permit(OrderTrigger.START_PREPARATION, OrderState.PREPARING)
            .permit(OrderTrigger.CANCEL_ORDER, OrderState.CANCELLED);
        
        // PREPARING state transitions
        config.configure(OrderState.PREPARING)
            .permit(OrderTrigger.MARK_READY, OrderState.READY_FOR_PICKUP);
        
        // READY_FOR_PICKUP state transitions
        config.configure(OrderState.READY_FOR_PICKUP)
            .permit(OrderTrigger.ASSIGN_RIDER, OrderState.ASSIGNED_TO_RIDER);
        
        // ASSIGNED_TO_RIDER state transitions
        config.configure(OrderState.ASSIGNED_TO_RIDER)
            .permit(OrderTrigger.RIDER_PICKUP, OrderState.PICKED_UP);
        
        // PICKED_UP state transitions
        config.configure(OrderState.PICKED_UP)
            .permit(OrderTrigger.DELIVER_ORDER, OrderState.DELIVERED);
        
        // DELIVERED state transitions
        config.configure(OrderState.DELIVERED)
            .permit(OrderTrigger.CLOSE_ORDER, OrderState.CLOSED);
        
        // Configure entry actions
        config.configure(OrderState.PENDING_ACCEPTANCE)
            .onEntry(() -> scheduleAcceptanceTimeout());
        
        config.configure(OrderState.PREPARING)
            .onEntry(() -> scheduleRiderAssignment());
        
        config.configure(OrderState.READY_FOR_PICKUP)
            .onEntry(() -> notifyReadyForPickup());
        
        return config;
    }
    
    @Override
    protected OrderState loadStateFromDatabase(UUID orderId) {
        return orderRepository.findById(orderId)
            .map(Order::getState)
            .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
    }
    
    @Override
    protected void persistStateToDatabase(UUID orderId, OrderState state) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        
        order.setState(state);
        
        // Update state-specific timestamps
        updateTimestamps(order, state);
        
        orderRepository.save(order);
        
        log.info("Persisted order state: orderId={}, state={}", orderId, state);
    }
    
    @Override
    protected String getEntityType() {
        return "ORDER";
    }
    
    private void updateTimestamps(Order order, OrderState state) {
        Instant now = Instant.now();
        
        switch (state) {
            case VALIDATED:
                order.setValidatedAt(now);
                break;
            case PAYMENT_CONFIRMED:
                order.setPaymentConfirmedAt(now);
                break;
            case ACCEPTED:
                order.setAcceptedAt(now);
                break;
            case PREPARING:
                order.setPreparingStartedAt(now);
                break;
            case READY_FOR_PICKUP:
                order.setReadyAt(now);
                break;
            case PICKED_UP:
                order.setPickedUpAt(now);
                break;
            case DELIVERED:
                order.setDeliveredAt(now);
                break;
            case CANCELLED:
            case REJECTED:
                order.setCancelledAt(now);
                break;
        }
    }
    
    private void scheduleAcceptanceTimeout() {
        UUID orderId = getCurrentOrderId();
        timeoutService.scheduleTimeout(
            "restaurant_acceptance",
            orderId,
            Duration.ofMinutes(2)
        );
        log.info("Scheduled acceptance timeout: orderId={}", orderId);
    }
    
    private void scheduleRiderAssignment() {
        UUID orderId = getCurrentOrderId();
        Order order = orderRepository.findById(orderId).orElseThrow();
        
        deliveryService.scheduleSmartRiderAssignment(order);
        log.info("Scheduled smart rider assignment: orderId={}", orderId);
    }
    
    private void notifyReadyForPickup() {
        UUID orderId = getCurrentOrderId();
        eventPublisher.publishOrderReadyForPickup(orderId);
        log.info("Published ready for pickup event: orderId={}", orderId);
    }
    
    private UUID getCurrentOrderId() {
        // This would be set in thread-local context during FSM execution
        return OrderContext.getCurrentOrderId();
    }
}
```

### **Order Service**

```java
@Service
@Slf4j
@Transactional
public class OrderService {
    
    private final OrderFSM orderFSM;
    private final OrderRepository orderRepository;
    private final ValidationService validationService;
    private final PaymentService paymentService;
    
    public OrderResponse createOrder(CreateOrderRequest request, UUID customerId) {
        log.info("Creating order for customer: {}", customerId);
        
        // Create order entity
        Order order = buildOrder(request, customerId);
        order.setState(OrderState.CREATED);
        order = orderRepository.save(order);
        
        // Set context for FSM
        OrderContext.setCurrentOrderId(order.getOrderId());
        
        try {
            // Trigger FSM transitions
            orderFSM.fire(order.getOrderId(), OrderTrigger.VALIDATE_ORDER);
            orderFSM.fire(order.getOrderId(), OrderTrigger.CONFIRM_PAYMENT);
            orderFSM.fire(order.getOrderId(), OrderTrigger.NOTIFY_RESTAURANT);
            
            // Reload order with updated state
            order = orderRepository.findById(order.getOrderId()).orElseThrow();
            
            return OrderResponse.from(order);
            
        } finally {
            OrderContext.clear();
        }
    }
    
    public void acceptOrder(UUID orderId, AcceptOrderRequest request) {
        log.info("Accepting order: orderId={}", orderId);
        
        Order order = orderRepository.findById(orderId).orElseThrow();
        
        // Update estimated prep time
        order.setEstimatedPrepTimeMinutes(request.getEstimatedPrepTime());
        orderRepository.save(order);
        
        // Trigger FSM
        orderFSM.fire(orderId, OrderTrigger.ACCEPT_ORDER);
        orderFSM.fire(orderId, OrderTrigger.START_PREPARATION);
    }
    
    public void rejectOrder(UUID orderId, String reason) {
        log.info("Rejecting order: orderId={}, reason={}", orderId, reason);
        
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setCancellationReason(reason);
        orderRepository.save(order);
        
        orderFSM.fire(orderId, OrderTrigger.REJECT_ORDER);
    }
    
    public void markOrderReady(UUID orderId) {
        log.info("Marking order ready: orderId={}", orderId);
        orderFSM.fire(orderId, OrderTrigger.MARK_READY);
    }
    
    public void cancelOrder(UUID orderId, CancelOrderRequest request) {
        log.info("Cancelling order: orderId={}", orderId);
        
        Order order = orderRepository.findById(orderId).orElseThrow();
        
        // Check if cancellation is allowed
        if (!canCancelOrder(order.getState())) {
            throw new OrderCancellationNotAllowedException(
                "Order cannot be cancelled in state: " + order.getState()
            );
        }
        
        order.setCancellationReason(request.getReason());
        order.setCancelledBy(request.getCancelledBy());
        orderRepository.save(order);
        
        orderFSM.fire(orderId, OrderTrigger.CANCEL_ORDER);
    }
    
    private boolean canCancelOrder(OrderState state) {
        return state == OrderState.CREATED ||
               state == OrderState.VALIDATED ||
               state == OrderState.PAYMENT_CONFIRMED ||
               state == OrderState.PENDING_ACCEPTANCE ||
               state == OrderState.ACCEPTED;
    }
    
    private Order buildOrder(CreateOrderRequest request, UUID customerId) {
        // Build order entity from request
        // ... implementation
        return new Order();
    }
}
```

---

## 📋 Testing Requirements

### **Unit Tests**
- [ ] Test all 13 state transitions
- [ ] Test invalid transitions are blocked
- [ ] Test entry/exit actions
- [ ] Test timestamp updates
- [ ] Test cancellation logic
- [ ] Test timeout scheduling

### **Integration Tests**
- [ ] Test complete order lifecycle
- [ ] Test state persistence to database
- [ ] Test state caching in Redis
- [ ] Test audit trail recording
- [ ] Test event publishing to Kafka
- [ ] Test concurrent state transitions

### **Edge Case Tests**
- [ ] Test timeout handling
- [ ] Test cancellation at different states
- [ ] Test rejection flow
- [ ] Test FSM recovery after failure

---

## 📚 References

- [Order FSM Design](../../business-flows/02_ORDER_FSM_DESIGN.md)
- [Architecture Decisions](../../business-flows/01_ARCHITECTURE_DECISIONS.md)
- [BE-003-17: Base FSM Framework](./BE-003-17-base-fsm-framework-v2.md)
- [REST API Standards](../../REST_API_STANDARDS.md)

---

## 🎯 Definition of Done

- [ ] All 13 OrderState enum values defined
- [ ] All 12 OrderTrigger enum values defined
- [ ] OrderFSM class implemented with Stateless4j
- [ ] All state transitions configured
- [ ] Entry/exit actions implemented
- [ ] OrderService implemented with FSM integration
- [ ] State persistence working (database + cache)
- [ ] Audit trail recording working
- [ ] Event publishing working
- [ ] Unit tests passing with > 80% coverage
- [ ] Integration tests passing
- [ ] Code reviewed and approved
- [ ] Documentation updated
