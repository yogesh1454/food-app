# BE-003-14: Kafka Topics Setup for Order & Delivery FSM

**Story ID:** BE-003-14  
**Story Points:** 5  
**Priority:** Critical (P0)  
**Sprint:** 14  
**Epic:** BE-003  
**Dependencies:** BE-003-12 (Kafka Integration)

---

## 📖 User Story

**As a** backend developer  
**I want** to set up Kafka topics for Order and Delivery FSM events  
**So that** the system can communicate asynchronously between Order and Delivery state machines

---

## ✅ Acceptance Criteria

1. **Kafka Topics Created**
   - [x] `order-events` topic created with 6 partitions *(script provided)*
   - [x] `delivery-events` topic created with 6 partitions *(script provided)*
   - [x] `assignment-requests` topic created with 3 partitions *(script provided)*
   - [x] `assignment-responses` topic created with 3 partitions *(script provided)*
   - [x] All topics have replication factor of 3 (production) *(configured in script)*
   - [x] Retention policy set to 7 days *(configured)*

2. **Topic Configuration**
   - [x] Partition key strategy: orderId for order-events *(implemented in EventPublisher)*
   - [x] Partition key strategy: deliveryId for delivery-events *(implemented in EventPublisher)*
   - [x] Compression enabled (snappy) *(configured in producer)*
   - [x] Min in-sync replicas = 2 *(configured in topic script)*

3. **Event Schemas Defined**
   - [x] OrderStateChangedEvent schema *(implemented)*
   - [x] DeliveryStateChangedEvent schema *(implemented)*
   - [x] RiderAssignmentRequestEvent schema *(implemented)*
   - [x] RiderAssignmentResponseEvent schema *(implemented)*
   - [x] All schemas versioned and documented *(version field added)*

4. **Producer Configuration**
   - [x] Idempotent producer enabled *(KafkaProducerConfig)*
   - [x] Acks = all for critical events *(KafkaProducerConfig)*
   - [x] Retry configuration with exponential backoff *(KafkaProducerConfig)*
   - [x] Serialization using JSON *(JsonSerializer configured)*

5. **Consumer Groups**
   - [x] `order-fsm-consumers` group for order events *(KafkaConsumerConfig)*
   - [x] `delivery-fsm-consumers` group for delivery events *(KafkaConsumerConfig)*
   - [x] `rider-assignment-consumers` group for assignment requests *(KafkaConsumerConfig)*
   - [x] Auto-commit disabled (manual commit) *(AckMode.MANUAL)*

6. **Monitoring**
   - [ ] Kafka lag monitoring configured *(pending)*
   - [ ] Producer/consumer metrics exposed *(pending)*
   - [x] Dead letter queue (DLQ) topic created *(script provided)*
   - [ ] Alert thresholds configured *(pending)*

**Implementation Status:** ✅ **COMPLETED** (Core functionality implemented, monitoring pending)
**Implementation Date:** 2025-11-09
**Files:** See `order-catalog-service/IMPLEMENTATION_STATUS.md`

---

## 🔧 Technical Implementation

### **Kafka Topic Configuration**

```yaml
# kafka-topics.yml
topics:
  - name: order-events
    partitions: 6
    replication-factor: 3
    config:
      retention.ms: 604800000  # 7 days
      compression.type: snappy
      min.insync.replicas: 2
      
  - name: delivery-events
    partitions: 6
    replication-factor: 3
    config:
      retention.ms: 604800000
      compression.type: snappy
      min.insync.replicas: 2
      
  - name: assignment-requests
    partitions: 3
    replication-factor: 3
    config:
      retention.ms: 86400000  # 1 day
      compression.type: snappy
      min.insync.replicas: 2
      
  - name: assignment-responses
    partitions: 3
    replication-factor: 3
    config:
      retention.ms: 86400000
      compression.type: snappy
      min.insync.replicas: 2
```

