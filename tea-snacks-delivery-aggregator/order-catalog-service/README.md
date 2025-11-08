# Order Catalog Service (OCS)

This service manages the food catalog and ordering operations including:
- Menu management and item catalog
- Restaurant and vendor management
- Order processing and validation
- Inventory tracking
- Pricing and promotions

## Table of Contents
- [Prerequisites](#prerequisites)
- [Local Development Setup](#local-development-setup)
- [Running the Application](#running-the-application)
- [Running Tests](#running-tests)
- [API Documentation](#api-documentation)
- [Configuration](#configuration)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

Before running the Order Catalog Service, ensure you have the following installed:

- **Java 21** or higher
- **Gradle 8.x** (wrapper included)
- **Docker** and **Docker Compose** (for infrastructure)
- **PostgreSQL 15+** (via Docker)
- **Redis 7+** (via Docker)
- **Kafka** (optional, via Docker)

---

## Local Development Setup

### 1. Start Infrastructure Services

The service requires PostgreSQL and Redis to be running. Start them using Docker Compose:

```bash
# Navigate to infrastructure directory
cd /Users/yogesh/Documents/ws/food-app/infrastructure/docker

# Start PostgreSQL and Redis
docker-compose up -d postgres redis

# Verify services are running
docker ps | grep -E "postgres|redis"
```

**Expected output:**
```
tea-snacks-postgres   Up   0.0.0.0:5432->5432/tcp
tea-snacks-redis      Up   0.0.0.0:6379->6379/tcp
```

### 2. Verify Database

Check that the `order_catalog_db` database exists:

```bash
docker exec tea-snacks-postgres psql -U tea_snacks_user -d order_catalog_db -c "\l"
```

### 3. Configure Application

The service uses `application.yml` for local development with these defaults:

- **Server Port:** 8082
- **Database:** localhost:5432/order_catalog_db
- **Database User:** tea_snacks_user
- **Database Password:** tea_snacks_password
- **Redis:** localhost:6379
- **Kafka:** localhost:9092 (optional)

No changes needed if using the default Docker setup.

---

## Running the Application

### Option 1: Using Gradle (Recommended for Development)

```bash
# Navigate to project root
cd /Users/yogesh/Documents/ws/food-app/tea-snacks-delivery-aggregator

# Run the service
./gradlew :order-catalog-service:bootRun
```

**Expected output:**
```
Started OrderCatalogApplication in 2.658 seconds (process running for 2.841)
```

The service will be available at: **http://localhost:8082**

### Option 2: Using JAR File

```bash
# Build the JAR
./gradlew :order-catalog-service:build

# Run the JAR
java -jar order-catalog-service/build/libs/order-catalog-service-0.0.1-SNAPSHOT.jar
```

### Option 3: With Custom Profile

```bash
# Run with specific profile
./gradlew :order-catalog-service:bootRun --args='--spring.profiles.active=dev'

# Run with custom port
./gradlew :order-catalog-service:bootRun --args='--server.port=8081'
```

### Verify Application is Running

```bash
# Health check
curl http://localhost:8082/actuator/health

# Expected response
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "redis": { "status": "UP" }
  }
}
```

---

## Running Tests

### Run All Tests

```bash
cd /Users/yogesh/Documents/ws/food-app/tea-snacks-delivery-aggregator

# Run all tests in the service
./gradlew :order-catalog-service:test
```

### Run Specific Test Classes

```bash
# Run Menu E2E Tests (40 tests covering 125 use cases)
./gradlew :order-catalog-service:test --tests MenuItemOperationsCompleteE2ETest

# Run Vendor Onboarding E2E Tests
./gradlew :order-catalog-service:test --tests VendorBranchOnboardingE2ETest

# Run with detailed output
./gradlew :order-catalog-service:test --tests MenuItemOperationsCompleteE2ETest --info
```

### Run Tests with Coverage

```bash
# Run tests with JaCoCo coverage report
./gradlew :order-catalog-service:test jacocoTestReport

# View coverage report
open order-catalog-service/build/reports/jacoco/test/html/index.html
```

### Test Reports

After running tests, view the HTML report:

```bash
# Open test report in browser
open order-catalog-service/build/reports/tests/test/index.html
```

**Test Suite Summary:**
- **Menu E2E Tests:** 40 tests covering 125 use cases
- **Vendor Onboarding Tests:** Complete vendor and branch lifecycle
- **Unit Tests:** Service layer and repository tests

---

## API Documentation

### Available Endpoints

#### Vendor Management
- `POST /api/v1/vendors` - Register new vendor
- `GET /api/v1/vendors/{vendorId}` - Get vendor details
- `PUT /api/v1/vendors/{vendorId}` - Update vendor
- `DELETE /api/v1/vendors/{vendorId}` - Delete vendor

#### Branch Management
- `POST /api/v1/vendors/{vendorId}/branches` - Create branch
- `GET /api/v1/branches/{branchId}` - Get branch details
- `PUT /api/v1/branches/{branchId}` - Update branch
- `DELETE /api/v1/branches/{branchId}` - Delete branch

#### Menu Management
- `POST /api/v1/menu-items` - Create menu item
- `GET /api/v1/menu-items/{menuItemId}` - Get menu item
- `PUT /api/v1/menu-items/{menuItemId}` - Update menu item
- `DELETE /api/v1/menu-items/{menuItemId}` - Soft delete menu item
- `GET /api/v1/branches/{branchId}/menu` - Get branch menu

#### Health & Monitoring
- `GET /actuator/health` - Health check
- `GET /actuator/info` - Application info
- `GET /actuator/prometheus` - Prometheus metrics

### Swagger UI

If Swagger is enabled, access the interactive API documentation at:

```
http://localhost:8082/swagger-ui.html
```

---

## Configuration

### Local Development (`application.yml`)

```yaml
server:
  port: 8082

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/order_catalog_db
    username: tea_snacks_user
    password: tea_snacks_password
  
  data:
    redis:
      host: localhost
      port: 6379
  
  kafka:
    bootstrap-servers: localhost:9092
```

### Docker Deployment (`application-docker.yml`)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://tea-snacks-postgres:5432/tea_snacks_db
    username: tea_snacks_user
    password: tea_snacks_password
  
  data:
    redis:
      host: tea-snacks-redis
      port: 6379
  
  kafka:
    bootstrap-servers: tea-snacks-kafka:29092
```

### Environment Variables

You can override configuration using environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/custom_db
export SPRING_DATASOURCE_USERNAME=custom_user
export SPRING_DATASOURCE_PASSWORD=custom_password
export SERVER_PORT=8083

./gradlew :order-catalog-service:bootRun
```

---

## Troubleshooting

### Issue: Application fails to start with "Failed to configure a DataSource"

**Solution:**
1. Verify PostgreSQL is running:
   ```bash
   docker ps | grep postgres
   ```
2. Check database exists:
   ```bash
   docker exec tea-snacks-postgres psql -U tea_snacks_user -l | grep order_catalog_db
   ```
3. Verify credentials in `application.yml` match Docker setup

### Issue: Port 8082 already in use

**Solution:**
1. Find and kill the process:
   ```bash
   lsof -ti:8082
   kill -9 $(lsof -ti:8082)
   ```
2. Or use a different port:
   ```bash
   ./gradlew :order-catalog-service:bootRun --args='--server.port=8083'
   ```

### Issue: Tests failing with database connection errors

**Solution:**
1. Tests use `application-test.yml` profile
2. Ensure PostgreSQL is running
3. Check test database exists: `order_catalog_test_db`
4. Run with `--info` flag for detailed logs:
   ```bash
   ./gradlew :order-catalog-service:test --info
   ```

### Issue: Redis connection timeout

**Solution:**
1. Verify Redis is running:
   ```bash
   docker ps | grep redis
   ```
2. Test Redis connection:
   ```bash
   docker exec tea-snacks-redis redis-cli ping
   # Expected: PONG
   ```

### Issue: Flyway migration errors

**Solution:**
1. Check migration files in `src/main/resources/db/migration/`
2. Verify migration version numbers are sequential
3. Clean and rebuild:
   ```bash
   ./gradlew :order-catalog-service:clean build
   ```

---

## Development Workflow

### 1. Make Code Changes

Edit your Java files in `src/main/java/`

### 2. Run Tests

```bash
./gradlew :order-catalog-service:test
```

### 3. Run Application Locally

```bash
./gradlew :order-catalog-service:bootRun
```

### 4. Test API Endpoints

```bash
# Create a vendor
curl -X POST http://localhost:8082/api/v1/vendors \
  -H "Content-Type: application/json" \
  -d '{
    "brandName": "Test Vendor",
    "companyName": "Test Company",
    "companyEmail": "test@example.com"
  }'
```

### 5. Stop Application

Press `Ctrl+C` in the terminal running the application

---

## Quick Reference

### Common Commands

```bash
# Start infrastructure
cd infrastructure/docker && docker-compose up -d postgres redis

# Run application
cd tea-snacks-delivery-aggregator
./gradlew :order-catalog-service:bootRun

# Run all tests
./gradlew :order-catalog-service:test

# Run specific test
./gradlew :order-catalog-service:test --tests MenuItemOperationsCompleteE2ETest

# Build JAR
./gradlew :order-catalog-service:build

# Clean build
./gradlew :order-catalog-service:clean build

# Check health
curl http://localhost:8082/actuator/health

# Stop infrastructure
cd infrastructure/docker && docker-compose down
```

---

## Additional Resources

- **Use Cases:** See `docs/use-cases/BRANCH_MENU_OPERATIONS_USECASES_V2.md` for detailed use cases
- **E2E Tests:** See `docs/testing/E2E_TEST_IMPLEMENTATION_SUMMARY.md` for test documentation
- **Architecture:** See project root documentation for overall architecture

---

## Support

For issues or questions:
1. Check the [Troubleshooting](#troubleshooting) section
2. Review test logs in `build/reports/tests/test/`
3. Check application logs for detailed error messages
