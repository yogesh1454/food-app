# Quick Deploy Guide - Order Catalog Service

## 🚀 One-Command Deployment

```bash
cd /Users/yogesh/Documents/ws/food-app
./deploy-to-ec2.sh
```

This script will:
1. ✅ Build the JAR file
2. ✅ Copy it to EC2
3. ✅ Kill any existing process on port 8080
4. ✅ Restart the application
5. ✅ Verify port 8080 is listening
6. ✅ Provide you with Swagger UI link

---

## 🌐 Access Your Application

Once deployed, access these URLs:

### **Swagger UI (Interactive API Documentation)**
```
http://13.223.13.132:8080/swagger-ui.html
```

### **Health Check**
```bash
curl http://13.223.13.132:8080/actuator/health
```

### **API Documentation (JSON)**
```
http://13.223.13.132:8080/v3/api-docs
```

---

## 📋 Manual Deployment Steps

If you prefer to deploy manually:

### 1. Build JAR (with bundled prod config)
```bash
cd /Users/yogesh/Documents/ws/food-app/tea-snacks-delivery-aggregator
./gradlew :order-catalog-service:bootJar
```

### 2. Copy to EC2
```bash
cd /Users/yogesh/Documents/ws/food-app
scp -i infrastructure/cloudformation/nastto-key.pem \
    tea-snacks-delivery-aggregator/order-catalog-service/build/libs/order-catalog-service-0.0.1-SNAPSHOT.jar \
    ec2-user@13.223.13.132:/opt/nashtto/
```

### 3. SSH into EC2 and Run with prod profile
```bash
ssh -i infrastructure/cloudformation/nastto-key.pem ec2-user@13.223.13.132
cd /opt/nashtto
java -jar -Dspring.profiles.active=prod \
     order-catalog-service-0.0.1-SNAPSHOT.jar

# Or restart service if configured:
sudo systemctl restart nashtto-order-service
```

---

## 🧪 Testing After Deployment

### Test 1: Health Check
```bash
curl http://13.223.13.132:8080/actuator/health
```

**Expected Response:**
```json
{
  "status": "UP"
}
```

### Test 2: Open Swagger UI

Click this link: [http://13.223.13.132:8080/swagger-ui.html](http://13.223.13.132:8080/swagger-ui.html)

You should see the Swagger UI with all API endpoints.

### Test 3: Call an API

**Example: Get Restaurants**
```bash
curl -X GET http://13.223.13.132:8080/api/v1/restaurants \
     -H "Content-Type: application/json"
```

---

## 📊 Monitoring

### View Logs
```bash
ssh -i infrastructure/cloudformation/nastto-key.pem ec2-user@13.223.13.132
tail -f /opt/nashtto/logs/application.log
```

### Check Service Status
```bash
ssh -i infrastructure/cloudformation/nastto-key.pem ec2-user@13.223.13.132
sudo systemctl status nashtto-order-service
```

---

## 🔧 Common Issues

### Issue: Port 8080 already in use

The deployment script automatically handles this by:
1. Detecting any process using port 8080
2. Killing the existing process
3. Starting the new application

**Manual check:**
```bash
ssh -i infrastructure/cloudformation/nastto-key.pem ec2-user@13.223.13.132

# Check what's using port 8080
sudo lsof -i:8080

# Manually kill process if needed
sudo kill -9 $(sudo lsof -ti:8080)
```

### Issue: "Connection refused" when accessing Swagger UI

**Solution 1: Check Security Group**
```bash
# Port 8080 must be open in EC2 Security Group
# AWS Console → EC2 → Security Groups → Add Inbound Rule
# Type: Custom TCP, Port: 8080, Source: 0.0.0.0/0
```

**Solution 2: Verify application is running**
```bash
ssh -i infrastructure/cloudformation/nastto-key.pem ec2-user@13.223.13.132
sudo lsof -i:8080  # Should show java process

# If not running, check logs
tail -f /opt/nashtto/logs/application.log
```

### Issue: Application not starting

**Check logs:**
```bash
ssh -i infrastructure/cloudformation/nastto-key.pem ec2-user@13.223.13.132
tail -f /opt/nashtto/logs/application.log

# Or for systemd service:
journalctl -u nashtto-order-service -n 50 --no-pager
```

### Issue: Old JAR still running after deployment

**The script now handles this automatically**, but if you need to manually verify:
```bash
ssh -i infrastructure/cloudformation/nastto-key.pem ec2-user@13.223.13.132

# Find all Java processes
ps aux | grep java

# Kill specific PID if needed
sudo kill -9 <PID>
```

---

## 📚 Full Documentation

For complete deployment guide with troubleshooting:

**See:** `tea-snacks-delivery-aggregator/order-catalog-service/DEPLOYMENT_GUIDE.md`

---

## ✅ Quick Checklist

After deployment, verify:

- [ ] Health endpoint returns `UP`
- [ ] Swagger UI loads in browser
- [ ] Can see API endpoints in Swagger
- [ ] Can execute test API call
- [ ] Logs show no errors

---

## 🎯 Your URLs

| Resource | URL |
|----------|-----|
| **Swagger UI** | http://13.223.13.132:8080/swagger-ui.html |
| **Health Check** | http://13.223.13.132:8080/actuator/health |
| **API Docs (JSON)** | http://13.223.13.132:8080/v3/api-docs |
| **Metrics** | http://13.223.13.132:8080/actuator/metrics |

---

## 🆘 Need Help?

If something doesn't work:

1. Check application logs on EC2
2. Verify Security Group allows port 8080
3. Check RDS and Redis connections
4. Review full deployment guide in `DEPLOYMENT_GUIDE.md`

---

**Happy Deploying! 🚀**

