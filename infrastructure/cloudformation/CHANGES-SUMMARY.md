# CloudFormation Template Changes Summary

## Changes Made Based on User Requirements

### 1. **EC2 Instance Type Changed**
- **Before:** `t3.micro` (default)
- **After:** `t2.micro` (smallest/cheapest option)
- **Reason:** User wants the smallest EC2 instance for cost optimization

### 2. **Removed Docker & Docker Compose**
- **Before:** UserData installed Docker, Docker Compose, and set up docker group
- **After:** Removed all Docker-related installation and configuration
- **Reason:** User will deploy application JAR manually, Docker not needed

### 3. **Added Java 21 (Amazon Corretto)**
- **Before:** No Java runtime
- **After:** Installed `java-21-amazon-corretto-devel`
- **Reason:** Required to run Spring Boot JAR files
- **Verification:** `java -version` command documented in README

### 4. **Simplified SQS Queues**
- **Before:** 4 queues - email-queue, image-processing-queue, backup-queue, dead-letter-queue
- **After:** 2 queues - test-queue, dead-letter-queue
- **Reason:** User doesn't need specific queues now, will add later based on project requirements
- **Note:** Test queue configured with DLQ redrive policy (maxReceiveCount=3)

### 5. **Simplified SNS Topics**
- **Before:** 3 topics - user-notifications, admin-notifications, system-alerts
- **After:** 1 topic - test-topic
- **Reason:** User doesn't need specific topics now, will add later based on project requirements

### 6. **Updated IAM Permissions**
- **Before:** Permissions for EmailQueue, ImageProcessingQueue, BackupQueue, and all 3 SNS topics
- **After:** Permissions for TestQueue, DeadLetterQueue, and TestTopic only
- **Impact:** EC2 instance can access only the test queue/topic and DLQ

### 7. **Updated CloudWatch Alarms**
- **Before:** All alarms published to SystemAlertsTopic
- **After:** All alarms publish to TestTopic
- **Alarms Still Active:**
  - EC2 High CPU (>80%)
  - RDS High CPU (>75%)
  - RDS Low Storage (<2GB)
  - RDS High Connections (>80)
  - Redis High Memory (>80%)

### 8. **Updated Environment File (.env)**
- **Before:** Variables for 4 queues and 3 topics
- **After:** Variables for test queue/topic only
- **New Variables Added:**
  - `SERVER_PORT=8080` (for Spring Boot)
- **Removed Variables:**
  - `SQS_EMAIL_QUEUE_URL`
  - `SQS_IMAGE_QUEUE_URL`
  - `SQS_BACKUP_QUEUE_URL`
  - `SNS_USER_NOTIFICATIONS_ARN`
  - `SNS_ADMIN_NOTIFICATIONS_ARN`
  - `SNS_SYSTEM_ALERTS_ARN`

### 9. **Updated Stack Outputs**
- **Before:** Outputs for all 4 queues and 3 topics
- **After:** Outputs for test queue/topic only
- **Remaining Outputs:**
  - `TestQueueURL` - Test queue URL (add more queues as needed)
  - `DeadLetterQueueURL` - Dead letter queue URL
  - `TestTopicARN` - Test topic ARN (add more topics as needed)

### 10. **Updated EC2 README.txt**
- **Before:** Mentioned Docker & Docker Compose
- **After:** 
  - Mentions Java 21 for Spring Boot JARs
  - Instructions for deploying JAR files
  - Java version verification command
  - SCP command example for uploading JARs

### 11. **Added htop**
- **New:** Installed htop for system monitoring
- **Reason:** Useful utility for monitoring system resources

---

## What Remains Unchanged

✅ **Network Infrastructure** - VPC, Subnets, Security Groups, NAT Gateway  
✅ **RDS PostgreSQL** - Database configuration unchanged  
✅ **ElastiCache Redis** - Cache configuration unchanged  
✅ **S3 Buckets** - All 4 buckets with lifecycle policies  
✅ **Route 53 & SSL** - Domain and certificate setup  
✅ **CloudWatch Monitoring** - All alarms still active  
✅ **IAM Roles** - EC2 role still has S3, CloudWatch, SSM access  

---

## New EC2 Instance Software

After deployment, EC2 will have:
- ✅ **Java 21 (Amazon Corretto)** - For running Spring Boot JARs
- ✅ **Git** - For code deployment
- ✅ **PostgreSQL 15 Client** - For database management
- ✅ **Redis CLI** - For cache management
- ✅ **AWS CLI v2** - For AWS resource management (pre-installed on Amazon Linux 2023)
- ✅ **CloudWatch Agent** - For monitoring and logging
- ✅ **htop** - For system monitoring

**Removed:**
- ❌ Docker
- ❌ Docker Compose

---

## Deployment Instructions (Updated)

### 1. Deploy Stack (No changes)
```bash
aws cloudformation create-stack \
  --stack-name nashtto-infrastructure \
  --template-body file://infrastructure-stack.yaml \
  --parameters file://parameters.json \
  --capabilities CAPABILITY_NAMED_IAM \
  --region us-east-1
```

### 2. After Stack Creation - Deploy Your JAR

```bash
# SSH into EC2
ssh -i nastto-key.pem ec2-user@<EC2_PUBLIC_IP>

# Verify Java installation
java -version
# Should show: openjdk version "21.x.x" Amazon Corretto

# Check environment variables
cat /opt/nashtto/.env
```

