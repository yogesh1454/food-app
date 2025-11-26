# Kafka to AWS SNS/SQS Migration Plan

## Overview

This document outlines the strategy to migrate from Apache Kafka to AWS SNS (Simple Notification Service) and SQS (Simple Queue Service) for event-driven architecture.

---

## 🎯 Migration Goals

1. **Replace Kafka with Native AWS Services**
2. **Reduce Infrastructure Complexity** (no Kafka cluster management)
3. **Lower Costs** (SNS/SQS vs MSK)
4. **Leverage AWS Free Tier** (1M SQS requests, 1M SNS publishes free/month)
5. **Improve Reliability** (AWS managed services)
6. **Maintain Event-Driven Architecture**

---

## 📊 Current Kafka Architecture

### **Kafka Topics Currently Used**

| Topic Name | Purpose | Producers | Consumers |
|------------|---------|-----------|-----------|
| `order-placed-events` | Order creation notification | Order Service | Payment Service |
| `payment-completed-events` | Payment success notification | Payment Service | Order Service, Delivery Service |
| `order-state-changed-events` | Order FSM transitions | Order Service | Delivery Service, Notification Service |
| `delivery-events` | Delivery FSM transitions | Delivery Service | Order Service |
| `assignment-requests` | Rider assignment requests | Order Service | Delivery Service |
| `assignment-responses` | Rider assignment results | Delivery Service | Order Service |

### **Event Flow Example: Order Creation**

```
Customer → Order API → OrderCreated Event (Kafka)
                         ↓
            Payment Service (consume) → Process Payment
                         ↓
            PaymentCompleted Event (Kafka)
                         ↓
            Order Service (consume) → Update Order State
                         ↓
            OrderStateChanged Event (Kafka)
                         ↓
            Delivery Service (consume) → Create Delivery
```

---

## 🏗️ Proposed SNS/SQS Architecture

### **Design Pattern: Fanout with SNS + SQS**

**SNS (Topics)** → **SQS (Queues)** → **Service Consumers**

```
                    ┌─────────────────┐
                    │   SNS Topic:    │
                    │  order-events   │
                    └────────┬────────┘
                             │
                ┌────────────┼────────────┐
                │            │            │
         ┌──────▼──────┐ ┌──▼─────────┐ ┌▼──────────────┐
         │  SQS Queue: │ │ SQS Queue: │ │  SQS Queue:   │
         │  payment-   │ │ delivery-  │ │  notification-│
         │  order-     │ │ order-     │ │  order-       │
         │  queue      │ │ queue      │ │  queue        │
         └──────┬──────┘ └──┬─────────┘ └┬──────────────┘
                │            │            │
         ┌──────▼──────┐ ┌──▼─────────┐ ┌▼──────────────┐
         │  Payment    │ │ Delivery   │ │  Notification │
         │  Service    │ │ Service    │ │  Service      │
         └─────────────┘ └────────────┘ └───────────────┘
```

**Benefits:**
- **Fanout:** One message published to SNS → Multiple SQS queues receive it
- **Decoupling:** Services don't need to know about each other
- **Filtering:** SNS subscription filters (optional)
- **Retries:** SQS built-in retry and DLQ
- **Scalability:** AWS handles scaling

---

## 🗺️ Migration Mapping

### **Kafka Topics → SNS Topics + SQS Queues**

| Kafka Topic | SNS Topic | SQS Queues | Consumers |
|-------------|-----------|------------|-----------|
| `order-placed-events` | `nashtto-order-events` | `nashtto-payment-order-queue`<br>`nashtto-notification-order-queue` | Payment Service<br>Notification Service |
| `payment-completed-events` | `nashtto-payment-events` | `nashtto-order-payment-queue`<br>`nashtto-delivery-payment-queue` | Order Service<br>Delivery Service |
| `order-state-changed-events` | `nashtto-order-state-events` | `nashtto-delivery-state-queue`<br>`nashtto-notification-state-queue` | Delivery Service<br>Notification Service |
| `delivery-events` | `nashtto-delivery-events` | `nashtto-order-delivery-queue` | Order Service |
| `assignment-requests` | `nashtto-assignment-requests` | `nashtto-delivery-assignment-queue` | Delivery Service |
| `assignment-responses` | `nashtto-assignment-responses` | `nashtto-order-assignment-queue` | Order Service |

