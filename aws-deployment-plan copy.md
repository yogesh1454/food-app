Complete AWS Deployment Guide for New Users
Application Stack: EC2 + RDS PostgreSQL + ElastiCache Redis + S3 + SQS + SNS
📋 Prerequisites Checklist
Before You Start:
 AWS Account created (new account for free tier benefits)
 Domain name: nastto.com (from GoDaddy)
 Application code ready for deployment
 Docker setup working locally
 Basic understanding of your application's requirements
🎯 Phase 1: AWS Account Security Setup (Day 1)
Step 1.1: Secure Your Root Account
Enable MFA on Root User:
1. Sign in to AWS Console as root user
2. Click on your account name (top right) → Security credentials
3. Scroll to "Multi-factor authentication (MFA)" section
4. Click "Assign MFA device"
5. Choose "Authenticator app" (recommended)
6. Use Google Authenticator or AWS Authenticator app
7. Scan QR code and enter two consecutive codes
8. Click "Assign MFA"

Run in CloudShell
Create Strong Root Password:
1. Go to Security credentials
2. Click "Change password"
3. Use a complex password (save in password manager)
4. Never use root account for daily operations

Run in CloudShell
Step 1.2: Create IAM Administrative User
Create Admin User:
1. Go to IAM Console → Users → Create user
2. User name: "nastto-admin"
3. Check "Provide user access to the AWS Management Console"
4. Choose "I want to create an IAM user"
5. Set custom password (save it securely)
6. Uncheck "Users must create a new password at next sign-in"
7. Click "Next"

8. Permissions: "Attach policies directly"
9. Search and select "AdministratorAccess"
10. Click "Next" → "Create user"

Run in CloudShell
Enable MFA for Admin User:
1. Go to IAM → Users → nastto-admin
2. Security credentials tab
3. Assigned MFA device → Assign
4. Follow same MFA setup process as root

Run in CloudShell
Create Access Keys for CLI:
1. In nastto-admin user → Security credentials
2. Access keys → Create access key
3. Choose "Command Line Interface (CLI)"
4. Check acknowledgment → Next
5. Add description: "Local development CLI access"
6. Create access key
7. Download .csv file (keep it secure!)

Run in CloudShell
Step 1.3: Set Up Billing Alerts
Enable Billing Alerts:
1. Go to Billing Dashboard
2. Billing preferences → Edit
3. Check "Receive Billing Alerts"
4. Save preferences

Run in CloudShell
Create Budget Alerts:
1. Go to AWS Budgets → Create budget
2. Budget type: "Cost budget"
3. Budget name: "Free Tier Monitor"
4. Period: Monthly
5. Budget amount: $10
6. Add alerts:
   - Alert 1: 50% of budget ($5)
   - Alert 2: 80% of budget ($8)
   - Alert 3: 100% of budget ($10)
7. Email: your-email@domain.com
8. Create budget

Run in CloudShell
Step 1.4: Install AWS CLI
Install AWS CLI:
# On macOS
brew install awscli

# On Ubuntu/Debian
sudo apt update
sudo apt install awscli

# On Windows
# Download from: https://aws.amazon.com/cli/

Run in CloudShell
Configure AWS CLI:
aws configure
# Enter:
# AWS Access Key ID: [from downloaded CSV]
# AWS Secret Access Key: [from downloaded CSV]
# Default region name: us-east-1
# Default output format: json

# Test configuration
aws sts get-caller-identity

Run in CloudShell
🌐 Phase 2: Network Infrastructure Setup (Day 2)
Step 2.1: Create VPC
Create VPC via Console:
1. Go to VPC Console → Create VPC
2. Choose "VPC and more" (recommended for beginners)
3. Configuration:
   - Name tag: "nastto-vpc"
   - IPv4 CIDR: 10.0.0.0/16
   - IPv6 CIDR: No IPv6 CIDR block
   - Tenancy: Default
   - Number of AZs: 2
   - Number of public subnets: 2
   - Number of private subnets: 2
   - NAT gateways: In 1 AZ (to save costs)
   - VPC endpoints: None
4. Click "Create VPC"

Run in CloudShell
Verify VPC Creation:
# Using AWS CLI
aws ec2 describe-vpcs --filters "Name=tag:Name,Values=nastto-vpc"

Run in CloudShell
Step 2.2: Create Security Groups
Web Server Security Group:
aws ec2 create-security-group \
    --group-name nastto-web-sg \
    --description "Security group for web servers" \
    --vpc-id vpc-xxxxxxxxx  # Replace with your VPC ID

# Add inbound rules
aws ec2 authorize-security-group-ingress \
    --group-id sg-xxxxxxxxx \  # Replace with created SG ID
    --protocol tcp \
    --port 80 \
    --cidr 0.0.0.0/0

aws ec2 authorize-security-group-ingress \
    --group-id sg-xxxxxxxxx \
    --protocol tcp \
    --port 443 \
    --cidr 0.0.0.0/0

aws ec2 authorize-security-group-ingress \
    --group-id sg-xxxxxxxxx \
    --protocol tcp \
    --port 22 \
    --cidr 0.0.0.0/0  # Restrict this to your IP in production

Run in CloudShell
Database Security Group:
aws ec2 create-security-group \
    --group-name nastto-db-sg \
    --description "Security group for database" \
    --vpc-id vpc-xxxxxxxxx

# Allow PostgreSQL access from web servers only
aws ec2 authorize-security-group-ingress \
    --group-id sg-xxxxxxxxx \  # DB security group ID
    --protocol tcp \
    --port 5432 \
    --source-group sg-xxxxxxxxx  # Web security group ID

