# Order Catalog Service - Infrastructure Prerequisites

**Service:** order-catalog-service  
**Port:** 8082  
**Last Updated:** November 16, 2025

---

## ✅ Integration Status

### Redis Integration
- **Status:** ✅ **FULLY INTEGRATED**
- **Configuration:** `RedisConfig.java`
- **Template:** `RedisTemplate<String, Object>` configured
- **Serialization:** 
  - Keys: `StringRedisSerializer`
  - Values: `GenericJackson2JsonRedisSerializer`
- **Features:**
  - Keyspace notifications enabled for expired events
  - RedisMessageListenerContainer configured
- **Usage:**
  - ✅ Checkout session management (`SessionManagementService`)
  - ✅ Menu item caching (`MenuCacheService`)
  - ✅ FSM state caching (`StateCacheService`)
  - ✅ Order timeout tracking (`OrderTimeoutService`)

### Kafka Integration
- **Status:** ✅ **FULLY INTEGRATED**
- **Configuration:** `KafkaProducerConfig.java`
- **Template:** `KafkaTemplate<String, Object>` configured
- **Serialization:**
  - Keys: `StringSerializer`
  - Values: `JsonSerializer`
- **Producer Features:**
  - ✅ Idempotent producer (exactly-once semantics)
  - ✅ Acks = all (ensures durability)
  - ✅ Retries: 3 with exponential backoff
  - ✅ Compression: Snappy
  - ✅ Batching enabled (10ms linger, 16KB batch size)
- **Usage:**
  - ✅ Order events (`OrderEventPublisher`)
    - `order-placed-events` topic
    - `payment-completed-events` topic
    - `order-state-changed-events` topic
  - ✅ FSM events (`EventPublisher`)
  - ✅ Delivery events (via FSM)

---

## 🔧 Required Infrastructure Components

### 1. PostgreSQL Database ✅
**Purpose:** Primary data store for orders, vendors, menu items

```yaml
Host: localhost
Port: 5432
Database: order_catalog_db
Username: tea_snacks_user
Password: tea_snacks_password
```

**Schema Management:**
- Flyway migrations enabled
- Baseline on migrate: true
- DDL auto: validate

**Extensions Required:**
- PostGIS (for geospatial queries)

**Setup Command:**
```bash
# Create database
createdb -U postgres order_catalog_db

# Create user
psql -U postgres -c "CREATE USER tea_snacks_user WITH PASSWORD 'tea_snacks_password';"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE order_catalog_db TO tea_snacks_user;"

# Enable PostGIS
psql -U postgres -d order_catalog_db -c "CREATE EXTENSION IF NOT EXISTS postgis;"
```

---

### 2. Redis ✅
**Purpose:** Session management, caching, timeout tracking

```yaml
Host: localhost
Port: 6379
Timeout: 2000ms
```

**Configuration Required:**
```bash
# Enable keyspace notifications for expired events
redis-cli CONFIG SET notify-keyspace-events Ex
```

**Data Stored:**
- Checkout sessions (15-min TTL)
- Menu item cache
- FSM state cache
- Order timeout tracking

**Setup Command:**
```bash
# Start Redis
redis-server

# Verify connection
redis-cli ping
# Should return: PONG

# Enable keyspace notifications
redis-cli CONFIG SET notify-keyspace-events Ex
```

---

### 3. Apache Kafka ✅
**Purpose:** Event streaming, async communication

```yaml
Bootstrap Servers: localhost:9092
Producer Group: order-catalog-group
```

**Topics Required:**
1. `order-placed-events` - Order creation events
2. `payment-completed-events` - Payment completion events
3. `order-state-changed-events` - Order state transitions
4. `delivery-events` - Delivery lifecycle events

**Setup Command:**
```bash
# Start Zookeeper
zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties

# Start Kafka
kafka-server-start /usr/local/etc/kafka/server.properties

# Create topics
kafka-topics --create --topic order-placed-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics --create --topic payment-completed-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics --create --topic order-state-changed-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics --create --topic delivery-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# Verify topics
kafka-topics --list --bootstrap-server localhost:9092
```

---

## 🐳 Docker Compose Setup (Recommended)

Create `docker-compose.yml` in the project root:

```yaml
version: '3.8'

services:
  postgres:
    image: postgis/postgis:15-3.3
    container_name: order-catalog-postgres
    environment:
      POSTGRES_DB: order_catalog_db
      POSTGRES_USER: tea_snacks_user
      POSTGRES_PASSWORD: tea_snacks_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U tea_snacks_user -d order_catalog_db"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: order-catalog-redis
    ports:
      - "6379:6379"
    command: redis-server --notify-keyspace-events Ex
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    container_name: order-catalog-zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    container_name: order-catalog-kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
    healthcheck:
      test: ["CMD", "kafka-broker-api-versions", "--bootstrap-server", "localhost:9092"]
      interval: 10s
      timeout: 10s
      retries: 5

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: order-catalog-kafka-ui
    depends_on:
      - kafka
    ports:
      - "8080:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:9092
      KAFKA_CLUSTERS_0_ZOOKEEPER: zookeeper:2181

volumes:
  postgres_data:
```

