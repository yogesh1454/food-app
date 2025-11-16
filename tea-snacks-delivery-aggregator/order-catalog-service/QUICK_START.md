# ⚡ Quick Start Guide - Integration Testing

## 🚀 TL;DR - Run Tests in 30 Seconds

```bash
# 1. Start Docker (once per day)
cd /Users/yogesh/Documents/ws/food-app/tea-snacks-delivery-aggregator
docker-compose up -d

# 2. Run all tests
cd order-catalog-service
./run-tests.sh

# Or use Gradle directly:
cd ..
./gradlew :order-catalog-service:test --tests "*IntegrationTest*"
```

---

## 📁 Key Files

| File | Purpose |
|------|---------|
| `run-tests.sh` | 🎯 Test runner script (easiest way) |
| `LOCAL_TESTING_STRATEGY.md` | 📖 Complete testing guide |
| `TESTING_APPROACHES_COMPARISON.md` | 🔍 Testcontainers vs Local Docker explanation |
| `BaseIntegrationTest.java` | 🧩 Base class for all tests |
| `TestDataBuilder.java` | 🏗️ Creates isolated test data |

---

## 🎯 Common Commands

### Start Infrastructure
```bash
docker-compose up -d          # Start all containers
docker-compose ps             # Check status
docker-compose logs -f        # View logs
docker-compose stop           # Stop containers
docker-compose down           # Stop + remove containers
```

### Run Tests
```bash
./run-tests.sh                # All tests
./run-tests.sh checkout       # Only CheckoutAPI tests
./run-tests.sh place-order    # Only PlaceOrder tests
./run-tests.sh setup          # Setup Docker infrastructure

# Or with Gradle:
./gradlew :order-catalog-service:test
./gradlew :order-catalog-service:test --tests "CheckoutAPIIntegrationTest"
```

### Run Application
```bash
./gradlew :order-catalog-service:bootRun
curl http://localhost:8082/actuator/health
```

---

## 🔍 Architecture at a Glance

```
Docker Compose (localhost)
  - PostgreSQL:5432
  - Redis:6379
  - Kafka:9092
         ↑
         │ Fixed ports
         │
Integration Tests
  - Each test: BEGIN TX → Create data → Test → ROLLBACK
  - Result: Clean database for every test
  - Speed: ~2 seconds per test
```

---

## ✅ Why This Approach?

**You asked:** "Best way for local testing with independent test cases"

**Answer:** Local Docker + @Transactional

**Benefits:**
- ⚡ **30-60 seconds faster** than Testcontainers per run
- 🔒 **Complete test isolation** via transaction rollback
- 🎯 **Independent data** via TestDataBuilder
- 🛠️ **Easy debugging** with fixed ports

---

## 🎨 Writing Tests

### Pattern 1: Use TestDataBuilder

```java
@Test
void myTest() {
    // Create test data
    TestVendor vendor = testDataBuilder.createVendor("Test Cafe", 19.076, 72.877);
    TestMenuItem chai = testDataBuilder.createMenuItem(vendor.branchId(), "Chai", 20.00);
    
    // Use in test
    CheckoutRequest request = CheckoutRequest.builder()
        .vendorBranchId(vendor.branchId())
        .cartItems(List.of(
            CartItemRequest.builder()
                .menuItemId(chai.menuItemId())
                .quantity(2)
                .build()
        ))
        .build();
    
    // After test: data auto-cleaned by @Transactional rollback
}
```

### Pattern 2: Use Complete Scenario

```java
@Test
void myTest() {
    // Quick setup
    TestScenario scenario = testDataBuilder.createCompleteScenario();
    
    TestVendor vendor = scenario.vendor();
    TestMenuItem item1 = scenario.item1();
    TestMenuItem item2 = scenario.item2();
    
    // Use in test...
}
```

---

## 🐛 Debugging

### Check Docker
```bash
docker-compose ps                    # Are containers running?
docker-compose logs postgres         # PostgreSQL logs
docker-compose logs redis            # Redis logs
docker exec -it postgres psql -U tea_snacks_user -d order_catalog_db
```

### Check Test Data
```sql
-- Connect to database
docker exec -it postgres psql -U tea_snacks_user -d order_catalog_db

-- Should be empty (after test rollback)
SELECT * FROM vendor_branches;
SELECT * FROM menu_items;
```

### Check Application
```bash
# Health check
curl http://localhost:8082/actuator/health

# Manual test
curl -X POST http://localhost:8082/api/v1/checkout/calculate \
  -H "Content-Type: application/json" \
  -d @test-data/checkout-request.json
```

---

## 📊 Expected Results

```bash
$ ./run-tests.sh

═══════════════════════════════════════════════════
  Order Catalog Service - Integration Test Runner  
═══════════════════════════════════════════════════

[1/3] Checking Docker containers...
✓ Docker containers are running

[2/3] Running integration tests...
Test filter: All Integration Tests

> Task :order-catalog-service:test
CheckoutAPIIntegrationTest
  ✓ Test 1: shouldCreateCheckoutSessionSuccessfully (2.1s)
  ✓ Test 2: shouldRetrieveCheckoutSessionById (1.8s)
  ✓ Test 3: shouldDemonstrateSessionIdempotency (2.3s)
  ...
  ✓ Test 18: shouldHandleValidationErrors (1.5s)

PlaceOrderFromCheckoutIntegrationTest
  ✓ Test 1: shouldPlaceOrderFromValidCheckoutSession (2.8s)
  ...
  ✓ Test 15: shouldValidatePaymentInfo (1.9s)

BUILD SUCCESSFUL in 2m 15s
42 tests completed, 42 passed

[3/3] Test results
✓ All tests passed!
```

---

## 🎓 Key Concepts

### Testcontainers vs Local Docker

| Aspect | Testcontainers | Local Docker (✅ You) |
|--------|---------------|---------------------|
| Speed | Slow (30s+) | Fast (instant) |
| Setup | Automatic | Manual (docker-compose up) |
| Ports | Random | Fixed (5432, 6379, 9092) |
| Use Case | CI/CD | Local development |

### Test Isolation

**How data is cleaned:**
```
Test 1: BEGIN TX → INSERT vendor, menu → Test → ROLLBACK (data deleted)
Test 2: BEGIN TX → INSERT vendor, menu → Test → ROLLBACK (data deleted)
Test 3: BEGIN TX → INSERT vendor, menu → Test → ROLLBACK (data deleted)

Result: Each test sees empty database ✓
```

**Key:** `@Transactional` annotation on `BaseIntegrationTest`

---

## ⚠️ Common Issues

### Issue: "Connection refused to localhost:5432"
**Fix:** Start Docker containers
```bash
docker-compose up -d
```

### Issue: "Port 5432 already in use"
**Fix:** Stop conflicting PostgreSQL
```bash
brew services stop postgresql
# or
sudo systemctl stop postgresql
```

### Issue: "Tests fail with old data"
**Fix:** Tests should be isolated via @Transactional (check BaseIntegrationTest)

### Issue: "Flyway migration failed"
**Fix:** Clean database and restart
```bash
docker-compose down -v
docker-compose up -d
```

---

## 📚 Learn More

- **Full guide:** `LOCAL_TESTING_STRATEGY.md`
- **Comparison:** `TESTING_APPROACHES_COMPARISON.md`
- **Test examples:** `src/test/java/*/integration/`

---

## ✨ You're Ready!

```bash
# Your daily workflow:
docker-compose up -d      # Once per day
./run-tests.sh            # Every time you make changes
./gradlew bootRun         # To run the application

# That's it! 🎉
```

