# 🎯 Local Testing Strategy for Order Catalog Service

## Overview

This document outlines the **best practices for local testing** without CI/CD pipelines. The strategy ensures:
- ✅ **Fast test execution** (no container startup overhead)
- ✅ **Complete test isolation** (each test gets clean state)
- ✅ **Independent test data** (no data dependencies between tests)
- ✅ **Easy maintenance** (simple local Docker setup)

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│               Docker Compose (Local)                        │
│  - PostgreSQL:5432   (persistent container)                 │
│  - Redis:6379        (persistent container)                 │
│  - Kafka:9092        (persistent container)                 │
│  - Zookeeper:2181    (for Kafka)                           │
└─────────────────────────────────────────────────────────────┘
                         ↑
                         │ Fixed ports
                         │
┌─────────────────────────────────────────────────────────────┐
│              Integration Tests                              │
│                                                             │
│  Each Test Method:                                         │
│  1. @Transactional → Start DB transaction                 │
│  2. testDataBuilder.create...() → Insert test data        │
│  3. Run API tests (POST/GET/DELETE)                       │
│  4. Auto-rollback → Clean database automatically          │
│                                                             │
│  Redis cleanup: Manual flush in @BeforeEach                │
└─────────────────────────────────────────────────────────────┘
```

---

## 📋 Setup Instructions

### Step 1: Start Local Docker Infrastructure

```bash
# Navigate to project root
cd /Users/yogesh/Documents/ws/food-app/tea-snacks-delivery-aggregator

# Start all infrastructure containers
docker-compose up -d

# Verify containers are running
docker-compose ps

# Expected output:
# postgresql    running    0.0.0.0:5432->5432/tcp
# redis         running    0.0.0.0:6379->6379/tcp
# kafka         running    0.0.0.0:9092->9092/tcp
# zookeeper     running    0.0.0.0:2181->2181/tcp
```

### Step 2: Run Integration Tests

```bash
# Run all integration tests for order-catalog-service
./gradlew :order-catalog-service:test --tests "*IntegrationTest*"

# Run specific test class
./gradlew :order-catalog-service:test --tests "CheckoutAPIIntegrationTest"

# Run single test method
./gradlew :order-catalog-service:test --tests "CheckoutAPIIntegrationTest.shouldCreateCheckoutSessionSuccessfully"

# Run with detailed output
./gradlew :order-catalog-service:test --tests "*IntegrationTest*" --info
```

### Step 3: Run Application Locally (Optional)

```bash
# Start the application (connects to local Docker)
./gradlew :order-catalog-service:bootRun

# Verify health
curl http://localhost:8082/actuator/health

# Test manually
curl -X POST http://localhost:8082/api/v1/checkout/calculate \
  -H "Content-Type: application/json" \
  -d '{...}'
```

---

## 🔧 How Test Isolation Works

### Database Isolation (PostgreSQL)

**Mechanism:** `@Transactional` annotation on `BaseIntegrationTest`

```java
@SpringBootTest
@ActiveProfiles("local-integration")
@Transactional  // ← KEY: Each test runs in a transaction
public abstract class BaseIntegrationTest {
    @Autowired
    protected TestDataBuilder testDataBuilder;
}
```

**How it works:**
1. **Test starts** → Spring opens a transaction
2. **Test creates data** → `testDataBuilder.createVendor()` inserts into DB
3. **Test runs** → API calls see the data (same transaction)
4. **Test ends** → Transaction **rolls back automatically**
5. **Data disappears** → Next test gets clean database

**Benefits:**
- ⚡ **No manual cleanup** needed
- 🔒 **Perfect isolation** between tests
- 🚀 **Fast** - no `DELETE` statements required

### Cache Isolation (Redis)

**Mechanism:** Manual flush in `@BeforeEach`

```java
@BeforeEach
void setUp() {
    cleanRedis();  // Flush all keys before each test
}

protected void cleanRedis() {
    redisTemplate.getConnectionFactory()
        .getConnection()
        .serverCommands()
        .flushAll();
}
```

**Why not @Transactional for Redis?**
- Redis is **not transactional** in the same way as PostgreSQL
- Simpler to just flush all keys (very fast operation)

### Message Queue Isolation (Kafka)

**Mechanism:** Each test uses unique consumer groups

```yaml
# application-local-integration.yml
spring:
  kafka:
    consumer:
      group-id: test-consumer-group-${random.uuid}
      auto-offset-reset: earliest