### **Dead Letter Queues (DLQs)**

For each SQS queue, create a DLQ:

| Primary Queue | Dead Letter Queue | Max Receives |
|---------------|-------------------|--------------|
| `nashtto-payment-order-queue` | `nashtto-payment-order-dlq` | 3 |
| `nashtto-order-payment-queue` | `nashtto-order-payment-dlq` | 3 |
| ... | ... | 3 |

---

## 📋 Migration Steps

### **Phase 1: Infrastructure Setup (AWS CloudFormation)**

**Update `infrastructure-stack.yaml`:**

```yaml
# SNS Topics
OrderEventsTopic:
  Type: AWS::SNS::Topic
  Properties:
    TopicName: !Sub ${ProjectName}-order-events
    DisplayName: Order Events
    Tags:
      - Key: Name
        Value: !Sub ${ProjectName}-order-events

PaymentEventsTopic:
  Type: AWS::SNS::Topic
  Properties:
    TopicName: !Sub ${ProjectName}-payment-events
    DisplayName: Payment Events

DeliveryEventsTopic:
  Type: AWS::SNS::Topic
  Properties:
    TopicName: !Sub ${ProjectName}-delivery-events
    DisplayName: Delivery Events

OrderStateEventsTopic:
  Type: AWS::SNS::Topic
  Properties:
    TopicName: !Sub ${ProjectName}-order-state-events
    DisplayName: Order State Change Events

AssignmentRequestsTopic:
  Type: AWS::SNS::Topic
  Properties:
    TopicName: !Sub ${ProjectName}-assignment-requests
    DisplayName: Rider Assignment Requests

AssignmentResponsesTopic:
  Type: AWS::SNS::Topic
  Properties:
    TopicName: !Sub ${ProjectName}-assignment-responses
    DisplayName: Rider Assignment Responses

# SQS Queues
PaymentOrderQueue:
  Type: AWS::SQS::Queue
  Properties:
    QueueName: !Sub ${ProjectName}-payment-order-queue
    VisibilityTimeout: 300
    MessageRetentionPeriod: 1209600  # 14 days
    RedrivePolicy:
      deadLetterTargetArn: !GetAtt PaymentOrderDLQ.Arn
      maxReceiveCount: 3

PaymentOrderDLQ:
  Type: AWS::SQS::Queue
  Properties:
    QueueName: !Sub ${ProjectName}-payment-order-dlq
    MessageRetentionPeriod: 1209600

# SNS Subscriptions (SNS → SQS)
PaymentOrderSubscription:
  Type: AWS::SNS::Subscription
  Properties:
    Protocol: sqs
    TopicArn: !Ref OrderEventsTopic
    Endpoint: !GetAtt PaymentOrderQueue.Arn
    RawMessageDelivery: true  # Pass message as-is

# SQS Queue Policy (allow SNS to send)
PaymentOrderQueuePolicy:
  Type: AWS::SQS::QueuePolicy
  Properties:
    Queues:
      - !Ref PaymentOrderQueue
    PolicyDocument:
      Statement:
        - Effect: Allow
          Principal:
            Service: sns.amazonaws.com
          Action: SQS:SendMessage
          Resource: !GetAtt PaymentOrderQueue.Arn
          Condition:
            ArnEquals:
              aws:SourceArn: !Ref OrderEventsTopic
```

**Repeat for all queues/topics**

### **Phase 2: Code Changes**

#### **2.1 Add AWS SDK Dependencies** (`build.gradle`)

