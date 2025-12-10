# BE-003-12: Kafka Event Publishing Integration

**Story ID:** BE-003-12  
**Story Points:** 8  
**Priority:** High  
**Sprint:** 9  
**Epic:** BE-003  
**Dependencies:** All previous stories

---

## 📖 User Story

**As a** system  
**I want** to publish domain events to Kafka  
**So that** other services can react to vendor, menu, and order changes

---

## ✅ Acceptance Criteria

1. **Event Topics**
   - [ ] vendor.events - Vendor lifecycle events
   - [ ] branch.events - Branch lifecycle events
   - [ ] menu.events - Menu update events
   - [ ] order.events - Order lifecycle events

2. **Event Types**
   - [ ] vendor.created, vendor.updated
   - [ ] branch.created, branch.approved, branch.rejected
   - [ ] menu.updated, menu.item.created, menu.item.updated, menu.item.deleted
   - [ ] order.created, order.status.changed, order.completed, order.cancelled

3. **Event Schema**
   - [ ] Consistent event structure (eventId, eventType, timestamp, payload)
   - [ ] JSON serialization
   - [ ] Schema versioning support

4. **Reliability**
   - [ ] Events published transactionally
   - [ ] Retry mechanism for failures
   - [ ] Dead letter queue for failed events
   - [ ] Event ordering guaranteed per partition key

---

## 🔧 Key Implementation

### **Event Base Class**
```java
@Data
@SuperBuilder
public abstract class DomainEvent {
    private UUID eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String version = "1.0";
}

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class OrderEvent extends DomainEvent {
    private UUID orderId;
    private UUID customerId;
    private UUID branchId;
    private String orderStatus;
    private BigDecimal totalAmount;
    private LocalDateTime orderedAt;
}
```

### **Kafka Configuration**
```java
@Configuration
public class KafkaProducerConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Bean
    public ProducerFactory<String, DomainEvent> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(config);
    }
    
    @Bean
    public KafkaTemplate<String, DomainEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

### **Event Publisher**
```java
@Component
@Slf4j
public class OrderEventPublisher {
    
    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;
    
    @Value("${kafka.topics.order-events}")
    private String orderEventsTopic;
    
    public void publishOrderCreated(Order order) {
        OrderEvent event = OrderEvent.builder()
            .eventId(UUID.randomUUID())
            .eventType("order.created")
            .timestamp(LocalDateTime.now())
            .orderId(order.getOrderId())
            .customerId(order.getCustomerId())
            .branchId(order.getBranch().getBranchId())
            .orderStatus(order.getOrderStatus())
            .totalAmount(order.getTotalAmount())
            .orderedAt(order.getOrderedAt())
            .build();
        
        kafkaTemplate.send(orderEventsTopic, order.getOrderId().toString(), event);
        log.info("Published order.created event: {}", order.getOrderId());
    }
}
```

---

## 📋 Definition of Done

- [ ] Kafka topics configured
- [ ] Event publishers implemented
- [ ] Event schemas defined
- [ ] Transactional publishing
- [ ] Retry mechanism
- [ ] Tests passing
- [ ] Code reviewed