```

**How it works:**
- Each test run gets a unique consumer group
- Tests don't interfere with each other's messages
- Old messages are ignored (earliest offset reset)

---

## 🎨 Creating Test Data (Best Practices)

### Pattern 1: Use TestDataBuilder

**❌ BAD** (Hardcoded IDs - breaks test isolation):
```java
@Test
void testCheckout() {
    CheckoutRequest request = CheckoutRequest.builder()
        .vendorBranchId(1001L)  // ❌ Assumes data exists
        .cartItems(List.of(
            CartItemRequest.builder()
                .menuItemId(1L)  // ❌ Hardcoded
                .quantity(2)
                .build()
        ))
        .build();
}
```

**✅ GOOD** (Test-specific data):
```java
@Test
void testCheckout() {
    // Create test data for THIS test only
    TestVendor vendor = testDataBuilder.createVendor("Test Cafe", 19.076, 72.877);
    TestMenuItem chai = testDataBuilder.createMenuItem(vendor.branchId(), "Chai", 20.00);
    TestMenuItem samosa = testDataBuilder.createMenuItem(vendor.branchId(), "Samosa", 15.00);
    
    // Use the created data
    CheckoutRequest request = CheckoutRequest.builder()
        .vendorBranchId(vendor.branchId())  // ✅ Test-specific
        .cartItems(List.of(
            CartItemRequest.builder()
                .menuItemId(chai.menuItemId())  // ✅ Created just now
                .quantity(2)
                .build()
        ))
        .build();
    
    // After test completes, data is auto-deleted via @Transactional rollback
}
```

### Pattern 2: Complete Scenarios

For complex tests, use the scenario builder:

```java
@Test
void testCompleteOrderFlow() {
    // Create complete scenario with vendor + 2 menu items
    TestScenario scenario = testDataBuilder.createCompleteScenario();
    
    TestVendor vendor = scenario.vendor();
    TestMenuItem item1 = scenario.item1();
    TestMenuItem item2 = scenario.item2();
    
    // Use in test...
}
```

### Pattern 3: Custom Test Data

For specific test cases (e.g., testing unavailable items):

```java
@Test
void testUnavailableItem() {
    TestVendor vendor = testDataBuilder.createVendor();
    
    // Create unavailable item
    TestMenuItem unavailableItem = testDataBuilder.createMenuItem(
        vendor.branchId(),
        "Out of Stock Chai",
        "Test description",
        20.00,
        "BEVERAGES",
        false  // ← NOT available
    );
    
    // Test that checkout rejects unavailable items...
}
```

---

## 📊 Test Execution Flow

### Single Test Execution

```
1. Docker Compose already running ✓
   ↓
2. Test class loads (@SpringBootTest)
   ↓
3. Spring connects to localhost:5432, :6379, :9092 ✓
   ↓
4. Test method starts
   ↓
5. @Transactional → BEGIN transaction
   ↓
6. @BeforeEach → cleanRedis()
   ↓
7. Test code:
   - testDataBuilder.createVendor() → INSERT INTO vendor_branches
   - testDataBuilder.createMenuItem() → INSERT INTO menu_items
   - POST /api/v1/checkout/calculate → runs with test data
   - Assertions...
   ↓
8. Test method ends
   ↓
9. @Transactional → ROLLBACK (all INSERTs disappear)
   ↓
10. Next test starts with clean database ✓
```

### Full Test Suite Execution

```
$ ./gradlew :order-catalog-service:test --tests "*IntegrationTest*"

Running CheckoutAPIIntegrationTest
  ✓ Test 1: shouldCreateCheckoutSessionSuccessfully (2.3s)
     - Created vendor #12345
     - Created menu items #98765, #98766
     - Test passed
     - Data rolled back ✓
     
  ✓ Test 2: shouldRetrieveCheckoutSessionById (1.8s)
     - Created vendor #12346  ← NEW vendor, not #12345
     - Created menu items #98767, #98768
     - Test passed
     - Data rolled back ✓
     
  ✓ Test 3: shouldRejectInvalidVendor (1.2s)
     - Created vendor #12347
     - Test passed (correctly rejected)
     - Data rolled back ✓

Running PlaceOrderFromCheckoutIntegrationTest
  ✓ Test 1: shouldPlaceOrderFromCheckoutSession (3.1s)
     - Created vendor #12348  ← Still isolated!
     - Created menu items #98769, #98770
     - Test passed
     - Data rolled back ✓

Total: 42 tests, 42 passed, 0 failed
Time: 2m 15s
```

---

## 🚀 Performance Considerations

### Why This is Fast

| Approach | Container Startup | Test Execution | Cleanup |
|----------|------------------|----------------|---------|
| **Testcontainers** | ~30s per run | 2-3s per test | Automatic |
| **Local Docker** ✅ | 0s (already running) | 2-3s per test | Automatic |

**Time savings:**
- First run: **30 seconds saved**
- Subsequent runs: **30 seconds saved each time**
- 10 test runs per day = **5 minutes saved daily**

### Optimizations Applied

1. ✅ **Reuse containers** - Docker Compose containers stay running
2. ✅ **No Flyway clean** - Using `@Transactional` rollback instead
3. ✅ **Connection pooling** - HikariCP reuses connections
4. ✅ **Redis flush** - Very fast operation (~1ms)
5. ✅ **Parallel tests** - Can enable if needed (currently sequential for safety)

---

## 🔍 Debugging Tips

### Check Docker Containers

```bash
# View running containers
docker-compose ps

# Check PostgreSQL logs
docker-compose logs -f postgres

# Check Redis logs
docker-compose logs -f redis

# Check Kafka logs
docker-compose logs -f kafka

# Connect to PostgreSQL
docker exec -it postgres psql -U tea_snacks_user -d order_catalog_db