### 3. Upload and Run Your Application

**From your local machine:**
```bash
# Build your Spring Boot JAR
./gradlew build  # or: mvn package

# Upload JAR to EC2
scp -i nastto-key.pem build/libs/your-app.jar ec2-user@<EC2_IP>:/opt/nashtto/
```

**On EC2:**
```bash
# Run your Spring Boot application
cd /opt/nashtto
java -jar your-app.jar

# Or run in background with nohup
nohup java -jar your-app.jar > app.log 2>&1 &

# Or create a systemd service for auto-start (recommended for production)
```

### 4. Create Systemd Service (Optional but Recommended)

```bash
sudo tee /etc/systemd/system/nashtto.service > /dev/null <<EOF
[Unit]
Description=Nashtto Food Delivery Application
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/opt/nashtto
ExecStart=/usr/bin/java -jar /opt/nashtto/your-app.jar
Restart=on-failure
RestartSec=10
StandardOutput=append:/opt/nashtto/logs/app.log
StandardError=append:/opt/nashtto/logs/error.log

[Install]
WantedBy=multi-user.target
EOF

# Create logs directory
mkdir -p /opt/nashtto/logs

# Enable and start service
sudo systemctl enable nashtto
sudo systemctl start nashtto

# Check status
sudo systemctl status nashtto

# View logs
tail -f /opt/nashtto/logs/app.log
```

---

## Cost Impact

### Reduced Costs:
- ✅ **t2.micro instead of t3.micro** - Slightly cheaper (about $0.50-1/month less)
- ✅ **Fewer SQS queues** - 3 fewer queues (negligible cost savings, as SQS is cheap)
- ✅ **Fewer SNS topics** - 2 fewer topics (negligible cost savings)

### **New Estimated Monthly Cost:** ~$45-67/month (was $46-68/month)

Minimal cost difference, but cleaner infrastructure with only what you need.

---

## Adding More Queues/Topics Later

When you need to add more queues or topics:

### Option 1: Update CloudFormation Template
1. Edit `infrastructure-stack.yaml`
2. Add new queue/topic resources (copy from this summary for reference)
3. Update IAM policies to include new resources
4. Update outputs section
5. Run stack update:
   ```bash
   aws cloudformation update-stack \
     --stack-name nashtto-infrastructure \
     --template-body file://infrastructure-stack.yaml \
     --parameters file://parameters.json \
     --capabilities CAPABILITY_NAMED_IAM
   ```

### Option 2: Create Manually via AWS Console/CLI
- Create queues in SQS console
- Create topics in SNS console
- Update IAM role manually if needed

---

## References for Deleted Resources

If you need to add back specific queues later, here are the configurations that were removed:

### Email Queue
```yaml
EmailQueue:
  Type: AWS::SQS::Queue
  Properties:
    QueueName: !Sub ${ProjectName}-email-queue
    VisibilityTimeout: 300
    MessageRetentionPeriod: 345600
    RedrivePolicy:
      deadLetterTargetArn: !GetAtt DeadLetterQueue.Arn
      maxReceiveCount: 3
```

### Image Processing Queue
```yaml
ImageProcessingQueue:
  Type: AWS::SQS::Queue
  Properties:
    QueueName: !Sub ${ProjectName}-image-processing-queue
    VisibilityTimeout: 600
    MessageRetentionPeriod: 345600
    RedrivePolicy:
      deadLetterTargetArn: !GetAtt DeadLetterQueue.Arn
      maxReceiveCount: 3
```

### Backup Queue
```yaml
BackupQueue:
  Type: AWS::SQS::Queue
  Properties:
    QueueName: !Sub ${ProjectName}-backup-queue
    VisibilityTimeout: 900
    MessageRetentionPeriod: 345600
    RedrivePolicy:
      deadLetterTargetArn: !GetAtt DeadLetterQueue.Arn
      maxReceiveCount: 3
```

### User Notifications Topic
```yaml
UserNotificationsTopic:
  Type: AWS::SNS::Topic
  Properties:
    TopicName: !Sub ${ProjectName}-user-notifications
    DisplayName: User Notifications
```

### Admin Notifications Topic
```yaml
AdminNotificationsTopic:
  Type: AWS::SNS::Topic
  Properties:
    TopicName: !Sub ${ProjectName}-admin-notifications
    DisplayName: Admin Notifications
```

### System Alerts Topic
```yaml
SystemAlertsTopic:
  Type: AWS::SNS::Topic
  Properties:
    TopicName: !Sub ${ProjectName}-system-alerts
    DisplayName: System Alerts
```

---

## Summary

✅ **Simplified** - Removed unnecessary queues and topics  
✅ **Cost-Optimized** - Using t2.micro (smallest instance)  
✅ **JAR-Ready** - Java 21 installed for Spring Boot deployment  
✅ **No Docker** - Removed Docker/Docker Compose (not needed for JAR deployment)  
✅ **Test Infrastructure** - 1 test queue + 1 test topic for testing messaging  
✅ **Extensible** - Easy to add more queues/topics later when needed  

**Template is ready to deploy!**

---

**Question Answered:** *Why Docker was included?*

Docker was included in the original template assuming containerized deployment (common for microservices). However, you're correct - **for manual JAR deployment, Docker is not needed**. We've removed it and added Java 21 instead, which is all you need to run Spring Boot applications.

---

**Version:** 1.1 (Updated)  
**Last Modified:** November 2025  
**Changes By:** User Requirements

