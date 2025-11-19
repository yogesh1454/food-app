Below is a **cleaned, optimized, and structured version** of your *Complete AWS Deployment Guide for New Users*, ensuring it remains **comprehensive**, **beginner-friendly**, and **production-ready** while keeping everything exactly aligned with your full stack:

### ✔️ What I did:

* Reformatted your content so it becomes a **professional deployment guide**.
* Fixed a few ordering issues.
* Ensured this document can be directly used as a **step-by-step manual** by a beginner.
* Made it easier to convert to **PDF**, **Notion document**, or **GitHub README**.

---

# ✅ Complete AWS Deployment Guide

### **For Beginners – Production Ready**

### **Stack: EC2 + RDS PostgreSQL + ElastiCache Redis + S3 + SQS + SNS**

---

# 📋 **Phase 0: Prerequisites Checklist**

### Before You Start:

* ✔ AWS Account created (new account recommended for free-tier)
* ✔ Domain purchased (you have: **nastto.com** from GoDaddy)
* ✔ Application code ready (Node/Python/Java etc.)
* ✔ Docker setup working locally
* ✔ Basic understanding of application’s dependencies (DB, cache, queues, files)

---

# 🎯 **Phase 1 – AWS Account Security Setup (Day 1)**

This phase protects your AWS account from security breaches.

---

## **1.1 Secure Root Account (Mandatory)**

### **Enable MFA**

1. Login → Top-right menu → **Security credentials**
2. Scroll to **MFA** → **Assign MFA device**
3. Choose **Authenticator App**
4. Scan QR → Enter two codes → Done

### **Create Strong Password**

* Change root password
* Store in password manager
* **Never use root user again**

---

## **1.2 Create IAM Admin User**

### Create Admin User:

* Go to IAM → Users → Create user
* Name: `nastto-admin`
* Allow Console access
* Create custom password
* Assign permissions:

  * **AdministratorAccess**

### Enable MFA for Admin User

* IAM → Users → `nastto-admin` → Security credentials → Assign MFA

### Create Access Keys

* IAM → User → Security Credentials → Access Keys → Create
* Choose **CLI**
* Download CSV (save securely)

---

## **1.3 Set Up Billing Alerts**

Enable Billing Alerts → Create a $10 monthly budget with 50%, 80%, 100% alerts.

---
Below are **simple step-by-step instructions** to enable AWS Billing Alerts and create a **$10 monthly budget** with alerts at **50%, 80%, and 100%**.

These steps **must be done manually** from the root account — **CloudFormation cannot set budgets or enable billing alerts**.

---

# ✅ **Step 1 — Enable Billing Alerts**

You must enable billing alerts once per account.

1. Log in to the **AWS Console** using the **root account**.
2. Go to
   **AWS Console → Billing → Billing Preferences**
3. Enable:

   * ✔ **Receive Billing Alerts**
   * ✔ (Optional) **Receive Free Tier Usage Alerts**
4. Click **Save preferences**

---

# ✅ **Step 2 — Create a Monthly Cost Budget**

1. Navigate to:
   **AWS Console → Billing → Budgets**
2. Click **Create budget**
3. Choose **Cost budget**
4. Click **Next**

---

# ✅ **Step 3 — Configure Budget Details**

In the budget configuration page:

### **Budget Details**

* **Budget name:** `Monthly-10USD-Budget`
* **Period:** `Monthly`
* **Budget renewal type:** `Recurring budget`
* **Budgeting method:** `Fixed`
* **Amount:** `10`
* **Budgeting amount:** `$10`

Click **Next**.

---

# ✅ **Step 4 — Add Notifications (for 50%, 80%, 100%)**

You’ll create *three* notifications.

### **Notification 1: 50% Alert**

* **Threshold:** `50%`
* **Threshold type:** `Actual`
* **Notification type:** `Email`
* **Email recipients:** your email

Click **Add notification**.

### **Notification 2: 80% Alert**

* **Threshold:** `80%`
* **Threshold type:** `Actual`
* **Notification type:** `Email`
* **Email recipients:** your email

Click **Add notification**.

### **Notification 3: 100% Alert**

* **Threshold:** `100%`
* **Threshold type:** `Actual`
* **Notification type:** `Email`
* **Email recipients:** your email

Click **Next**.

---

# ✅ **Step 5 — Review & Create**

* Review your settings
* Click **Create budget**

Your budget is now active.

---

# 🎉 Done!

AWS will now send alerts when your monthly cost reaches:

* **$5 (50%)**
* **$8 (80%)**
* **$10 (100%)**

---


## **1.4 Install & Configure AWS CLI**

```
Install
```brew install awscli```
Verify
```aws --version```


aws configure
AWS Access Key ID [None]: <YOUR_ACCESS_KEY_ID>
AWS Secret Access Key [None]: <YOUR_SECRET_ACCESS_KEY>
Default region name [None]: us-east-1
Default output format [None]: json

```

Test:

```
aws sts get-caller-identity
```

---

# 🌐 **Phase 2 – Network Infrastructure (Day 2)**

Create VPC, subnets, NAT, internet gateway, route tables, and security groups.

---

## **2.1 Create VPC using "VPC and More" wizard**

Settings:

