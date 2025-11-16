# Integration Tests - Order Catalog Service

## Overview

Comprehensive integration tests for the Order Catalog Service using **@SpringBootTest** with **Testcontainers** for true end-to-end testing.

## Test Coverage

### Total Test Scenarios: 32+

#### 1. Order Creation & Validation (7 tests)
- ✅ Create order successfully
- ✅ Reject order with empty items
- ✅ Reject order with missing delivery address
- ✅ Validate order successfully
- ✅ Retrieve order by ID
- ✅ Return 404 for non-existent order
- ✅ List customer orders

#### 2. Restaurant Acceptance Flow (6 tests)
- ✅ Submit order to restaurant with timeout scheduling
- ✅ Accept order and cancel timeout
- ✅ Reject order and cancel timeout
- ✅ Auto-reject order on restaurant timeout (Redis keyspace notifications)
- ✅ List pending orders for restaurant
- ✅ Prevent acceptance from invalid state

#### 3. Order Preparation & Delivery (6 tests)
- ✅ Start order preparation
- ✅ Mark order ready for pickup
- ✅ Assign rider to order
- ✅ Confirm rider pickup
- ✅ Confirm order delivery
- ✅ Complete full order lifecycle (9 state transitions)

#### 4. Order Cancellation (6 tests)
- ✅ Cancel order in early stage (VALIDATED)
- ✅ Cancel order after acceptance
- ✅ Prevent cancellation from non-cancellable state (DELIVERED)
- ✅ Cancel order in PREPARING state
- ✅ Prevent cancellation without customer ID
- ✅ Prevent cancellation with wrong customer ID

#### 5. Redis Cache & Kafka Events (7 tests)
- ✅ Cache order state on transition
- ✅ Retrieve state from cache
- ✅ Invalidate cache on state change
- ✅ Handle cache TTL expiration
- ✅ Publish OrderStateChangedEvent to Kafka
- ✅ Use orderId as partition key for consistent ordering
- ✅ Include all required fields in event

---

## Technology Stack

### Testing Framework
- **JUnit 5** - Test framework
- **AssertJ** - Fluent assertions
- **Awaitility** - Async/timeout testing

### Testcontainers
- **PostgreSQL 15** - Database
- **Redis 7** - Cache and timeout handling
- **Kafka (Confluent 7.5)** - Event streaming

### Spring Boot Test
- **@SpringBootTest(webEnvironment = RANDOM_PORT)** - Full application context
- **TestRestTemplate** - Real HTTP client
- **@Testcontainers** - Container lifecycle management

---

## Prerequisites

### Required Software
1. **Docker Desktop** - Must be running
2. **Java 21** - JDK installed
3. **Gradle** - Build tool

### Docker Resources
Recommended Docker settings:
- **Memory:** 4GB minimum (8GB recommended)
- **CPUs:** 2 minimum (4 recommended)
- **Disk:** 20GB free space

---

## Running Tests

### 1. Run All Integration Tests
```bash
cd order-catalog-service
./gradlew test --tests "com.teadelivery.ordercatalog.integration.*"
```

### 2. Run Specific Test Class
```bash
# Order Creation Tests
./gradlew test --tests OrderCreationFlowIntegrationTest

# Restaurant Acceptance Tests (includes timeout)
./gradlew test --tests RestaurantAcceptanceFlowIntegrationTest

# Preparation & Delivery Tests
./gradlew test --tests OrderPreparationDeliveryFlowIntegrationTest

# Cancellation Tests
./gradlew test --tests OrderCancellationFlowIntegrationTest

# Redis & Kafka Tests
./gradlew test --tests RedisCacheKafkaIntegrationTest
```

### 3. Run Test Suite
```bash
./gradlew test --tests IntegrationTestSuite
```

### 4. Run with Verbose Output
```bash
./gradlew test --tests "com.teadelivery.ordercatalog.integration.*" --info
```

### 5. Run in Continuous Mode
```bash
./gradlew test --tests "com.teadelivery.ordercatalog.integration.*" --continuous
```

---

## Test Execution Flow

### 1. Container Startup (Automatic)
```
Starting Testcontainers...
✓ PostgreSQL 15 container started (port: random)
✓ Redis 7 container started (port: random)
✓ Kafka container started (port: random)
```

### 2. Spring Boot Application Startup
```
✓ Full Spring context loaded
✓ Flyway migrations applied
✓ Embedded Tomcat started on random port
✓ All beans initialized
```

### 3. Test Execution
```
✓ Each test runs in isolation
✓ Database cleaned before each test
✓ Redis flushed before each test
✓ Real HTTP requests via TestRestTemplate
```

### 4. Container Cleanup (Automatic)
```
✓ Containers stopped after all tests
✓ Resources released
```

---

## Test Configuration

### Application Profile: `integration-test`
Location: `src/test/resources/application-integration-test.yml`

```yaml
spring:
  jpa:
    show-sql: true  # Debug SQL queries
    
# Shorter timeouts for faster tests
order:
  timeout:
    restaurant-acceptance: 5s  # Instead of 2m
    payment-processing: 10s    # Instead of 5m
    rider-assignment: 10s      # Instead of 5m
```