Run in CloudShell
Cache Security Group:
aws ec2 create-security-group \
    --group-name nastto-cache-sg \
    --description "Security group for ElastiCache" \
    --vpc-id vpc-xxxxxxxxx

# Allow Redis access from web servers only
aws ec2 authorize-security-group-ingress \
    --group-id sg-xxxxxxxxx \  # Cache security group ID
    --protocol tcp \
    --port 6379 \
    --source-group sg-xxxxxxxxx  # Web security group ID

Run in CloudShell
🗄️ Phase 3: Database Setup (Day 3)
Step 3.1: Create DB Subnet Group
Create Subnet Group:
aws rds create-db-subnet-group \
    --db-subnet-group-name nastto-db-subnet-group \
    --db-subnet-group-description "Subnet group for RDS" \
    --subnet-ids subnet-xxxxxxxxx subnet-yyyyyyyyy  # Private subnet IDs

Run in CloudShell
Step 3.2: Create RDS PostgreSQL Instance
Create Database:
aws rds create-db-instance \
    --db-instance-identifier nastto-postgres \
    --db-instance-class db.t3.micro \
    --engine postgres \
    --engine-version 15.4 \
    --master-username nastto_admin \
    --master-user-password 'YourSecurePassword123!' \
    --allocated-storage 20 \
    --storage-type gp2 \
    --vpc-security-group-ids sg-xxxxxxxxx \  # DB security group ID
    --db-subnet-group-name nastto-db-subnet-group \
    --backup-retention-period 7 \
    --storage-encrypted \
    --no-multi-az \
    --no-publicly-accessible \
    --db-name nastto_db

Run in CloudShell
Wait for Database Creation:
# Check status (takes 10-15 minutes)
aws rds describe-db-instances \
    --db-instance-identifier nastto-postgres \
    --query 'DBInstances[0].DBInstanceStatus'

# Get endpoint when ready
aws rds describe-db-instances \
    --db-instance-identifier nastto-postgres \
    --query 'DBInstances[0].Endpoint.Address'

Run in CloudShell
Step 3.3: Test Database Connection
Install PostgreSQL Client:
# On Ubuntu/Debian
sudo apt install postgresql-client

# On macOS
brew install postgresql

Run in CloudShell
Test Connection:
# Replace with your RDS endpoint
psql -h nastto-postgres.xxxxxxxxx.us-east-1.rds.amazonaws.com \
     -U nastto_admin \
     -d nastto_db