```gradle
dependencies {
    // AWS SDK for SNS/SQS
    implementation platform('com.amazonaws:aws-java-sdk-bom:1.12.529')
    implementation 'com.amazonaws:aws-java-sdk-sns'
    implementation 'com.amazonaws:aws-java-sdk-sqs'
    
    // OR use AWS SDK v2 (recommended)
    implementation platform('software.amazon.awssdk:bom:2.20.100')
    implementation 'software.amazon.awssdk:sns'
    implementation 'software.amazon.awssdk:sqs'
    implementation 'io.awspring.cloud:spring-cloud-aws-starter-sqs:3.0.3'
}
```

#### **2.2 Create SNS Publisher Configuration**

```java
// SnsPublisherConfig.java
@Configuration
@ConditionalOnProperty(name = "features.sns.enabled", havingValue = "true")
public class SnsPublisherConfig {
    
    @Value("${aws.region:us-east-1}")
    private String region;
    
    @Bean
    public SnsClient snsClient() {
        return SnsClient.builder()
            .region(Region.of(region))
            .build();
    }
}
```

#### **2.3 Create SQS Consumer Configuration**

```java
// SqsConsumerConfig.java
@Configuration
@ConditionalOnProperty(name = "features.sqs.enabled", havingValue = "true")
public class SqsConsumerConfig {
    
    @Value("${aws.region:us-east-1}")
    private String region;
    
    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
            .region(Region.of(region))
            .build();
    }
}
```

#### **2.4 Create SNS Publisher Service**

```java
// SnsEventPublisher.java
@Service
@Slf4j
@ConditionalOnProperty(name = "features.sns.enabled", havingValue = "true")
public class SnsEventPublisher {
    
    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;
    
    @Value("${aws.sns.topics.order-events}")
    private String orderEventsTopic;
    
    @Value("${aws.sns.topics.payment-events}")
    private String paymentEventsTopic;
    
    public void publishOrderPlaced(OrderPlacedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            
            PublishRequest request = PublishRequest.builder()
                .topicArn(orderEventsTopic)
                .message(message)
                .messageAttributes(Map.of(
                    "eventType", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue("OrderPlaced")
                        .build()
                ))
                .build();
            
            PublishResponse response = snsClient.publish(request);
            log.info("Published OrderPlacedEvent to SNS: messageId={}, orderId={}", 
                response.messageId(), event.getOrderId());
                
        } catch (Exception e) {
            log.error("Failed to publish OrderPlacedEvent to SNS: orderId={}", 
                event.getOrderId(), e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}
```

#### **2.5 Create SQS Consumer**

```java
// SqsOrderEventConsumer.java
@Service
@Slf4j
@ConditionalOnProperty(name = "features.sqs.enabled", havingValue = "true")
public class SqsOrderEventConsumer {
    
    private final OrderService orderService;
    private final ObjectMapper objectMapper;
    
    @SqsListener(value = "${aws.sqs.queues.payment-order-queue}")
    public void handleOrderEvent(String message) {
        try {
            OrderPlacedEvent event = objectMapper.readValue(message, OrderPlacedEvent.class);
            log.info("Received OrderPlacedEvent from SQS: orderId={}", event.getOrderId());
            
            // Process event
            orderService.handleOrderPlaced(event);
            
        } catch (Exception e) {
            log.error("Failed to process OrderPlacedEvent from SQS", e);
            throw new RuntimeException("Failed to process event", e); // Will go to DLQ
        }
    }
}
```

#### **2.6 Update application.yml**

