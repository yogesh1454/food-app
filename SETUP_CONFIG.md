# Configuration Management

## ✅ Configuration is Bundled in JAR!

**Good news:** The production configuration (`application-prod.yml`) is now **bundled inside the JAR file**!

**Location:** `order-catalog-service/src/main/resources/application-prod.yml`

**This means:**
- ✅ No need to copy separate config files to EC2
- ✅ Configuration travels with the JAR
- ✅ Simply use `-Dspring.profiles.active=prod` flag
- ✅ Easier deployment and updates

## 🚀 How to Use

Just run with the `prod` profile:

```bash
java -jar -Dspring.profiles.active=prod \
     order-catalog-service-0.0.1-SNAPSHOT.jar
```

Spring Boot will automatically load `application-prod.yml` from inside the JAR.

---

## ⚙️ Configuration Values

The `application-prod.yml` file contains:

### **Database (AWS RDS PostgreSQL)**
```yaml
datasource:
  url: jdbc:postgresql://nashtto-postgres.c2z440siod1m.us-east-1.rds.amazonaws.com:5432/nastto_db
  username: nastto_admin
  password: IjhY3HEqWGjk0deZ
```

### **Redis (AWS ElastiCache)**
```yaml
redis:
  host: nas-re-nqw16wn8wkhi.qoapqv.0001.use1.cache.amazonaws.com
  port: 6379
```

### **Kafka (Disabled)**
```yaml
features:
  kafka:
    enabled: false
```

---

## 🔄 If You Need to Update Configuration

Since configuration is bundled in the JAR:

### Update Configuration:

1. **Edit locally:**
   ```bash
   nano tea-snacks-delivery-aggregator/order-catalog-service/src/main/resources/application-prod.yml
   ```

2. **Rebuild JAR:**
   ```bash
   cd tea-snacks-delivery-aggregator
   ./gradlew :order-catalog-service:bootJar
   ```

3. **Deploy updated JAR:**
   ```bash
   cd /Users/yogesh/Documents/ws/food-app
   ./deploy-to-ec2.sh
   ```

That's it! The new configuration is automatically applied.

---

## 📊 View Current Configuration

To see what configuration the JAR is using:

```bash
# SSH into EC2
ssh -i infrastructure/cloudformation/nastto-key.pem ec2-user@13.223.13.132

# Check application logs for active profile
journalctl -u nashtto-order-service -n 50 --no-pager | grep "active profile"

# Expected output: "The following 1 profile is active: "prod""
```

---

## ✅ Verify Configuration

After deployment, verify the application is using correct settings:

### Check Active Profile:
```bash
ssh -i infrastructure/cloudformation/nastto-key.pem ec2-user@13.223.13.132
tail -f /opt/nashtto/logs/application.log | grep "active profile"

# Should show: "The following 1 profile is active: "prod""
```

### Check Database Connection:
```bash
curl http://13.223.13.132:8080/actuator/health | jq '.components.db'

# Should show: "status": "UP"
```

---

## 🚀 Quick Deployment

**Everything is automated:**

```bash
cd /Users/yogesh/Documents/ws/food-app
./deploy-to-ec2.sh
```

This will:
1. ✅ Build JAR with bundled config
2. ✅ Copy to EC2
3. ✅ Restart service with prod profile
4. ✅ Verify startup

---

## 📂 Configuration File Location

**Local (Source):**
```
tea-snacks-delivery-aggregator/
└── order-catalog-service/
    └── src/
        └── main/
            └── resources/
                ├── application.yml          # Base config
                └── application-prod.yml     # Production overrides
```

**In JAR (Bundled):**
```
order-catalog-service-0.0.1-SNAPSHOT.jar
└── BOOT-INF/
    └── classes/
        ├── application.yml
        └── application-prod.yml  ← Loaded when using -Dspring.profiles.active=prod
```

---

## 🎯 Benefits of This Approach

✅ **Simpler Deployment** - No separate config files to manage  
✅ **Version Control** - Configuration is versioned with code  
✅ **No Manual Steps** - Config is automatically in the JAR  
✅ **Cleaner EC2** - No loose config files on server  
✅ **One Artifact** - JAR contains everything needed  

---

**Configuration is now part of your deployment process!** 🎉