# Check if test data exists (should be empty after test rollback)
\dt
SELECT * FROM vendor_branches;
SELECT * FROM menu_items;
```

### Check Application Logs

```bash
# Run tests with debug logging
./gradlew :order-catalog-service:test --tests "*IntegrationTest*" --debug

# Check specific test logs
cat tea-snacks-delivery-aggregator/order-catalog-service/build/reports/tests/test/index.html
```

### Verify Test Isolation

```bash
# Run same test twice - should both pass with fresh data
./gradlew :order-catalog-service:test --tests "CheckoutAPIIntegrationTest.shouldCreateCheckoutSessionSuccessfully"
./gradlew :order-catalog-service:test --tests "CheckoutAPIIntegrationTest.shouldCreateCheckoutSessionSuccessfully"

# Both should pass independently ✓
```

---

## 📝 Maintenance Checklist

### Daily Development

- [ ] `docker-compose up -d` (once per day)
- [ ] Run tests: `./gradlew :order-catalog-service:test`
- [ ] Fix any failures
- [ ] Commit code

### Weekly

- [ ] Restart containers: `docker-compose restart`
- [ ] Clear old volumes: `docker volume prune`
- [ ] Review test execution times
- [ ] Update test data builders if entities change

### Before Deployment

- [ ] Run full test suite: `./gradlew test`
- [ ] Run application locally: `./gradlew :order-catalog-service:bootRun`
- [ ] Manually test critical flows
- [ ] Review logs for errors/warnings
- [ ] Check database migrations: `./gradlew flywayInfo`

---

## 🎯 Test Data Strategy Summary

### Principles

1. **Zero Shared State** - Each test creates its own data
2. **Auto Cleanup** - `@Transactional` rollback handles it
3. **Descriptive Names** - Use UUIDs in names for debugging
4. **Realistic Data** - Mirror production scenarios

### Data Creation Patterns

```java
// Pattern 1: Quick scenario
TestScenario scenario = testDataBuilder.createCompleteScenario();

// Pattern 2: Custom vendor location
TestVendor vendor = testDataBuilder.createVendor("Cafe", 19.076, 72.877);

// Pattern 3: Specific menu item
TestMenuItem item = testDataBuilder.createMenuItem(
    vendor.branchId(),
    "Masala Chai",
    "Authentic Indian tea",
    20.00,
    "BEVERAGES",
    true
);

// Pattern 4: Multiple items
TestMenuItem chai = testDataBuilder.createMenuItem(branchId, "Chai", 20.00);
TestMenuItem samosa = testDataBuilder.createMenuItem(branchId, "Samosa", 15.00);
TestMenuItem pakora = testDataBuilder.createMenuItem(branchId, "Pakora", 25.00);
```

---

## 🏆 Best Practices

### ✅ DO

- ✅ Create test data at the start of each test method
- ✅ Use `testDataBuilder` for all data creation
- ✅ Use descriptive names with UUIDs for debugging
- ✅ Test one scenario per test method
- ✅ Run tests frequently during development
- ✅ Keep tests fast (< 5 seconds per test)

### ❌ DON'T

- ❌ Use hardcoded IDs (1L, 1001L, etc.)
- ❌ Assume data exists from previous tests
- ❌ Share test data between tests
- ❌ Manually clean up data (let `@Transactional` handle it)
- ❌ Use `@DirtiesContext` (too slow)
- ❌ Use Testcontainers for local testing (slower)

---

## 📈 Expected Test Results

### Target Metrics

| Metric | Target | Current |
|--------|--------|---------|
| Test Execution Time | < 5 min for full suite | ~2-3 min ✓ |
| Single Test Time | < 3 seconds | ~2 seconds ✓ |
| Pass Rate | 100% | ✓ |
| Test Isolation | Complete | ✓ |

### Coverage Goals

- ✅ Happy path scenarios
- ✅ Validation errors
- ✅ Business rule violations
- ✅ Concurrent operations
- ✅ Edge cases

---

## 🔄 Workflow Summary

```bash
# Morning: Start Docker
docker-compose up -d

# Development: Run tests frequently
./gradlew :order-catalog-service:test --tests "*IntegrationTest*"

# Fix any failures, repeat

# Before commit: Run all tests
./gradlew :order-catalog-service:test

# Evening: Stop Docker (optional - can leave running)
docker-compose stop

# Weekend: Clean up
docker-compose down
docker volume prune -f
```

---

## 🎓 Key Takeaways

1. **Local Docker > Testcontainers** for local development (30s faster)
2. **@Transactional = Automatic cleanup** (no manual DELETE statements)
3. **TestDataBuilder = Test isolation** (each test creates its own data)
4. **Independent tests = Reliable tests** (can run in any order)
5. **Fast feedback loop = Happy developers** (2-3 min full suite)

---

## 📚 Additional Resources

- `BaseIntegrationTest.java` - Base class for all integration tests
- `TestDataBuilder.java` - Utility for creating test data
- `application-local-integration.yml` - Test configuration
- `docker-compose.yml` - Local infrastructure setup

---

**Last Updated:** November 16, 2025  
**Author:** Order Catalog Service Team