```yaml
features:
  kafka:
    enabled: false  # Disable Kafka
  sns:
    enabled: true   # Enable SNS
  sqs:
    enabled: true   # Enable SQS

aws:
  region: us-east-1
  sns:
    topics:
      order-events: arn:aws:sns:us-east-1:017654360674:nashtto-order-events
      payment-events: arn:aws:sns:us-east-1:017654360674:nashtto-payment-events
      delivery-events: arn:aws:sns:us-east-1:017654360674:nashtto-delivery-events
      order-state-events: arn:aws:sns:us-east-1:017654360674:nashtto-order-state-events
      assignment-requests: arn:aws:sns:us-east-1:017654360674:nashtto-assignment-requests
      assignment-responses: arn:aws:sns:us-east-1:017654360674:nashtto-assignment-responses
  sqs:
    queues:
      payment-order-queue: https://sqs.us-east-1.amazonaws.com/017654360674/nashtto-payment-order-queue
      order-payment-queue: https://sqs.us-east-1.amazonaws.com/017654360674/nashtto-order-payment-queue
      delivery-state-queue: https://sqs.us-east-1.amazonaws.com/017654360674/nashtto-delivery-state-queue
      order-delivery-queue: https://sqs.us-east-1.amazonaws.com/017654360674/nashtto-order-delivery-queue
```

### **Phase 3: Dual Publishing (Transition Period)**

Run both Kafka and SNS/SQS simultaneously:

```java
public void publishOrderPlaced(OrderPlacedEvent event) {
    // Publish to Kafka (if enabled)
    if (kafkaEnabled) {
        kafkaPublisher.publishOrderPlaced(event);
    }
    
    // Publish to SNS (if enabled)
    if (snsEnabled) {
        snsPublisher.publishOrderPlaced(event);
    }
}
```

### **Phase 4: Gradual Migration**

1. **Week 1:** Enable SNS/SQS (dual-write)
2. **Week 2:** Verify SNS/SQS consumers working
3. **Week 3:** Disable Kafka consumers (use only SNS/SQS)
4. **Week 4:** Disable Kafka publishers
5. **Week 5:** Remove Kafka dependencies

---

## 💰 Cost Comparison

### **Kafka (AWS MSK)**

| Component | Cost |
|-----------|------|
| MSK Cluster (3 brokers, kafka.t3.small) | ~$180/month |
| Storage (100GB EBS) | ~$10/month |
| **Total** | **~$190/month** |

### **SNS + SQS**

| Component | Free Tier | Paid (after Free Tier) |
|-----------|-----------|------------------------|
| SNS Publishes | 1M/month free | $0.50 per 1M publishes |
| SQS Requests | 1M/month free | $0.40 per 1M requests |
| SQS Data Transfer | Included | Minimal |
| **Estimated Monthly Cost** | **$0** (within Free Tier) | **~$5-10/month** (low traffic) |

**Savings: ~$180-185/month**

---

## 🔄 SNS/SQS vs Kafka

### **Advantages of SNS/SQS**

✅ **Fully Managed** - No cluster management  
✅ **Auto-Scaling** - AWS handles capacity  
✅ **Built-in DLQ** - Dead letter queues  
✅ **Free Tier Friendly** - 1M free requests  
✅ **AWS Integration** - Works with Lambda, EventBridge, etc.  
✅ **Lower Cost** - Pay per use  
✅ **IAM Integration** - Native AWS permissions

### **Advantages of Kafka**

✅ **Ordering Guarantees** - Per-partition ordering  
✅ **Replay Capability** - Can reprocess old messages  
✅ **High Throughput** - Better for millions of messages/sec  
✅ **Consumer Groups** - Advanced consumer patterns  
✅ **Exactly-Once Semantics** - Stronger guarantees

### **Recommendation for Nashtto**

**Use SNS/SQS** because:
- Traffic volume is moderate (not millions/sec)
- AWS native integration is valuable
- Cost savings are significant
- Simpler operations

---

## 📊 Message Ordering Considerations

### **Challenge: SQS Standard Queues Don't Guarantee Order**

**Solution: Use SQS FIFO Queues** (for critical workflows)

```yaml
OrderPaymentQueue:
  Type: AWS::SQS::Queue
  Properties:
    QueueName: !Sub ${ProjectName}-order-payment.fifo  # FIFO suffix
    FifoQueue: true
    ContentBasedDeduplication: true
    DeduplicationScope: messageGroup
    FifoThroughputLimit: perMessageGroupId
```

