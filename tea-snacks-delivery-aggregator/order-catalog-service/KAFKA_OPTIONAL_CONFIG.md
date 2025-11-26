# Kafka Optional Configuration Guide

## Overview

The order-catalog-service has been configured to work **with or without** Kafka. This allows the application to start and function properly on AWS EC2 without requiring Kafka infrastructure, while maintaining the ability to enable Kafka later.

---

## ✅ What Was Changed

### 1. **Feature Flag Added** (`application.yml`)

```yaml
features:
  kafka:
    enabled: false  # Set to true when Kafka is available
```

**Location:** `src/main/resources/application.yml`

### 2. **Conditional Configuration Classes**

The following configuration classes are now conditional (only active when `features.kafka.enabled=true`):

- `KafkaProducerConfig.java` - Kafka producer beans
- `KafkaConsumerConfig.java` - Kafka consumer factories

**Annotation Added:**
```java
@ConditionalOnProperty(name = "features.kafka.enabled", havingValue = "true", matchIfMissing = false)
```

### 3. **Conditional Publishers (No-Op When Disabled)**

Updated to gracefully handle Kafka being disabled:

- `OrderEventPublisher.java` - Order event publishing
- `EventPublisher.java` - FSM state change publishing

**Changes:**
- Made `KafkaTemplate` optional (`@Autowired(required = false)`)
- Added feature flag check in all publish methods
- Return early with debug log if Kafka is disabled

### 4. **Conditional Consumers**

The following Kafka consumers are disabled when `features.kafka.enabled=false`:

- `DeliveryEventConsumer.java` - Listens to delivery events
- `OrderEventConsumer.java` - Listens to order events

**Annotation Added:**
```java
@ConditionalOnProperty(name = "features.kafka.enabled", havingValue = "true", matchIfMissing = false)
```

---

## 🚀 How to Deploy on AWS EC2

### **Current Configuration (Kafka Disabled)**

The default `application.yml` has Kafka **disabled**:

```yaml
features:
  kafka:
    enabled: false
```

### **Deploy Steps**

1. **Build the JAR:**
   ```bash
   cd /Users/yogesh/Documents/ws/food-app/tea-snacks-delivery-aggregator
   ./gradlew :order-catalog-service:bootJar
   ```

2. **Copy JAR to EC2:**
   ```bash
   scp -i infrastructure/cloudformation/nastto-key.pem \
       tea-snacks-delivery-aggregator/order-catalog-service/build/libs/order-catalog-service-0.0.1-SNAPSHOT.jar \
       ec2-user@13.223.13.132:/opt/nashtto/
   ```

3. **SSH into EC2 and Run:**
   ```bash
   ssh -i infrastructure/cloudformation/nastto-key.pem ec2-user@13.223.13.132
   cd /opt/nashtto
   java -jar order-catalog-service-0.0.1-SNAPSHOT.jar
   ```

4. **Application Will Start Successfully** ✅
   - No Kafka connection errors
   - All REST APIs will work
   - Events will not be published (no-op)

---

## ⚠️ Features Disabled When Kafka is Off

### **1. Event-Driven Communication**

Without Kafka, the following inter-service communication is disabled:

| Event Type | Topic | Impact |
|------------|-------|--------|
| **OrderPlacedEvent** | `order-placed-events` | Payment service won't be notified automatically |
| **PaymentCompletedEvent** | `payment-completed-events` | Delivery service won't be notified automatically |
| **OrderStateChangedEvent** | `order-state-changed-events` | No order state change notifications |
| **DeliveryStateChangedEvent** | `delivery-events` | Order service won't receive delivery updates |
| **RiderAssignmentRequestEvent** | `assignment-requests` | Rider assignment service won't receive requests |
| **RiderAssignmentResponseEvent** | `assignment-responses` | Order service won't receive rider assignment updates |

### **2. Asynchronous Workflows Disabled**

- **Automatic Delivery Creation:** When order reaches `READY_FOR_PICKUP` state, delivery won't be auto-created
- **Rider Assignment:** Automatic rider search and assignment won't work
- **Order Status Updates:** Order status won't auto-update based on delivery progress

### **3. What STILL WORKS** ✅

| Feature | Status | Notes |
|---------|--------|-------|
| **REST APIs** | ✅ Working | All CRUD operations functional |
| **Order Creation** | ✅ Working | Via `/api/v1/orders/checkout` |
| **Order State Machine** | ✅ Working | Manual state transitions work |
| **Database** | ✅ Working | PostgreSQL operations work |
| **Redis Cache** | ✅ Working | If Redis is available |
| **Restaurant Management** | ✅ Working | All restaurant APIs work |
| **Menu Management** | ✅ Working | All menu APIs work |
| **Customer APIs** | ✅ Working | Order queries, status checks |

---

## 🔄 When to Enable Kafka Later

### **Option A: Add Apache Kafka to AWS**

If you decide to deploy Kafka (MSK or EC2-based):

1. **Update `application.yml`:**
   ```yaml
   spring:
     kafka:
       bootstrap-servers: your-kafka-broker:9092  # Update this
   
   features:
     kafka:
       enabled: true  # Enable Kafka
   ```