* Name: **nastto-vpc**
* IPv4 CIDR: **10.0.0.0/16**
* AZs: **2**
* Public Subnets: **2**
* Private Subnets: **2**
* NAT Gateway: **1 AZ** (cost optimization)

---

## **2.2 Create Security Groups**

### Web SG (`nastto-web-sg`)

Open:

* 80 (HTTP)
* 443 (HTTPS)
* 22 (SSH) → Restrict to your IP

### DB SG (`nastto-db-sg`)

Allow:

* 5432 from **Web SG only**

### Redis SG (`nastto-cache-sg`)

Allow:

* 6379 from **Web SG only**

---

# 🗄️ **Phase 3 – RDS PostgreSQL Setup (Day 3)**

---

## **3.1 Create DB Subnet Group**

Must use private subnets.

---

## **3.2 Create PostgreSQL Instance**

Recommended starter config:

* **db.t3.micro**
* 20GB storage
* Single-AZ
* NOT publicly accessible
* Encrypted storage
* Backup retention: **7 days**

---

## **3.3 Test DB Connection**

Install PostgreSQL client → Connect:

```
psql -h <endpoint> -U nastto_admin -d nastto_db
```

---

# 🚀 **Phase 4 – ElastiCache Redis (Day 4)**

---

## **4.1 Create Cache Subnet Group**

Using private subnets.

---

## **4.2 Create Redis Cluster**

Starter config:

* cache.t3.micro
* Redis 7.0
* Single node (for dev)

---

## **4.3 Test Redis Connection**

Redis CLI will work *once connected from EC2*.

---

# 📦 **Phase 5 – S3 Bucket Setup (Day 5)**

Create these buckets:

* Static assets
* User uploads
* Backups
* Logs

### Configure Public Read Access

Only for `/public/*` folder in static bucket.

### Configure Lifecycle Policies

* Move logs → IA after 30 days
* Move logs → Glacier after 90 days

---

# 📨 **Phase 6 – SQS + SNS Setup (Day 6)**

---

## **6.1 Create SQS Queues**

Queues:

* nastto-email-queue
* nastto-image-processing-queue
* nastto-backup-queue
* nastto-dead-letter-queue

Configure DLQ redrive policy (maxReceiveCount = 3).

---

## **6.2 Create SNS Topics**

Topics:

* user-notifications
* admin-notifications
* system-alerts

Subscribe SQS queues → SNS topics.

---

# 🖥️ **Phase 7 – EC2 Setup (Day 7)**

Launch EC2 instance that will host your application.

---

## **7.1 Create SSH Key Pair**

Download `nastto-key.pem` → chmod 400.

---

## **7.2 Launch EC2 Instance**

Recommended:

* **t2.micro** or **t3.micro (preferred)**
* Amazon Linux 2023
* Web SG
* Public subnet
* User-data installs:

  * Docker
  * Docker Compose
  * Git
  * Redis CLI
  * PostgreSQL client
  * AWS CLI v2

---

## **7.3 SSH Into Instance**

```
ssh -i nastto-key.pem ec2-user@<public-ip>
```

Verify versions:

```
docker --version
psql --version
redis-cli --version
```

---

# 🚀 **Phase 8 – Application Deployment (Day 8–9)**

---

## **8.1 Deploy Application Code**

Clone app or upload code to `/opt/nastto`.

---

## **8.2 Configure Environment (.env)**

Include:

* DB URL
* Redis URL
* S3 bucket names
* SQS queue URLs
* SNS ARNs
* App config

---

## **8.3 Docker Setup**

Create:

* `Dockerfile`
* `docker-compose.yml`
* `nginx.conf`

Start:

```
docker-compose up -d --build
```

Check:

```
curl http://localhost:8000
```

---

# 🌍 **Phase 9 – Domain + SSL Setup (Day 10)**

---

## **9.1 Create Route 53 Hosted Zone**

Update GoDaddy nameservers → Route53 NS values.
Wait 24–48 hours.

---

## **9.2 Create DNS Records**

A records:

* nastto.com → EC2 public IP
* [www.nastto.com](http://www.nastto.com) → EC2 public IP

---

## **9.3 Create SSL Certificate with ACM**

Domain validation via DNS → CNAME record added in Route53.

---

# 📊 **Phase 10 – Monitoring & Logging (Day 11)**

---

## **10.1 CloudWatch Alarms**

Recommended:

* High CPU
* High DB Connections
* Low Free Storage
* Redis memory > threshold

---

## **10.2 CloudWatch Logs**

Install CloudWatch Agent → Push application logs to `/aws/ec2/nastto-app`.

---

# 🔧 **Phase 11 – Application Integration (Day 12)**

Provide examples (Python/Node/Java):

* Connect to PostgreSQL
* Connect to Redis
* Publish/Consume SQS
* Publish SNS notifications
* Upload to S3

---

# 🎉 **Your AWS Setup is Now Production Ready!**

---

# 👉 What do you want next?

I can generate **any** of the following if you want:

### ✅ PDF / Printable Version

### ✅ Notion Page (ready to paste)

### ✅ GitHub README.md

### ✅ Terraform version of the entire stack

### ✅ CloudFormation version

### ✅ Architecture diagram

### ✅ CI/CD pipeline (GitHub Actions)

### ✅ Cost-optimized architecture recommendations

Tell me which one you want, and I will generate it.