**FIFO Queue Characteristics:**
- **Ordering:** Messages processed in order (per MessageGroupId)
- **Deduplication:** Prevents duplicate messages (5-minute window)
- **Throughput:** 300 TPS per MessageGroupId (3000 TPS with batching)

**MessageGroupId Strategy:**

```java
// Use orderId as MessageGroupId for per-order ordering
SendMessageRequest request = SendMessageRequest.builder()
    .queueUrl(queueUrl)
    .messageBody(message)
    .messageGroupId(event.getOrderId().toString())  // Per-order ordering
    .build();
```

---

## ✅ Migration Checklist

### **Infrastructure**

- [ ] Create SNS topics in CloudFormation
- [ ] Create SQS queues (Standard or FIFO)
- [ ] Create DLQs for each queue
- [ ] Set up SNS → SQS subscriptions
- [ ] Configure IAM roles for EC2 to access SNS/SQS
- [ ] Deploy CloudFormation stack

### **Code Changes**

- [ ] Add AWS SDK dependencies
- [ ] Create SnsPublisherConfig
- [ ] Create SqsConsumerConfig
- [ ] Implement SnsEventPublisher
- [ ] Implement SQS consumers
- [ ] Add feature flags (features.sns.enabled, features.sqs.enabled)
- [ ] Update application.yml with topic/queue ARNs

### **Testing**

- [ ] Test SNS publishing locally (use LocalStack)
- [ ] Test SQS consuming locally
- [ ] Integration tests with SNS/SQS
- [ ] Load testing (ensure throughput is adequate)

### **Deployment**

- [ ] Enable dual-write (Kafka + SNS/SQS)
- [ ] Monitor both systems for 1 week
- [ ] Gradually disable Kafka consumers
- [ ] Disable Kafka publishers
- [ ] Remove Kafka dependencies
- [ ] Update documentation

---

## 🔍 Monitoring & Alerts

### **CloudWatch Metrics to Monitor**

**SNS:**
- `NumberOfMessagesPublished`
- `NumberOfNotificationsFailed`
- `PublishSize`

**SQS:**
- `ApproximateNumberOfMessagesVisible`
- `ApproximateAgeOfOldestMessage`
- `NumberOfMessagesReceived`
- `NumberOfMessagesSent`
- `ApproximateNumberOfMessagesNotVisible` (in-flight)

### **CloudWatch Alarms**

```yaml
SQSMessageAgeAlarm:
  Type: AWS::CloudWatch::Alarm
  Properties:
    AlarmName: !Sub ${ProjectName}-sqs-message-age-high
    MetricName: ApproximateAgeOfOldestMessage
    Namespace: AWS/SQS
    Statistic: Maximum
    Period: 300
    EvaluationPeriods: 1
    Threshold: 900  # 15 minutes
    ComparisonOperator: GreaterThanThreshold
    Dimensions:
      - Name: QueueName
        Value: !GetAtt PaymentOrderQueue.QueueName
    AlarmActions:
      - !Ref TestTopic  # SNS notification
```

---

## 📝 Next Steps

1. **Phase 1 (Week 1):** Update CloudFormation to add SNS/SQS resources
2. **Phase 2 (Week 2):** Implement SNS publishers in code
3. **Phase 3 (Week 3):** Implement SQS consumers
4. **Phase 4 (Week 4):** Enable dual-write (Kafka + SNS/SQS)
5. **Phase 5 (Week 5):** Monitor and validate
6. **Phase 6 (Week 6):** Disable Kafka, remove dependencies

---

## 📚 References

- [AWS SNS Documentation](https://docs.aws.amazon.com/sns/latest/dg/welcome.html)
- [AWS SQS Documentation](https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/welcome.html)
- [SNS FIFO Topics](https://docs.aws.amazon.com/sns/latest/dg/fifo-topics.html)
- [SQS FIFO Queues](https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/FIFO-queues.html)
- [Spring Cloud AWS](https://docs.awspring.io/spring-cloud-aws/docs/3.0.3/reference/html/index.html)