2. **Create Kafka Topics:**
   ```bash
   # Order events
   kafka-topics.sh --create --bootstrap-server localhost:9092 \
     --topic order-placed-events --partitions 3 --replication-factor 1
   
   kafka-topics.sh --create --bootstrap-server localhost:9092 \
     --topic payment-completed-events --partitions 3 --replication-factor 1
   
   kafka-topics.sh --create --bootstrap-server localhost:9092 \
     --topic order-state-changed-events --partitions 3 --replication-factor 1
   
   # Delivery events
   kafka-topics.sh --create --bootstrap-server localhost:9092 \
     --topic delivery-events --partitions 3 --replication-factor 1
   
   # Assignment events
   kafka-topics.sh --create --bootstrap-server localhost:9092 \
     --topic assignment-requests --partitions 3 --replication-factor 1
   
   kafka-topics.sh --create --bootstrap-server localhost:9092 \
     --topic assignment-responses --partitions 3 --replication-factor 1
   ```

3. **Restart Application**

### **Option B: Migrate to AWS SNS/SQS** (Recommended)

See `KAFKA_TO_SNS_SQS_MIGRATION_PLAN.md` for detailed migration strategy.

---

## 🧪 Testing

### **Test Application Starts Without Kafka**

```bash
# 1. Ensure Kafka is disabled
grep "features:" src/main/resources/application.yml -A 3

# Output should show:
# features:
#   kafka:
#     enabled: false

# 2. Build
./gradlew :order-catalog-service:bootJar

# 3. Run (will start successfully)
java -jar order-catalog-service/build/libs/order-catalog-service-0.0.1-SNAPSHOT.jar

# 4. Check logs - should see:
# - No Kafka connection errors
# - "Kafka disabled, skipping..." debug messages when events would be published
```

### **Test API Functionality**

```bash
# Health check
curl http://localhost:8080/actuator/health

# Create order (should work)
curl -X POST http://localhost:8080/api/v1/orders/checkout \
  -H "Content-Type: application/json" \
  -d '{ "customerId": "...", "restaurantId": "...", "items": [...] }'

# Get order (should work)
curl http://localhost:8080/api/v1/orders/{orderId}
```

---

## 📋 Configuration Files Modified

| File | Change | Purpose |
|------|--------|---------|
| `application.yml` | Added `features.kafka.enabled: false` | Feature flag |
| `KafkaProducerConfig.java` | Added `@ConditionalOnProperty` | Conditional bean creation |
| `KafkaConsumerConfig.java` | Added `@ConditionalOnProperty` | Conditional bean creation |
| `OrderEventPublisher.java` | Made KafkaTemplate optional + checks | No-op when disabled |
| `EventPublisher.java` | Made KafkaTemplate optional + checks | No-op when disabled |
| `DeliveryEventConsumer.java` | Added `@ConditionalOnProperty` | Disable consumer |
| `OrderEventConsumer.java` | Added `@ConditionalOnProperty` | Disable consumer |

---

## 💡 Key Points

1. **Zero Code Changes for APIs** - All REST APIs work exactly as before
2. **Graceful Degradation** - Events are silently skipped with debug logs
3. **Easy to Enable** - Just flip `features.kafka.enabled` to `true`
4. **No Runtime Errors** - Application starts successfully without Kafka
5. **Future-Proof** - Ready for Kafka or SNS/SQS migration

---

## 🆘 Troubleshooting

### **Application Won't Start**

1. Check if Kafka autoconfiguration is trying to connect:
   ```bash
   # Add this to application.yml
   spring:
     autoconfigure:
       exclude:
         - org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
   ```

2. Verify feature flag is false:
   ```bash
   grep "kafka:" src/main/resources/application.yml -A 2
   ```

### **Events Not Being Published (Expected)**

This is normal when Kafka is disabled. Check logs:

```
DEBUG c.t.o.order.service.OrderEventPublisher : Kafka disabled, skipping OrderPlacedEvent: orderId=...
```

### **Want to See What Events Would Be Published**

Change log level to DEBUG:

```yaml
logging:
  level:
    com.teadelivery.ordercatalog.order.service.OrderEventPublisher: DEBUG
    com.teadelivery.ordercatalog.common.fsm.EventPublisher: DEBUG
```

---

## ✅ Summary

✅ **Application starts without Kafka**  
✅ **All REST APIs functional**  
✅ **Database operations work**  
✅ **Redis caching works**  
✅ **Ready for AWS EC2 deployment**  
⚠️ **Event-driven workflows disabled** (will be replaced by SNS/SQS later)

---

## 📝 Next Steps

1. **Deploy to AWS EC2** - Application is ready
2. **Test APIs** - Verify all endpoints work
3. **Plan SNS/SQS Migration** - See migration guide
4. **Implement SNS/SQS Publishers** - Replace Kafka publishers
5. **Implement SNS/SQS Consumers** - Replace Kafka consumers
6. **Remove Kafka Dependencies** - Clean up build.gradle

