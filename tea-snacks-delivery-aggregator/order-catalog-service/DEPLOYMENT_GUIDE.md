# Order Catalog Service - AWS EC2 Deployment Guide

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Build the Application](#build-the-application)
3. [Deploy to AWS EC2](#deploy-to-aws-ec2)
4. [Run the Application](#run-the-application)
5. [Access Swagger UI](#access-swagger-ui)
6. [Testing the APIs](#testing-the-apis)
7. [Monitoring and Logs](#monitoring-and-logs)
8. [Troubleshooting](#troubleshooting)
9. [Stopping the Application](#stopping-the-application)

---

## Prerequisites

### ✅ What You Need:

1. **AWS EC2 Instance Running**
   - Instance Type: `t2.micro` (or larger)
   - OS: Amazon Linux 2023
   - Java 21 installed
   - Security Group allows ports: `22` (SSH), `8080` (HTTP)

2. **SSH Key Pair**
   - Location: `infrastructure/cloudformation/nastto-key.pem`
   - Permissions: `chmod 400 nastto-key.pem`

3. **AWS Infrastructure Deployed**
   - RDS PostgreSQL: Running
   - ElastiCache Redis: Running
   - S3 Buckets: Created
   - SQS/SNS: Created (optional, not used yet)

4. **Your IP Address**
   - Current IP: `49.207.218.102` (should be whitelisted in EC2 Security Group)

---

## 🔨 Build the Application

### Step 1: Navigate to Project Directory

```bash
cd /Users/yogesh/Documents/ws/food-app/tea-snacks-delivery-aggregator
```

### Step 2: Clean and Build

```bash
./gradlew :order-catalog-service:clean :order-catalog-service:bootJar
```

**Output:**
```
BUILD SUCCESSFUL in 10s
```

### Step 3: Verify JAR Created

```bash
ls -lh order-catalog-service/build/libs/

# Expected output:
# order-catalog-service-0.0.1-SNAPSHOT.jar (~100MB)
```

---

## 🚀 Deploy to AWS EC2

### Step 1: Copy JAR to EC2

```bash
# From project root
cd /Users/yogesh/Documents/ws/food-app

# Copy JAR file
scp -i infrastructure/cloudformation/nastto-key.pem \
    tea-snacks-delivery-aggregator/order-catalog-service/build/libs/order-catalog-service-0.0.1-SNAPSHOT.jar \
    ec2-user@13.223.13.132:/home/ec2-user/
```

**Expected Output:**
```
order-catalog-service-0.0.1-SNAPSHOT.jar    100%   95MB   2.1MB/s   00:45
```

### Step 2: SSH into EC2

```bash
ssh -i infrastructure/cloudformation/nastto-key.pem ec2-user@13.223.13.132
```

### Step 3: Setup Application Directory on EC2

```bash
# Create app directory
sudo mkdir -p /opt/nashtto
sudo chown ec2-user:ec2-user /opt/nashtto

# Move JAR to app directory
mv /home/ec2-user/order-catalog-service-0.0.1-SNAPSHOT.jar /opt/nashtto/

# Navigate to app directory
cd /opt/nashtto
```

### Step 4: Verify JAR Contains Production Config

The JAR file already contains `application-prod.yml` with AWS RDS and Redis configuration:

**✅ Pre-configured with:**
- RDS PostgreSQL: `nashtto-postgres.c2z440siod1m.us-east-1.rds.amazonaws.com`
- ElastiCache Redis: `nas-re-nqw16wn8wkhi.qoapqv.0001.use1.cache.amazonaws.com`
- Kafka: Disabled
- Logging: `/opt/nashtto/logs/application.log`

**No additional configuration files needed!** Just use `-Dspring.profiles.active=prod` flag.

---

## ▶️ Run the Application

### Option A: Run in Foreground (for testing)

```bash
cd /opt/nashtto
java -jar -Dspring.profiles.active=prod \
     order-catalog-service-0.0.1-SNAPSHOT.jar
```

**Watch for:**
```
✅ Started OrderCatalogApplication in X.XXX seconds
✅ Tomcat started on port(s): 8080 (http)
```

Press `Ctrl+C` to stop.

---

### Option B: Run as Background Service (Recommended for Production)

#### Create Systemd Service

```bash
sudo tee /etc/systemd/system/nashtto-order-service.service > /dev/null << 'EOF'
[Unit]
Description=Nashtto Order Catalog Service
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/opt/nashtto
ExecStart=/usr/bin/java \
  -Xms512m -Xmx1024m \
  -Dspring.profiles.active=prod \
  -jar /opt/nashtto/order-catalog-service-0.0.1-SNAPSHOT.jar
Restart=on-failure
RestartSec=10
StandardOutput=append:/opt/nashtto/logs/application.log
StandardError=append:/opt/nashtto/logs/error.log

[Install]
WantedBy=multi-user.target
EOF
```

#### Start the Service

```bash
# Create logs directory
mkdir -p /opt/nashtto/logs

# Reload systemd
sudo systemctl daemon-reload

# Start service
sudo systemctl start nashtto-order-service

# Enable auto-start on boot
sudo systemctl enable nashtto-order-service

# Check status
sudo systemctl status nashtto-order-service
```

**Expected Output:**
```
● nashtto-order-service.service - Nashtto Order Catalog Service
   Loaded: loaded (/etc/systemd/system/nashtto-order-service.service; enabled)
   Active: active (running) since Mon 2025-11-25 18:30:00 UTC; 10s ago
```

#### Useful Service Commands

```bash
# Stop service
sudo systemctl stop nashtto-order-service

# Restart service
sudo systemctl restart nashtto-order-service

# View logs
journalctl -u nashtto-order-service -f

# View application logs
tail -f /opt/nashtto/logs/application.log
```

---

## 🌐 Access Swagger UI

### From Your Local Machine

Once the application is running on EC2, access Swagger UI at:

```
http://13.223.13.132:8080/swagger-ui.html
```

**Or using domain (if DNS is configured):**

```
http://nashtto.com:8080/swagger-ui.html
http://www.nashtto.com:8080/swagger-ui.html
```

### Available Swagger Endpoints

| Endpoint | Description |
|----------|-------------|
| `http://13.223.13.132:8080/swagger-ui.html` | Swagger UI (interactive API documentation) |
| `http://13.223.13.132:8080/v3/api-docs` | OpenAPI JSON specification |
| `http://13.223.13.132:8080/actuator/health` | Health check endpoint |
| `http://13.223.13.132:8080/actuator/info` | Application info |

---

## 🧪 Testing the APIs

### 1. Health Check

```bash
curl http://13.223.13.132:8080/actuator/health

# Expected output:
# {"status":"UP"}
```

### 2. Check Database Connection

```bash
curl http://13.223.13.132:8080/actuator/health | jq '.components.db'

# Expected output:
# {
#   "status": "UP",
#   "details": {
#     "database": "PostgreSQL",
#     "validationQuery": "isValid()"
#   }
# }
```

### 3. Access Swagger UI

Open in browser:
```
http://13.223.13.132:8080/swagger-ui.html
```

You should see the interactive API documentation with all available endpoints.

### 4. Test API via Swagger

1. Navigate to any endpoint (e.g., `GET /api/v1/restaurants`)
2. Click "Try it out"
3. Fill in parameters (if required)
4. Click "Execute"
5. View response

### 5. Test API via cURL

**Example: Get Restaurants**

```bash
curl -X GET http://13.223.13.132:8080/api/v1/restaurants \
     -H "Content-Type: application/json"
```

**Example: Create Order (Checkout)**

```bash
curl -X POST http://13.223.13.132:8080/api/v1/orders/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "123e4567-e89b-12d3-a456-426614174000",
    "restaurantId": "123e4567-e89b-12d3-a456-426614174001",
    "items": [
      {
        "menuItemId": "123e4567-e89b-12d3-a456-426614174002",
        "quantity": 2
      }
    ],
    "deliveryAddress": {
      "street": "123 Main St",
      "city": "New York",
      "zipCode": "10001"
    }
  }'
```

---

## 📊 Monitoring and Logs

### View Application Logs

```bash
# Real-time logs (systemd)
journalctl -u nashtto-order-service -f

# Application log file
tail -f /opt/nashtto/logs/application.log

# Error logs
tail -f /opt/nashtto/logs/error.log

# Last 100 lines
tail -n 100 /opt/nashtto/logs/application.log
```

### Check Application Status

```bash
# Service status
sudo systemctl status nashtto-order-service

# Check if port 8080 is listening
sudo netstat -tlnp | grep 8080

# Check process
ps aux | grep order-catalog-service
```

### Monitor Resources

```bash
# CPU and Memory usage
htop

# Or use top
top
```

### Health Endpoints

```bash
# Health check
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics

# Specific metric (e.g., memory)
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

---

## 🔧 Troubleshooting

### Issue 1: Application Won't Start

**Check logs:**
```bash
journalctl -u nashtto-order-service -n 100 --no-pager
tail -n 100 /opt/nashtto/logs/application.log
```

**Common causes:**
- Database connection failed (check RDS endpoint)
- Redis connection failed (check Redis endpoint)
- Port 8080 already in use
- Insufficient memory

**Solutions:**
```bash
# Check if port is in use
sudo lsof -i :8080

# Kill process using port 8080
sudo kill -9 $(sudo lsof -ti:8080)

# Check which profile is active
journalctl -u nashtto-order-service -n 50 --no-pager | grep "active profile"

# Verify database connectivity
psql -h nashtto-postgres.c2z440siod1m.us-east-1.rds.amazonaws.com \
     -U nastto_admin -d nastto_db

# Verify Redis connectivity
redis-cli -h nas-re-nqw16wn8wkhi.qoapqv.0001.use1.cache.amazonaws.com ping
```

---

### Issue 2: Cannot Access Swagger UI from Browser

**Symptoms:**
- Browser shows "This site can't be reached"
- Connection timeout

**Check Security Group:**

1. Go to AWS Console → EC2 → Security Groups
2. Find security group attached to your EC2 instance
3. Verify inbound rules allow:
   - Port `8080` from your IP (`49.207.218.102/32`)
   - Port `8080` from `0.0.0.0/0` (if you want public access)

**Add Rule (if missing):**

```bash
# Get Security Group ID
aws ec2 describe-instances \
  --instance-ids $(aws ec2 describe-instances \
    --filters "Name=tag:Name,Values=*nashtto*" \
    --query 'Reservations[0].Instances[0].InstanceId' \
    --output text) \
  --query 'Reservations[0].Instances[0].SecurityGroups[0].GroupId' \
  --output text

# Add rule for port 8080
aws ec2 authorize-security-group-ingress \
  --group-id sg-XXXXXXXXX \
  --protocol tcp \
  --port 8080 \
  --cidr 0.0.0.0/0
```

---

### Issue 3: Flyway Migration Errors

**Error:** "Flyway migrations failed"

**Solution:**

```bash
# Connect to database and check schema
psql -h nashtto-postgres.c2z440siod1m.us-east-1.rds.amazonaws.com \
     -U nastto_admin -d nastto_db

# Run in psql:
SELECT * FROM flyway_schema_history;

# If corrupted, reset Flyway baseline
# (CAUTION: Only in development)
DELETE FROM flyway_schema_history WHERE success = false;
```

---

### Issue 4: Out of Memory

**Error:** `java.lang.OutOfMemoryError`

**Solution:**

Edit service file to increase memory:

```bash
sudo nano /etc/systemd/system/nashtto-order-service.service

# Change:
# -Xms512m -Xmx1024m
# To:
# -Xms1024m -Xmx2048m

# Reload and restart
sudo systemctl daemon-reload
sudo systemctl restart nashtto-order-service
```

---

### Issue 5: Redis Connection Timeout

**Error:** "Unable to connect to Redis"

**Check:**

```bash
# Test Redis from EC2
redis-cli -h nas-re-nqw16wn8wkhi.qoapqv.0001.use1.cache.amazonaws.com ping

# Expected: PONG
```

**If fails:**
- Verify Redis security group allows connections from EC2 security group
- Check Redis is running: AWS Console → ElastiCache

---

## 🛑 Stopping the Application

### If Running in Foreground

Press `Ctrl+C`

### If Running as Service

```bash
# Stop service
sudo systemctl stop nashtto-order-service

# Disable auto-start
sudo systemctl disable nashtto-order-service
```

### Kill Process Manually

```bash
# Find process
ps aux | grep order-catalog-service

# Kill by PID
kill -9 <PID>
```

---

## 📝 Quick Reference Commands

### Deploy/Update Application

```bash
# 1. Build locally
cd /Users/yogesh/Documents/ws/food-app/tea-snacks-delivery-aggregator
./gradlew :order-catalog-service:bootJar

# 2. Copy to EC2
cd /Users/yogesh/Documents/ws/food-app
scp -i infrastructure/cloudformation/nastto-key.pem \
    tea-snacks-delivery-aggregator/order-catalog-service/build/libs/order-catalog-service-0.0.1-SNAPSHOT.jar \
    ec2-user@13.223.13.132:/opt/nashtto/

# 3. Restart service on EC2
ssh -i infrastructure/cloudformation/nastto-key.pem ec2-user@13.223.13.132
sudo systemctl restart nashtto-order-service
```

### Check Application Status

```bash
# Service status
sudo systemctl status nashtto-order-service

# Logs
tail -f /opt/nashtto/logs/application.log

# Health check
curl http://localhost:8080/actuator/health
```

### Access URLs

| Resource | URL |
|----------|-----|
| **Swagger UI** | `http://13.223.13.132:8080/swagger-ui.html` |
| **API Docs** | `http://13.223.13.132:8080/v3/api-docs` |
| **Health** | `http://13.223.13.132:8080/actuator/health` |
| **Metrics** | `http://13.223.13.132:8080/actuator/metrics` |

---

## 🎯 Post-Deployment Checklist

- [ ] Application started successfully
- [ ] Swagger UI accessible
- [ ] Health check returns `UP`
- [ ] Database connection working
- [ ] Redis connection working
- [ ] APIs responding correctly
- [ ] Logs being written
- [ ] Service enabled for auto-start

---

## 📚 Additional Resources

- [KAFKA_OPTIONAL_CONFIG.md](./KAFKA_OPTIONAL_CONFIG.md) - Kafka configuration guide
- [KAFKA_TO_SNS_SQS_MIGRATION_PLAN.md](./KAFKA_TO_SNS_SQS_MIGRATION_PLAN.md) - SNS/SQS migration
- [Infrastructure CloudFormation Stack](../../infrastructure/cloudformation/infrastructure-stack.yaml)

---

## 🚀 Success!

Your Order Catalog Service is now deployed on AWS EC2 and accessible via:

**Swagger UI:** http://13.223.13.132:8080/swagger-ui.html

Happy coding! 🎉