### Dynamic Properties
Testcontainers automatically configures:
- Database URL, username, password
- Redis host and port
- Kafka bootstrap servers

---

## Key Features

### 1. True End-to-End Testing
- ✅ Real HTTP server (Tomcat)
- ✅ Real database (PostgreSQL)
- ✅ Real cache (Redis)
- ✅ Real message broker (Kafka)
- ✅ Full Spring context

### 2. Timeout Testing with Redis
```java
@Test
void shouldAutoRejectOrderOnTimeout() {
    // Given: Order in PENDING_ACCEPTANCE
    UUID orderId = createOrderInPendingAcceptanceState();
    
    // When: Wait for 5 second timeout
    await().atMost(Duration.ofSeconds(8))
        .untilAsserted(() -> {
            Order order = orderRepository.findById(orderId).orElseThrow();
            assertThat(order.getState()).isEqualTo(OrderState.REJECTED);
        });
    
    // Then: Order auto-rejected by Redis keyspace notification
}
```

### 3. Kafka Event Verification
```java
@Test
void shouldPublishOrderStateChangedEvent() {
    // Given: Kafka consumer listening
    BlockingQueue<ConsumerRecord<String, OrderStateChangedEvent>> records = 
        new LinkedBlockingQueue<>();
    
    // When: Order state changes
    restTemplate.postForEntity(url, request, OrderResponse.class);
    
    // Then: Event published to Kafka
    ConsumerRecord<String, OrderStateChangedEvent> record = 
        records.poll(10, TimeUnit.SECONDS);
    assertThat(record.value().getOrderId()).isEqualTo(orderId);
}
```

### 4. Redis Cache Verification
```java
@Test
void shouldCacheOrderStateOnTransition() {
    // When: Order state changes
    restTemplate.postForEntity(url, request, OrderResponse.class);
    
    // Then: Redis cache updated
    String cachedState = (String) redisTemplate.opsForValue()
        .get("order:state:" + orderId);
    assertThat(cachedState).isEqualTo(OrderState.ACCEPTED.name());
}
```

---

## Troubleshooting

### Issue: Docker not running
```
Error: Could not find a valid Docker environment
Solution: Start Docker Desktop
```

### Issue: Port conflicts
```
Error: Port 5432 already in use
Solution: Testcontainers uses random ports, ensure no other tests are running
```

### Issue: Tests timeout
```
Error: Test timed out after 60 seconds
Solution: 
1. Check Docker has sufficient resources
2. Increase timeout in test: await().atMost(Duration.ofSeconds(120))
```

### Issue: Flyway migration fails
```
Error: Flyway migration failed
Solution: 
1. Check migration scripts in src/main/resources/db/migration
2. Ensure PostgreSQL container started successfully
```

### Issue: Redis keyspace notifications not working
```
Error: Timeout waiting for order rejection
Solution: 
1. Verify Redis container is running
2. Check RedisConfig enables keyspace notifications
3. Increase await timeout
```

---

## Performance

### Typical Execution Times
- **Container startup:** 10-15 seconds (first run)
- **Container startup:** 2-3 seconds (subsequent runs with reuse)
- **Single test:** 1-3 seconds
- **Full test suite:** 2-5 minutes

### Optimization Tips
1. **Enable container reuse:**
   ```java
   @Container
   static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
       .withReuse(true);  // ← Reuse across test runs
   ```

2. **Run tests in parallel:**
   ```bash
   ./gradlew test --parallel --max-workers=4
   ```

3. **Use test slices for faster feedback:**
   ```bash
   # Run only critical tests first
   ./gradlew test --tests RestaurantAcceptanceFlowIntegrationTest
   ```

---

## CI/CD Integration

### GitHub Actions Example
```yaml
name: Integration Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          
      - name: Run Integration Tests
        run: |
          cd order-catalog-service
          ./gradlew test --tests "com.teadelivery.ordercatalog.integration.*"
          
      - name: Publish Test Report
        uses: dorny/test-reporter@v1
        if: always()
        with:
          name: Integration Test Results
          path: '**/build/test-results/test/*.xml'
          reporter: java-junit
```

---

## Next Steps

### 1. Add More Test Scenarios
- [ ] Concurrent order modifications
- [ ] Payment timeout scenarios
- [ ] Rider assignment timeout scenarios
- [ ] Error recovery scenarios

### 2. Performance Testing
- [ ] Load test with 1000+ concurrent orders
- [ ] Stress test Redis cache
- [ ] Kafka throughput testing

### 3. Chaos Engineering
- [ ] Test with Redis failures
- [ ] Test with Kafka unavailability
- [ ] Test with database connection loss

---

## References

- [Testcontainers Documentation](https://www.testcontainers.org/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Awaitility Documentation](http://www.awaitility.org/)

---

**Last Updated:** 2025-11-09  
**Test Coverage:** 32+ scenarios  
**Status:** ✅ Ready for execution