# Create initial tables (example)
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE posts (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

Run in CloudShell
🚀 Phase 4: ElastiCache Redis Setup (Day 4)
Step 4.1: Create Cache Subnet Group
Create Subnet Group:
aws elasticache create-cache-subnet-group \
    --cache-subnet-group-name nastto-cache-subnet-group \
    --cache-subnet-group-description "Subnet group for ElastiCache" \
    --subnet-ids subnet-xxxxxxxxx subnet-yyyyyyyyy  # Private subnet IDs

Run in CloudShell
Step 4.2: Create Redis Cluster
Create Redis Instance:
aws elasticache create-cache-cluster \
    --cache-cluster-id nastto-redis \
    --cache-node-type cache.t3.micro \
    --engine redis \
    --engine-version 7.0 \
    --num-cache-nodes 1 \
    --cache-parameter-group default.redis7.x \
    --cache-subnet-group-name nastto-cache-subnet-group \
    --security-group-ids sg-xxxxxxxxx  # Cache security group ID

Run in CloudShell
Wait for Redis Creation:
# Check status (takes 5-10 minutes)
aws elasticache describe-cache-clusters \
    --cache-cluster-id nastto-redis \
    --query 'CacheClusters[0].CacheClusterStatus'

# Get endpoint when ready
aws elasticache describe-cache-clusters \
    --cache-cluster-id nastto-redis \
    --show-cache-node-info \
    --query 'CacheClusters[0].CacheNodes[0].Endpoint'

Run in CloudShell
Step 4.3: Test Redis Connection
Install Redis CLI:
# On Ubuntu/Debian
sudo apt install redis-tools

# On macOS
brew install redis

Run in CloudShell
Test Connection (from EC2 instance later):
# This will work once you have EC2 in the same VPC
redis-cli -h nastto-redis.xxxxxx.cache.amazonaws.com -p 6379

# Test commands
SET test_key "Hello Redis"
GET test_key

Run in CloudShell
📦 Phase 5: S3 Storage Setup (Day 5)
Step 5.1: Create S3 Buckets
Create Buckets:
# Static assets bucket
aws s3 mb s3://nastto-static-assets-$(date +%s)

# User uploads bucket
aws s3 mb s3://nastto-user-uploads-$(date +%s)

# Backups bucket
aws s3 mb s3://nastto-backups-$(date +%s)

# Application logs bucket
aws s3 mb s3://nastto-logs-$(date +%s)

Run in CloudShell
Configure Bucket Policies:
# Create policy file for static assets
cat > static-assets-policy.json << EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "PublicReadGetObject",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::nastto-static-assets-*/public/*"
        }
    ]
}
EOF

# Apply policy
aws s3api put-bucket-policy \
    --bucket nastto-static-assets-xxxxxxxxx \
    --policy file://static-assets-policy.json

Run in CloudShell
Enable Static Website Hosting:
aws s3 website s3://nastto-static-assets-xxxxxxxxx \
    --index-document index.html \
    --error-document error.html

Run in CloudShell
Step 5.2: Set Up Lifecycle Policies
Create Lifecycle Policy:
cat > lifecycle-policy.json << EOF
{
    "Rules": [
        {
            "ID": "LogsTransition",
            "Status": "Enabled",
            "Filter": {
                "Prefix": "logs/"
            },
            "Transitions": [
                {
                    "Days": 30,
                    "StorageClass": "STANDARD_IA"
                },
                {
                    "Days": 90,
                    "StorageClass": "GLACIER"
                }
            ]
        }
    ]
}
EOF

aws s3api put-bucket-lifecycle-configuration \
    --bucket nastto-logs-xxxxxxxxx \
    --lifecycle-configuration file://lifecycle-policy.json

Run in CloudShell
📨 Phase 6: SQS and SNS Setup (Day 6)
Step 6.1: Create SQS Queues
Create Queues:
# Email processing queue
aws sqs create-queue --queue-name nastto-email-queue

# Image processing queue
aws sqs create-queue --queue-name nastto-image-processing-queue

# Backup queue
aws sqs create-queue --queue-name nastto-backup-queue

# Dead letter queue
aws sqs create-queue --queue-name nastto-dead-letter-queue

Run in CloudShell
Configure Dead Letter Queue:
# Get queue URLs
EMAIL_QUEUE_URL=$(aws sqs get-queue-url --queue-name nastto-email-queue --query 'QueueUrl' --output text)
DLQ_URL=$(aws sqs get-queue-url --queue-name nastto-dead-letter-queue --query 'QueueUrl' --output text)

# Get DLQ ARN
DLQ_ARN=$(aws sqs get-queue-attributes --queue-url $DLQ_URL --attribute-names QueueArn --query 'Attributes.QueueArn' --output text)

# Set redrive policy
aws sqs set-queue-attributes \
    --queue-url $EMAIL_QUEUE_URL \
    --attributes '{
        "RedrivePolicy": "{\"deadLetterTargetArn\":\"'$DLQ_ARN'\",\"maxReceiveCount\":3}"
    }'

Run in CloudShell
Step 6.2: Create SNS Topics
Create Topics:
# User notifications topic
aws sns create-topic --name nastto-user-notifications

# System alerts topic
aws sns create-topic --name nastto-system-alerts

# Admin notifications topic
aws sns create-topic --name nastto-admin-notifications

Run in CloudShell
Subscribe SQS to SNS (Fan-out Pattern):
# Get topic ARN
TOPIC_ARN=$(aws sns list-topics --query 'Topics[?contains(TopicArn, `nastto-user-notifications`)].TopicArn' --output text)

# Get queue ARN
QUEUE_ARN=$(aws sqs get-queue-attributes --queue-url $EMAIL_QUEUE_URL --attribute-names QueueArn --query 'Attributes.QueueArn' --output text)

# Subscribe queue to topic
aws sns subscribe \
    --topic-arn $TOPIC_ARN \
    --protocol sqs \
    --notification-endpoint $QUEUE_ARN

# Allow SNS to send messages to SQS
cat > sqs-policy.json << EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Principal": {
                "Service": "sns.amazonaws.com"
            },
            "Action": "sqs:SendMessage",
            "Resource": "$QUEUE_ARN",
            "Condition": {
                "ArnEquals": {
                    "aws:SourceArn": "$TOPIC_ARN"
                }
            }
        }
    ]
}
EOF

aws sqs set-queue-attributes \
    --queue-url $EMAIL_QUEUE_URL \
    --attributes file://sqs-policy.json

Run in CloudShell
🖥️ Phase 7: EC2 Instance Setup (Day 7)
Step 7.1: Create Key Pair
Create SSH Key Pair:
aws ec2 create-key-pair \
    --key-name nastto-key \
    --query 'KeyMaterial' \
    --output text > nastto-key.pem

chmod 400 nastto-key.pem

Run in CloudShell
Step 7.2: Launch EC2 Instance
Create User Data Script:
cat > user-data.sh << 'EOF'
#!/bin/bash
yum update -y
yum install -y docker git

# Start Docker
systemctl start docker
systemctl enable docker
usermod -a -G docker ec2-user

# Install Docker Compose
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# Install Node.js (if needed)
curl -fsSL https://rpm.nodesource.com/setup_18.x | bash -
yum install -y nodejs

# Install Python and pip (if needed)
yum install -y python3 python3-pip

# Install AWS CLI v2
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
./aws/install

# Install Redis CLI
yum install -y gcc make
cd /tmp
wget http://download.redis.io/redis-stable.tar.gz
tar xvzf redis-stable.tar.gz
cd redis-stable
make
cp src/redis-cli /usr/local/bin/
chmod +x /usr/local/bin/redis-cli

# Install PostgreSQL client
yum install -y postgresql15

# Create application directory
mkdir -p /opt/nastto
chown ec2-user:ec2-user /opt/nastto
EOF

Run in CloudShell
Launch Instance:
aws ec2 run-instances \
    --image-id ami-0c02fb55956c7d316 \  # Amazon Linux 2023 AMI
    --count 1 \
    --instance-type t2.micro \
    --key-name nastto-key \
    --security-group-ids sg-xxxxxxxxx \  # Web security group ID
    --subnet-id subnet-xxxxxxxxx \  # Public subnet ID
    --user-data file://user-data.sh \
    --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=nastto-web-server}]' \
    --associate-public-ip-address

Run in CloudShell
Get Instance Information:
# Get instance ID
INSTANCE_ID=$(aws ec2 describe-instances \
    --filters "Name=tag:Name,Values=nastto-web-server" "Name=instance-state-name,Values=running" \
    --query 'Reservations[0].Instances[0].InstanceId' \
    --output text)

# Get public IP
PUBLIC_IP=$(aws ec2 describe-instances \
    --instance-ids $INSTANCE_ID \
    --query 'Reservations[0].Instances[0].PublicIpAddress' \
    --output text)

echo "Instance ID: $INSTANCE_ID"
echo "Public IP: $PUBLIC_IP"

Run in CloudShell
Step 7.3: Connect to EC2 Instance
SSH to Instance:
ssh -i nastto-key.pem ec2-user@$PUBLIC_IP

Run in CloudShell
Verify Installation:
# On EC2 instance
docker --version
docker-compose --version
aws --version
redis-cli --version
psql --version

# Test database connection
psql -h nastto-postgres.xxxxxxxxx.us-east-1.rds.amazonaws.com \
     -U nastto_admin \
     -d nastto_db

# Test Redis connection
redis-cli -h nastto-redis.xxxxxx.cache.amazonaws.com -p 6379 ping

Run in CloudShell
🚀 Phase 8: Application Deployment (Day 8-9)
Step 8.1: Prepare Application Code
Create Application Structure:
# On EC2 instance
cd /opt/nastto
git clone https://github.com/yourusername/your-app.git .

# Or upload your code
# scp -i nastto-key.pem -r ./your-app/* ec2-user@$PUBLIC_IP:/opt/nastto/

Run in CloudShell
Create Environment Configuration:
# Create .env file
cat > .env << EOF
# Database Configuration
DATABASE_URL=postgresql://nastto_admin:YourSecurePassword123!@nastto-postgres.xxxxxxxxx.us-east-1.rds.amazonaws.com:5432/nastto_db

# Redis Configuration
REDIS_URL=redis://nastto-redis.xxxxxx.cache.amazonaws.com:6379

# AWS Configuration
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key

# S3 Buckets
S3_STATIC_BUCKET=nastto-static-assets-xxxxxxxxx
S3_UPLOADS_BUCKET=nastto-user-uploads-xxxxxxxxx
S3_BACKUPS_BUCKET=nastto-backups-xxxxxxxxx
S3_LOGS_BUCKET=nastto-logs-xxxxxxxxx

# SQS Queues
SQS_EMAIL_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/123456789012/nastto-email-queue
SQS_IMAGE_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/123456789012/nastto-image-processing-queue

# SNS Topics
SNS_USER_NOTIFICATIONS_ARN=arn:aws:sns:us-east-1:123456789012:nastto-user-notifications
SNS_SYSTEM_ALERTS_ARN=arn:aws:sns:us-east-1:123456789012:nastto-system-alerts

# Application Configuration
APP_ENV=production
APP_PORT=8000
APP_HOST=0.0.0.0
EOF

Run in CloudShell
Step 8.2: Create Docker Configuration
Create Dockerfile:
# Dockerfile
FROM node:18-alpine  # or python:3.11-slim for Python apps

WORKDIR /app

# Copy package files
COPY package*.json ./  # or requirements.txt for Python

# Install dependencies
RUN npm install  # or pip install -r requirements.txt

# Copy application code
COPY . .

# Expose port
EXPOSE 8000

# Start application
CMD ["npm", "start"]  # or python app.py

Create Docker Compose:
# docker-compose.yml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8000:8000"
    environment:
      - NODE_ENV=production
    env_file:
      - .env
    restart: unless-stopped
    volumes:
      - ./logs:/app/logs
    depends_on:
      - nginx

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    restart: unless-stopped
    depends_on:
      - app

volumes:
  logs:

Create Nginx Configuration:
# nginx.conf
events {
    worker_connections 1024;
}

http {
    upstream app {
        server app:8000;
    }

    server {
        listen 80;
        server_name nastto.com www.nastto.com;

        location / {
            proxy_pass http://app;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        location /static/ {
            proxy_pass https://nastto-static-assets-xxxxxxxxx.s3.amazonaws.com/;
        }
    }
}

Step 8.3: Deploy Application
Build and Start Application:
# On EC2 instance
cd /opt/nastto

# Build and start services
docker-compose up -d --build

# Check status
docker-compose ps

# View logs
docker-compose logs -f app

Run in CloudShell
Test Application:
# Test local access
curl http://localhost:8000

# Test external access
curl http://$PUBLIC_IP

Run in CloudShell
🌍 Phase 9: Domain and SSL Setup (Day 10)
Step 9.1: Transfer Domain to Route 53
Create Hosted Zone:
aws route53 create-hosted-zone \
    --name nastto.com \
    --caller-reference $(date +%s)

Run in CloudShell
Get Name Servers:
aws route53 list-hosted-zones-by-name \
    --dns-name nastto.com \
    --query 'HostedZones[0].Id' \
    --output text

# Get nameservers
aws route53 get-hosted-zone \
    --id /hostedzone/ZXXXXXXXXXXXXX \
    --query 'DelegationSet.NameServers'

Run in CloudShell
Update Domain Nameservers at GoDaddy:
# Go to GoDaddy DNS management
# Replace nameservers with Route 53 nameservers
# Wait 24-48 hours for propagation

Run in CloudShell
Step 9.2: Create DNS Records
Create A Record:
cat > change-batch.json << EOF
{
    "Changes": [
        {
            "Action": "CREATE",
            "ResourceRecordSet": {
                "Name": "nastto.com",
                "Type": "A",
                "TTL": 300,
                "ResourceRecords": [
                    {
                        "Value": "$PUBLIC_IP"
                    }
                ]
            }
        },
        {
            "Action": "CREATE",
            "ResourceRecordSet": {
                "Name": "www.nastto.com",
                "Type": "A",
                "TTL": 300,
                "ResourceRecords": [
                    {
                        "Value": "$PUBLIC_IP"
                    }
                ]
            }
        }
    ]
}
EOF

aws route53 change-resource-record-sets \
    --hosted-zone-id /hostedzone/ZXXXXXXXXXXXXX \
    --change-batch file://change-batch.json

Run in CloudShell
Step 9.3: Set Up SSL Certificate
Request SSL Certificate:
aws acm request-certificate \
    --domain-name nastto.com \
    --subject-alternative-names www.nastto.com \
    --validation-method DNS \
    --region us-east-1

Run in CloudShell
Validate Certificate:
# Get certificate ARN
CERT_ARN=$(aws acm list-certificates \
    --query 'CertificateSummaryList[?DomainName==`nastto.com`].CertificateArn' \
    --output text)

# Get validation records
aws acm describe-certificate \
    --certificate-arn $CERT_ARN \
    --query 'Certificate.DomainValidationOptions'

# Add CNAME records to Route 53 for validation
# (Follow the output instructions)

Run in CloudShell
📊 Phase 10: Monitoring and Logging (Day 11)
Step 10.1: Set Up CloudWatch Monitoring
Create CloudWatch Alarms:
# CPU Utilization Alarm
aws cloudwatch put-metric-alarm \
    --alarm-name "nastto-high-cpu" \
    --alarm-description "High CPU utilization" \
    --metric-name CPUUtilization \
    --namespace AWS/EC2 \
    --statistic Average \
    --period 300 \
    --threshold 80 \
    --comparison-operator GreaterThanThreshold \
    --evaluation-periods 2 \
    --alarm-actions arn:aws:sns:us-east-1:123456789012:nastto-system-alerts \
    --dimensions Name=InstanceId,Value=$INSTANCE_ID

# Database Connection Alarm
aws cloudwatch put-metric-alarm \
    --alarm-name "nastto-db-connections" \
    --alarm-description "High database connections" \
    --metric-name DatabaseConnections \
    --namespace AWS/RDS \
    --statistic Average \
    --period 300 \
    --threshold 80 \
    --comparison-operator GreaterThanThreshold \
    --evaluation-periods 2 \
    --alarm-actions arn:aws:sns:us-east-1:123456789012:nastto-system-alerts \
    --dimensions Name=DBInstanceIdentifier,Value=nastto-postgres

Run in CloudShell
Step 10.2: Set Up Application Logging
Create Log Group:
aws logs create-log-group --log-group-name /aws/ec2/nastto-app

Run in CloudShell
Install CloudWatch Agent:
# On EC2 instance
wget https://s3.amazonaws.com/amazoncloudwatch-agent/amazon_linux/amd64/latest/amazon-cloudwatch-agent.rpm
sudo rpm -U ./amazon-cloudwatch-agent.rpm

# Create CloudWatch config
cat > /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json << EOF
{
    "logs": {
        "logs_collected": {
            "files": {
                "collect_list": [
                    {
                        "file_path": "/opt/nastto/logs/app.log",
                        "log_group_name": "/aws/ec2/nastto-app",
                        "log_stream_name": "{instance_id}/app.log"
                    }
                ]
            }
        }
    }
}
EOF

# Start CloudWatch agent
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
    -a fetch-config \
    -m ec2 \
    -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json \
    -s

Run in CloudShell
🔧 Phase 11: Application Integration Examples (Day 12)
Step 11.1: Database Integration
Python Example:
# database.py
import psycopg2
import os
from contextlib import contextmanager

DATABASE_URL = os.getenv('DATABASE_URL')

@contextmanager
def get_db_connection():
    conn = None
    try:
        conn = psycopg2.connect(DATABASE_URL)
        yield conn
    except Exception as e:
        if conn:
            conn.rollback()
        raise e
    finally:
        if conn:
            conn.close()

def create_user(username, email):
    with get_db_connection() as conn:
        cursor = conn.cursor()
        cursor.execute(
            "INSERT INTO users (username, email) VALUES (%s, %s) RETURNING id",
            (username, email)
        )
        user_id = cursor.fetchone()[0]
        conn.commit()
        return user_id

def get_user(user_id):
    with get_db_connection() as conn:
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM users WHERE id = %s", (user_id,))
        return cursor.fetchone()

Node.js Example:
// database.js
const { Pool } = require('pg');

const pool = new Pool({
    connectionString: process.env.DATABASE_URL,
    ssl: process.env.NODE_ENV === 'production' ? { rejectUnauthorized: false } : false
});

async function createUser(username, email) {
    const client = await pool.connect();
    try {
        const result = await client.query(
            'INSERT INTO users (username, email) VALUES ($1, $2) RETURNING id',
            [username, email]
        );
        return result.rows[0].id;
    } finally {
        client.release();
    }
}

async function getUser(userId) {
    const client = await pool.connect();
    try {
        const result = await client.query('SELECT * FROM users WHERE id = $1', [userId]);
        return result.rows[0];
    } finally {
        client.release();
    }
}

module.exports = { createUser, getUser };

Step 11.2: Redis Cache Integration
Python Example:
# cache.py
import redis
import json
import os
from functools import wraps

redis_client = redis.Redis.from_url(os.getenv('REDIS_URL'), decode_responses=True)

def cache_result(expiration=300):
    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            cache_key = f"{func.__name__}:{hash(str(args) + str(kwargs))}"
            
            try:
                cached_result = redis_client.get(cache_key)
                if cached_result:
                    return json.loads(cached_result)
            except Exception as e:
                print(f"Cache read error: {e}")
            
            result = func(*args, **kwargs)
            
            try:
                redis_client.setex(cache_key, expiration, json.dumps(result, default=str))
            except Exception as e:
                print(f"Cache write error: {e}")
            
            return result
        return wrapper
    return decorator

# Session management
class SessionManager:
    def __init__(self):
        self.redis = redis_client
        self.session_timeout = 3600  # 1 hour
    
    def create_session(self, user_id):
        import uuid
        from datetime import datetime
        
        session_id = str(uuid.uuid4())
        session_data = {
            'user_id': user_id,
            'created_at': datetime.now().isoformat(),
            'last_accessed': datetime.now().isoformat()
        }
        
        self.redis.setex(
            f"session:{session_id}",
            self.session_timeout,
            json.dumps(session_data)
        )
        
        return session_id
    
    def get_session(self, session_id):
        try:
            session_data = self.redis.get(f"session:{session_id}")
            if session_data:
                return json.loads(session_data)
        except Exception as e:
            print(f"Session error: {e}")
        return None
    
    def delete_session(self, session_id):
        self.redis.delete(f"session:{session_id}")

session_manager = SessionManager()

Node.js Example:
// cache.js
const redis = require('redis');
const client = redis.createClient({ url: process.env.REDIS_URL });

client.on('error', (err) => console.log('Redis Client Error', err));
client.connect();

function cacheResult(expiration = 300) {
    return function(target, propertyName, descriptor) {
        const method = descriptor.value;
        
        descriptor.value = async function(...args) {
            const cacheKey = `${propertyName}:${JSON.stringify(args)}`;
            
            try {
                const cached = await client.get(cacheKey);
                if (cached) {
                    return JSON.parse(cached);
                }
            } catch (error) {
                console.log('Cache read error:', error);
            }
            
            const result = await method.apply(this, args);
            
            try {
                await client.setEx(cacheKey, expiration, JSON.stringify(result));
            } catch (error) {
                console.log('Cache write error:', error);
            }
            
            return result;
        };
    };
}

// Session management
class SessionManager {
    constructor() {
        this.sessionTimeout = 3600; // 1 hour
    }
    
    async createSession(userId) {
        const { v4: uuidv4 } = require('uuid');
        const sessionId = uuidv4();
        const sessionData = {
            userId,
            createdAt: new Date().toISOString(),
            lastAccessed: new Date().toISOString()
        };
        
        await client.setEx(
            `session:${sessionId}`,
            this.sessionTimeout,
            JSON.stringify(sessionData)
        );
        
        return sessionId;
    }
    
    async getSession(sessionId) {
        try {
            const sessionData = await client.get(`session:${sessionId}`);
            if (sessionData) {
                return JSON.parse(sessionData);
            }
        } catch (error) {
            console.log('Session error:', error);
        }
        return null;
    }
    
    async deleteSession(sessionId) {
        await client.del(`session:${sessionId}`);
    }
}

module.exports = { cacheResult, SessionManager };

Step 11.3: S3 Integration
Python Example:
# s3_service.py
import boto3
import os
from botocore.exceptions import ClientError

s3_client = boto3.client('s3')

STATIC_BUCKET = os.getenv('S3_STATIC_BUCKET')
UPLOADS_BUCKET = os.getenv('S3_UPLOADS_BUCKET')
BACKUPS_BUCKET = os.getenv('S3_BACKUPS_BUCKET')

def upload_file(file_obj, bucket, key):
    try:
        s3_client.upload_fileobj(file_obj, bucket, key)
        return f"https://{bucket}.s3.amazonaws.com/{key}"
    except ClientError as e:
        print(f"Upload error: {e}")
        return None

def upload_user_file(file_obj, filename, user_id):
    key = f"users/{user_id}/{filename}"
    return upload_file(file_obj, UPLOADS_BUCKET, key)

def upload_static_asset(file_obj, filename):
    key = f"public/{filename}"
    return upload_file(file_obj, STATIC_BUCKET, key)

def create_presigned_url(bucket, key, expiration=3600):
    try:
        response = s3_client.generate_presigned_url(
            'get_object',
            Params={'Bucket': bucket, 'Key': key},
            ExpiresIn=expiration
        )
        return response
    except ClientError as e:
        print(f"Presigned URL error: {e}")
        return None

def backup_database():
    import subprocess
    import datetime
    
    timestamp = datetime.datetime.now().strftime('%Y%m%d_%H%M%S')
    backup_filename = f"db_backup_{timestamp}.sql"
    
    # Create database backup
    subprocess.run([
        'pg_dump',
        os.getenv('DATABASE_URL'),
        '-f', f'/tmp/{backup_filename}'
    ])
    
    # Upload to S3
    with open(f'/tmp/{backup_filename}', 'rb') as f:
        upload_file(f, BACKUPS_BUCKET, f"database/{backup_filename}")
    
    # Clean up local file
    os.remove(f'/tmp/{backup_filename}')

Step 11.4: SQS and SNS Integration
Python Example:
# messaging.py
import boto3
import json
import os

sqs_client = boto3.client('sqs')
sns_client = boto3.client('sns')

EMAIL_QUEUE_URL = os.getenv('SQS_EMAIL_QUEUE_URL')
IMAGE_QUEUE_URL = os.getenv('SQS_IMAGE_QUEUE_URL')
USER_NOTIFICATIONS_ARN = os.getenv('SNS_USER_NOTIFICATIONS_ARN')
SYSTEM_ALERTS_ARN = os.getenv('SNS_SYSTEM_ALERTS_ARN')

def send_email_task(to_email, subject, body):
    message = {
        'to_email': to_email,
        'subject': subject,
        'body': body,
        'timestamp': datetime.now().isoformat()
    }
    
    sqs_client.send_message(
        QueueUrl=EMAIL_QUEUE_URL,
        MessageBody=json.dumps(message)
    )

def send_image_processing_task(image_url, user_id, processing_type):
    message = {
        'image_url': image_url,
        'user_id': user_id,
        'processing_type': processing_type,
        'timestamp': datetime.now().isoformat()
    }
    
    sqs_client.send_message(
        QueueUrl=IMAGE_QUEUE_URL,
        MessageBody=json.dumps(message)
    )

def publish_user_notification(user_id, notification_type, message):
    notification = {
        'user_id': user_id,
        'type': notification_type,
        'message': message,
        'timestamp': datetime.now().isoformat()
    }
    
    sns_client.publish(
        TopicArn=USER_NOTIFICATIONS_ARN,
        Message=json.dumps(notification),
        Subject=f"User Notification: {notification_type}"
    )

def publish_system_alert(alert_type, message, severity='INFO'):
    alert = {
        'type': alert_type,
        'message': message,
        'severity': severity,
        'timestamp': datetime.now().isoformat()
    }
    
    sns_client.publish(
        TopicArn=SYSTEM_ALERTS_ARN,
        Message=json.dumps(alert),
        Subject=f"System Alert: {alert_type}"
    )

# Background task processor
def process_email_queue():
    while True:
        response = sqs_client.receive_message(
            QueueUrl=EMAIL_QUEUE_URL,
            MaxNumberOfMessages=10,
            WaitTimeSeconds=20
        )
        
        messages = response.get('Messages', [])
        
        for message in messages:
            try:
                body = json.loads(message['Body'])
                
                # Process email (integrate with your email service)
                send_actual_email(
                    body['to_email'],
                    body['subject'],
                    body['body']
                )
                
                # Delete message from queue
                sqs_client.delete_message(
                    QueueUrl=EMAIL_QUEUE_URL,
                    ReceiptHandle=message['ReceiptHandle']
                )
                
            except Exception as e:
                print(f"Error processing message: {e}")
                # Message will be retried or sent to DLQ

def send_actual_email(to_email, subject, body):
    # Implement your email sending logic here
    # Could use SES, SendGrid, etc.
    pass

🔄 Phase 12: Backup and Maintenance (Day 13)
Step 12.1: Automated Backups
Create Backup Script:
#!/bin/bash
# backup.sh

# Set variables
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/tmp/backups"
S3_BACKUP_BUCKET="nastto-backups-xxxxxxxxx"

# Create backup directory
mkdir -p $BACKUP_DIR

# Database backup
echo "Creating database backup..."
pg_dump $DATABASE_URL > $BACKUP_DIR/db_backup_$TIMESTAMP.sql

# Application files backup
echo "Creating application backup..."
tar -czf $BACKUP_DIR/app_backup_$TIMESTAMP.tar.gz /opt/nastto --exclude=/opt/nastto/node_modules

# Upload to S3
echo "Uploading backups to S3..."
aws s3 cp $BACKUP_DIR/db_backup_$TIMESTAMP.sql s3://$S3_BACKUP_BUCKET/database/
aws s3 cp $BACKUP_DIR/app_backup_$TIMESTAMP.tar.gz s3://$S3_BACKUP_BUCKET/application/

# Clean up local backups (keep last 3 days)
find $BACKUP_DIR -name "*.sql" -mtime +3 -delete
find $BACKUP_DIR -name "*.tar.gz" -mtime +3 -delete

echo "Backup completed successfully"

Run in CloudShell
Set Up Cron Job:
# Make script executable
chmod +x /opt/nastto/backup.sh

# Add to crontab (daily at 2 AM)
crontab -e
# Add this line:
0 2 * * * /opt/nastto/backup.sh >> /var/log/backup.log 2>&1

Run in CloudShell
Step 12.2: Health Check Script
Create Health Check:
#!/bin/bash
# health_check.sh

# Check application health
APP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8000/health)

# Check database connection
DB_STATUS=$(psql $DATABASE_URL -c "SELECT 1;" > /dev/null 2>&1 && echo "OK" || echo "FAIL")

# Check Redis connection
REDIS_STATUS=$(redis-cli -h nastto-redis.xxxxxx.cache.amazonaws.com -p 6379 ping 2>/dev/null)

# Send alerts if any service is down
if [ "$APP_STATUS" != "200" ] || [ "$DB_STATUS" != "OK" ] || [ "$REDIS_STATUS" != "PONG" ]; then
    aws sns publish \
        --topic-arn $SNS_SYSTEM_ALERTS_ARN \
        --message "Health check failed: APP=$APP_STATUS, DB=$DB_STATUS, REDIS=$REDIS_STATUS" \
        --subject "System Health Alert"
fi

Run in CloudShell
📈 Phase 13: Performance Optimization (Day 14)
Step 13.1: Application Performance
Implement Caching Strategy:
# performance.py
from functools import wraps
import time

# Cache frequently accessed data
@cache_result(expiration=3600)  # 1 hour
def get_popular_posts():
    return database.query("SELECT * FROM posts ORDER BY views DESC LIMIT 10")

@cache_result(expiration=1800)  # 30 minutes
def get_user_stats(user_id):
    return database.query(f"""
        SELECT 
            COUNT(*) as post_count,
            SUM(views) as total_views,
            MAX(created_at) as last_post
        FROM posts 
        WHERE user_id = {user_id}
    """)

# Implement rate limiting
def rate_limit(max_requests=100, window=3600):
    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            client_id = kwargs.get('client_id', 'anonymous')
            key = f"rate_limit:{func.__name__}:{client_id}"
            
            current_requests = redis_client.get(key) or 0
            if int(current_requests) >= max_requests:
                raise Exception("Rate limit exceeded")
            
            redis_client.incr(key)
            redis_client.expire(key, window)
            
            return func(*args, **kwargs)
        return wrapper
    return decorator

# Database connection pooling
import psycopg2.pool

db_pool = psycopg2.pool.ThreadedConnectionPool(
    minconn=1,
    maxconn=20,
    dsn=DATABASE_URL
)

def get_db_connection():
    return db_pool.getconn()

def return_db_connection(conn):
    db_pool.putconn(conn)

Step 13.2: Database Optimization
Create Database Indexes:
-- Add indexes for better performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);
CREATE INDEX idx_posts_views ON posts(views DESC);

-- Analyze query performance
EXPLAIN ANALYZE SELECT * FROM posts WHERE user_id = 123 ORDER BY created_at DESC LIMIT 10;

🚨 Phase 14: Security Hardening (Day 15)
Step 14.1: Security Best Practices
Update Security Groups:
# Restrict SSH access to your IP only
MY_IP=$(curl -s https://checkip.amazonaws.com)
aws ec2 revoke-security-group-ingress \
    --group-id sg-xxxxxxxxx \
    --protocol tcp \
    --port 22 \
    --cidr 0.0.0.0/0

aws ec2 authorize-security-group-ingress \
    --group-id sg-xxxxxxxxx \
    --protocol tcp \
    --port 22 \
    --cidr $MY_IP/32

Run in CloudShell
Enable SSL/TLS:
# Update nginx.conf for SSL
server {
    listen 80;
    server_name nastto.com www.nastto.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name nastto.com www.nastto.com;

    ssl_certificate /etc/nginx/ssl/nastto.com.crt;
    ssl_certificate_key /etc/nginx/ssl/nastto.com.key;
    
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512;
    ssl_prefer_server_ciphers off;
    
    location / {
        proxy_pass http://app;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

Step 14.2: Environment Security
Secure Environment Variables:
# Use AWS Systems Manager Parameter Store for secrets
aws ssm put-parameter \
    --name "/nastto/database/password" \
    --value "YourSecurePassword123!" \
    --type "SecureString"

aws ssm put-parameter \
    --name "/nastto/jwt/secret" \
    --value "your-jwt-secret-key" \
    --type "SecureString"

Run in CloudShell
Update Application to Use Parameter Store:
# secrets.py
import boto3

ssm_client = boto3.client('ssm')

def get_secret(parameter_name):
    try:
        response = ssm_client.get_parameter(
            Name=parameter_name,
            WithDecryption=True
        )
        return response['Parameter']['Value']
    except Exception as e:
        print(f"Error getting secret {parameter_name}: {e}")
        return None

# Usage
DATABASE_PASSWORD = get_secret('/nastto/database/password')
JWT_SECRET = get_secret('/nastto/jwt/secret')

📊 Final Checklist and Testing
✅ Deployment Checklist:
Infrastructure:
 VPC and subnets created
 Security groups configured
 EC2 instance running
 RDS PostgreSQL accessible
 ElastiCache Redis accessible
 S3 buckets created and configured
 SQS queues created
 SNS topics created
Application:
 Application deployed and running
 Database connection working
 Redis cache working
 File uploads to S3 working
 Message queues working
 Notifications working
Domain and SSL:
 Domain transferred to Route 53
 DNS records created
 SSL certificate issued and installed
 HTTPS redirect working
Monitoring:
 CloudWatch alarms configured
 Log aggregation working
 Health checks in place
 Backup automation working
Security:
 Security groups properly configured
 SSL/TLS enabled
 Secrets stored securely
 Access keys rotated
🧪 Testing Commands:
# Test application endpoints
curl https://nastto.com/health
curl https://nastto.com/api/users

# Test database connection
psql $DATABASE_URL -c "SELECT version();"

# Test Redis connection
redis-cli -h nastto-redis.xxxxxx.cache.amazonaws.com -p 6379 ping

# Test S3 upload
aws s3 cp test.txt s3://nastto-static-assets-xxxxxxxxx/test.txt

# Test SQS
aws sqs send-message \
    --queue-url $EMAIL_QUEUE_URL \
    --message-body '{"test": "message"}'

# Test SNS
aws sns publish \
    --topic-arn $USER_NOTIFICATIONS_ARN \
    --message "Test notification"

Run in CloudShell
💰 Cost Monitoring
Expected Monthly Costs (First 12 Months):
EC2 t2.micro: $0 (free tier)
RDS db.t3.micro: $0 (free tier)
ElastiCache cache.t3.micro: $0 (free tier)
S3 storage and requests: $0 (within free tier limits)
SQS requests: $0 (within free tier limits)
SNS requests: $0 (within free tier limits)
Route 53 hosted zone: $0.50/month
Data transfer: ~$1-2/month
Total: $1.50-$2.50/month
After Free Tier (Month 13+):
EC2 t2.micro: ~$8.50/month
RDS db.t3.micro: ~$12.50/month
ElastiCache cache.t3.micro: ~$12.50/month
Other services: ~$2-5/month
Total: ~$35-40/month
🎉 Congratulations!
You now have a complete, production-ready application deployed on AWS with:

✅ High Availability: Multi-AZ setup with load balancing ✅ Scalability: Auto-scaling capabilities and caching ✅ Security: SSL/TLS, security groups, and secret management ✅ Monitoring: CloudWatch alarms and logging ✅ Backup: Automated database and application backups ✅ Cost Optimization: Free tier utilization and cost monitoring

Your application is now ready to serve users with professional-grade infrastructure while staying within budget!