**Start All Services:**
```bash
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

---

## 🚀 Starting the Application

### Prerequisites Check
```bash
# 1. Check PostgreSQL
psql -U tea_snacks_user -d order_catalog_db -c "SELECT version();"

# 2. Check Redis
redis-cli ping

# 3. Check Kafka
kafka-topics --list --bootstrap-server localhost:9092
```

### Start Application
```bash
# From project root
cd tea-snacks-delivery-aggregator

# Build
./gradlew :order-catalog-service:build

# Run
./gradlew :order-catalog-service:bootRun

# Or using JAR
java -jar order-catalog-service/build/libs/order-catalog-service-0.0.1-SNAPSHOT.jar
```

### Verify Application Health
```bash
# Health check
curl http://localhost:8082/actuator/health

# Swagger UI
open http://localhost:8082/swagger-ui.html

# Prometheus metrics
curl http://localhost:8082/actuator/prometheus
```

---

## 📊 Monitoring & Observability

### Actuator Endpoints
- **Health:** `http://localhost:8082/actuator/health`
  - Database health check enabled
  - Redis health check disabled (optional)
  - Kafka health check disabled (optional)
- **Info:** `http://localhost:8082/actuator/info`
- **Metrics:** `http://localhost:8082/actuator/prometheus`

### Readiness & Liveness Probes
- **Readiness:** Checks database connectivity
- **Liveness:** Simple ping check

### Kafka UI
- **URL:** `http://localhost:8080` (if using docker-compose)
- **Features:**
  - View topics and messages
  - Monitor consumer groups
  - Inspect message payloads

---

## 🔍 Troubleshooting

### PostgreSQL Connection Issues
```bash
# Check if PostgreSQL is running
pg_isready -h localhost -p 5432

# Check database exists
psql -U postgres -l | grep order_catalog_db

# Check user permissions
psql -U postgres -c "\du tea_snacks_user"
```

### Redis Connection Issues
```bash
# Check if Redis is running
redis-cli ping

# Check keyspace notifications
redis-cli CONFIG GET notify-keyspace-events

# Monitor Redis commands
redis-cli MONITOR
```

### Kafka Connection Issues
```bash
# Check if Kafka is running
kafka-broker-api-versions --bootstrap-server localhost:9092

# List topics
kafka-topics --list --bootstrap-server localhost:9092

# Consume messages from a topic
kafka-console-consumer --bootstrap-server localhost:9092 --topic order-placed-events --from-beginning
```

### Application Startup Issues
```bash
# Check logs
tail -f order-catalog-service/logs/application.log

# Check port availability
lsof -i :8082

# Check Java version
java -version  # Should be Java 21
```

---

## 📦 Dependencies Summary

### Runtime Dependencies
- ✅ PostgreSQL 15+ with PostGIS extension
- ✅ Redis 7+
- ✅ Apache Kafka 3.5+
- ✅ Java 21

### Optional Dependencies
- Kafka UI (for monitoring)
- Prometheus (for metrics)
- Grafana (for dashboards)

---

## 🎯 Quick Start Checklist

- [ ] PostgreSQL running on port 5432
- [ ] Database `order_catalog_db` created
- [ ] User `tea_snacks_user` created with permissions
- [ ] PostGIS extension enabled
- [ ] Redis running on port 6379
- [ ] Redis keyspace notifications enabled
- [ ] Kafka running on port 9092
- [ ] Required Kafka topics created
- [ ] Java 21 installed
- [ ] Application builds successfully
- [ ] Application starts on port 8082
- [ ] Health check returns UP
- [ ] Swagger UI accessible

---

## 📝 Configuration Files

### application.yml
Location: `src/main/resources/application.yml`

Key configurations:
- Server port: 8082
- Database URL, credentials
- Redis host, port
- Kafka bootstrap servers
- Actuator endpoints
- Logging levels

### Environment Variables (Optional)
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/order_catalog_db
export SPRING_DATASOURCE_USERNAME=tea_snacks_user
export SPRING_DATASOURCE_PASSWORD=tea_snacks_password
export SPRING_DATA_REDIS_HOST=localhost
export SPRING_DATA_REDIS_PORT=6379
export SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

---

## 🔗 Related Documentation

- [Order Creation Implementation Status](ORDER_CREATION_IMPLEMENTATION_STATUS.md)
- [Checkout API README](src/main/java/com/teadelivery/ordercatalog/order/checkout/README.md)
- [Create Order API Requirements](docs/business-flows/08_CREATE_ORDER_API_REQUIREMENTS.md)

---

**Last Verified:** November 16, 2025  
**Service Version:** 0.0.1-SNAPSHOT  
**Spring Boot Version:** 3.2.3