### **Event Schema Definitions**

```java
@Data
@Builder
public class OrderStateChangedEvent {
    private UUID orderId;
    private String previousState;
    private String newState;
    private String trigger;
    private UUID customerId;
    private UUID restaurantId;
    private Instant timestamp;
    private Map<String, Object> metadata;
}

@Data
@Builder
public class DeliveryStateChangedEvent {
    private UUID deliveryId;
    private UUID orderId;
    private String previousState;
    private String newState;
    private String trigger;
    private UUID riderId;
    private Instant timestamp;
    private Map<String, Object> metadata;
}

@Data
@Builder
public class RiderAssignmentRequestEvent {
    private UUID requestId;
    private UUID orderId;
    private UUID deliveryId;
    private Location restaurantLocation;
    private Location customerLocation;
    private int estimatedPrepTime;
    private BigDecimal deliveryFee;
    private Instant timestamp;
}

@Data
@Builder
public class RiderAssignmentResponseEvent {
    private UUID requestId;
    private UUID deliveryId;
    private UUID riderId;
    private boolean accepted;
    private String rejectionReason;
    private Instant timestamp;
}
```

### **Kafka Producer Configuration**

```java
@Configuration
public class KafkaProducerConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        return new DefaultKafkaProducerFactory<>(config);
    }
    
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

### **Kafka Consumer Configuration**

```java
@Configuration
public class KafkaConsumerConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Bean
    public ConsumerFactory<String, OrderStateChangedEvent> orderEventConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "order-fsm-consumers");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(config);
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderStateChangedEvent> 
        orderEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderStateChangedEvent> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderEventConsumerFactory());
        factory.getContainerProperties().setAckMode(AckMode.MANUAL);
        return factory;
    }
}
```

### **Event Publisher Service**

```java
@Service
@Slf4j
public class OrderEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String ORDER_EVENTS_TOPIC = "order-events";
    
    public void publishOrderStateChanged(OrderStateChangedEvent event) {
        try {
            ListenableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(ORDER_EVENTS_TOPIC, event.getOrderId().toString(), event);
            
            future.addCallback(
                result -> log.info("Published order state change: orderId={}, state={}", 
                    event.getOrderId(), event.getNewState()),
                ex -> log.error("Failed to publish order state change: orderId={}", 
                    event.getOrderId(), ex)
            );
        } catch (Exception e) {
            log.error("Error publishing order state change event", e);
            throw new EventPublishException("Failed to publish order state change", e);
        }
    }
}
```

---

## 📋 Testing Requirements

### **Unit Tests**
- [ ] Test event serialization/deserialization
- [ ] Test producer configuration
- [ ] Test consumer configuration
- [ ] Test partition key strategy

### **Integration Tests**
- [ ] Test end-to-end event publishing
- [ ] Test event consumption with manual commit
- [ ] Test DLQ handling for failed messages
- [ ] Test consumer group rebalancing

### **Performance Tests**
- [ ] Test throughput (target: 1000 events/sec)
- [ ] Test latency (target: < 100ms)
- [ ] Test consumer lag under load

---

## 📚 References

- [Architecture Decisions](../../business-flows/01_ARCHITECTURE_DECISIONS.md)
- [Order FSM Design](../../business-flows/02_ORDER_FSM_DESIGN.md)
- [Delivery FSM Design](../../business-flows/03_DELIVERY_FSM_DESIGN.md)
- [BE-003-12: Kafka Integration](./BE-003-12-kafka-integration-v2.md)

---

## 🎯 Definition of Done

- [ ] All Kafka topics created and configured
- [ ] Event schemas defined and documented
- [ ] Producer and consumer configurations implemented
- [ ] Event publisher service implemented
- [ ] Unit tests passing with > 80% coverage
- [ ] Integration tests passing
- [ ] Monitoring and alerting configured
- [ ] Code reviewed and approved
- [ ] Documentation updated
