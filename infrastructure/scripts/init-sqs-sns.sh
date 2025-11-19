#!/bin/bash

###############################################################################
# Script: init-sqs-sns.sh
# Description: Configure SQS queues and SNS topics with message schemas
# Usage: Run this script after stack creation to configure queues and topics
# 
# This is a PLACEHOLDER script. Customize it with your message schemas.
###############################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== SQS & SNS Configuration Script ===${NC}"
echo ""

# Load environment variables
if [ -f "/opt/nashtto/.env" ]; then
    echo -e "${GREEN}✓ Loading environment variables from /opt/nashtto/.env${NC}"
    source /opt/nashtto/.env
else
    echo -e "${RED}✗ Error: Environment file not found at /opt/nashtto/.env${NC}"
    echo "Please ensure the CloudFormation stack has been created successfully."
    exit 1
fi

# Verify required environment variables
if [ -z "$SQS_EMAIL_QUEUE_URL" ] || [ -z "$SNS_USER_NOTIFICATIONS_ARN" ]; then
    echo -e "${RED}✗ Error: Required environment variables not set${NC}"
    echo "Required: SQS queue URLs and SNS topic ARNs"
    exit 1
fi

echo -e "${GREEN}✓ Environment variables loaded${NC}"
echo ""

###############################################################################
# CONFIGURE SQS QUEUES
###############################################################################

echo -e "${BLUE}=== Configuring SQS Queues ===${NC}"
echo ""

# Function to set queue attributes
configure_queue() {
    local queue_url=$1
    local queue_name=$2
    local visibility_timeout=$3
    local message_retention=$4
    
    echo -e "${YELLOW}Configuring: $queue_name${NC}"
    echo "  Queue URL: $queue_url"
    
    if [ -z "$queue_url" ]; then
        echo -e "${RED}  ✗ Queue URL not found${NC}"
        return
    fi
    
    # Set queue attributes
    aws sqs set-queue-attributes \
        --queue-url "$queue_url" \
        --attributes \
            VisibilityTimeout=$visibility_timeout,\
            MessageRetentionPeriod=$message_retention,\
            ReceiveMessageWaitTimeSeconds=20 \
        2>/dev/null
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}  ✓ Queue configured successfully${NC}"
    else
        echo -e "${RED}  ✗ Error configuring queue${NC}"
    fi
    echo ""
}

# Configure Email Queue
configure_queue "$SQS_EMAIL_QUEUE_URL" "Email Queue" 300 345600

# Configure Image Processing Queue
configure_queue "$SQS_IMAGE_QUEUE_URL" "Image Processing Queue" 600 345600

# Configure Backup Queue
configure_queue "$SQS_BACKUP_QUEUE_URL" "Backup Queue" 900 345600

# Configure Dead Letter Queue
configure_queue "$SQS_DLQ_URL" "Dead Letter Queue" 300 1209600

###############################################################################
# MESSAGE SCHEMAS (Documentation)
###############################################################################

echo -e "${BLUE}=== Message Schemas ===${NC}"
echo ""

cat << 'EOF'
Below are the recommended message schemas for each queue:

---
1. EMAIL QUEUE
---
Schema:
{
  "type": "email",
  "to": "user@example.com",
  "subject": "Subject line",
  "template": "welcome_email | order_confirmation | password_reset",
  "data": {
    "user_name": "John Doe",
    "order_number": "ORD-12345",
    "custom_field": "value"
  },
  "priority": "high | normal | low",
  "timestamp": "2025-11-18T10:00:00Z"
}

Example usage (AWS CLI):
aws sqs send-message \
  --queue-url $SQS_EMAIL_QUEUE_URL \
  --message-body '{
    "type": "email",
    "to": "customer@example.com",
    "subject": "Order Confirmation",
    "template": "order_confirmation",
    "data": {
      "order_number": "ORD-12345",
      "total_amount": "29.99"
    }
  }'

---
2. IMAGE PROCESSING QUEUE
---
Schema:
{
  "type": "image_processing",
  "action": "resize | compress | watermark | thumbnail",
  "source_bucket": "nashtto-user-uploads-ACCOUNT_ID",
  "source_key": "uploads/image.jpg",
  "destination_bucket": "nashtto-static-assets-ACCOUNT_ID",
  "destination_key": "processed/image.jpg",
  "options": {
    "width": 800,
    "height": 600,
    "quality": 85,
    "format": "jpeg | png | webp"
  },
  "timestamp": "2025-11-18T10:00:00Z"
}

Example usage (AWS CLI):
aws sqs send-message \
  --queue-url $SQS_IMAGE_QUEUE_URL \
  --message-body '{
    "type": "image_processing",
    "action": "resize",
    "source_bucket": "'$S3_UPLOADS_BUCKET'",
    "source_key": "uploads/restaurant-logo.jpg",
    "destination_bucket": "'$S3_STATIC_BUCKET'",
    "destination_key": "public/logos/restaurant-logo-thumb.jpg",
    "options": {
      "width": 200,
      "height": 200,
      "quality": 90
    }
  }'

---
3. BACKUP QUEUE
---
Schema:
{
  "type": "backup",
  "resource": "database | files | config",
  "source": "database_name | s3_bucket | path",
  "destination_bucket": "nashtto-backups-ACCOUNT_ID",
  "destination_prefix": "backups/2025-11-18/",
  "compression": true,
  "encryption": true,
  "retention_days": 30,
  "timestamp": "2025-11-18T10:00:00Z"
}

Example usage (AWS CLI):
aws sqs send-message \
  --queue-url $SQS_BACKUP_QUEUE_URL \
  --message-body '{
    "type": "backup",
    "resource": "database",
    "source": "nastto_db",
    "destination_bucket": "'$S3_BACKUPS_BUCKET'",
    "destination_prefix": "backups/database/'$(date +%Y-%m-%d)'/",
    "compression": true,
    "encryption": true,
    "retention_days": 90
  }'

---
4. DEAD LETTER QUEUE
---
This queue receives messages that failed processing after max retries.
Review these messages regularly to identify issues.

View messages:
aws sqs receive-message \
  --queue-url $SQS_DLQ_URL \
  --max-number-of-messages 10

EOF

###############################################################################
# CONFIGURE SNS TOPICS
###############################################################################

echo -e "${BLUE}=== Configuring SNS Topics ===${NC}"
echo ""

# Function to add email subscription to topic
add_email_subscription() {
    local topic_arn=$1
    local topic_name=$2
    local email=$3
    
    echo -e "${YELLOW}Configuring: $topic_name${NC}"
    echo "  Topic ARN: $topic_arn"
    
    if [ -z "$topic_arn" ]; then
        echo -e "${RED}  ✗ Topic ARN not found${NC}"
        return
    fi
    
    if [ -z "$email" ]; then
        echo -e "${YELLOW}  ⊗ No email provided - skipping subscription${NC}"
        return
    fi
    
    # Subscribe email to topic
    aws sns subscribe \
        --topic-arn "$topic_arn" \
        --protocol email \
        --notification-endpoint "$email" \
        2>/dev/null
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}  ✓ Email subscription created (check inbox for confirmation)${NC}"
    else
        echo -e "${YELLOW}  ⚠ Subscription may already exist${NC}"
    fi
    echo ""
}

# Prompt for email addresses (optional)
echo "Would you like to add email subscriptions to SNS topics?"
echo ""
read -p "Enter email for User Notifications (or press Enter to skip): " USER_EMAIL
read -p "Enter email for Admin Notifications (or press Enter to skip): " ADMIN_EMAIL
read -p "Enter email for System Alerts (or press Enter to skip): " SYSTEM_EMAIL

echo ""

# Add subscriptions
add_email_subscription "$SNS_USER_NOTIFICATIONS_ARN" "User Notifications" "$USER_EMAIL"
add_email_subscription "$SNS_ADMIN_NOTIFICATIONS_ARN" "Admin Notifications" "$ADMIN_EMAIL"
add_email_subscription "$SNS_SYSTEM_ALERTS_ARN" "System Alerts" "$SYSTEM_EMAIL"

###############################################################################
# SNS MESSAGE SCHEMAS (Documentation)
###############################################################################

echo -e "${BLUE}=== SNS Publishing Examples ===${NC}"
echo ""

cat << 'EOF'
Below are examples of publishing messages to SNS topics:

---
1. USER NOTIFICATIONS TOPIC
---
Purpose: Notifications to end users (order updates, promotions, etc.)

Example:
aws sns publish \
  --topic-arn $SNS_USER_NOTIFICATIONS_ARN \
  --subject "Order Update" \
  --message '{
    "type": "order_update",
    "user_id": "12345",
    "order_number": "ORD-12345",
    "status": "delivered",
    "message": "Your order has been delivered!",
    "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%SZ)'"
  }'

---
2. ADMIN NOTIFICATIONS TOPIC
---
Purpose: Notifications to admin users (new orders, reviews, etc.)

Example:
aws sns publish \
  --topic-arn $SNS_ADMIN_NOTIFICATIONS_ARN \
  --subject "New Order Received" \
  --message '{
    "type": "new_order",
    "order_number": "ORD-12345",
    "restaurant_id": "67890",
    "total_amount": "29.99",
    "customer_name": "John Doe",
    "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%SZ)'"
  }'

---
3. SYSTEM ALERTS TOPIC
---
Purpose: Critical system alerts (errors, resource limits, security issues)

Example:
aws sns publish \
  --topic-arn $SNS_SYSTEM_ALERTS_ARN \
  --subject "ALERT: High CPU Usage" \
  --message '{
    "type": "resource_alert",
    "severity": "warning | error | critical",
    "resource": "EC2 Instance i-1234567890",
    "metric": "CPUUtilization",
    "value": "85%",
    "threshold": "80%",
    "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%SZ)'"
  }'

EOF

###############################################################################
# TEST MESSAGES (Optional)
###############################################################################

echo -e "${BLUE}=== Send Test Messages ===${NC}"
echo ""
echo "Would you like to send test messages to verify the setup?"
read -p "Send test messages? (yes/no): " SEND_TEST

if [ "$SEND_TEST" == "yes" ]; then
    echo ""
    echo -e "${YELLOW}Sending test messages...${NC}"
    
    # Test Email Queue
    echo "  • Email Queue..."
    aws sqs send-message \
        --queue-url "$SQS_EMAIL_QUEUE_URL" \
        --message-body '{"type":"test","message":"Test message from init-sqs-sns.sh"}' \
        > /dev/null 2>&1
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}    ✓ Test message sent to Email Queue${NC}"
    fi
    
    # Test User Notifications Topic
    if [ ! -z "$SNS_USER_NOTIFICATIONS_ARN" ]; then
        echo "  • User Notifications Topic..."
        aws sns publish \
            --topic-arn "$SNS_USER_NOTIFICATIONS_ARN" \
            --subject "Test Notification" \
            --message "Test notification from init-sqs-sns.sh - $(date)" \
            > /dev/null 2>&1
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}    ✓ Test message published to User Notifications Topic${NC}"
        fi
    fi
    
    echo ""
    echo -e "${GREEN}✓ Test messages sent${NC}"
    echo "Check SQS queues and email inbox for test messages."
fi

###############################################################################
# MONITORING COMMANDS
###############################################################################

echo ""
echo -e "${BLUE}=== Monitoring Commands ===${NC}"
echo ""

cat << 'EOF'
Useful commands for monitoring queues and topics:

# View queue attributes
aws sqs get-queue-attributes \
  --queue-url $SQS_EMAIL_QUEUE_URL \
  --attribute-names All

# Receive messages from queue
aws sqs receive-message \
  --queue-url $SQS_EMAIL_QUEUE_URL \
  --max-number-of-messages 10

# Get approximate message count
aws sqs get-queue-attributes \
  --queue-url $SQS_EMAIL_QUEUE_URL \
  --attribute-names ApproximateNumberOfMessages

# List SNS subscriptions
aws sns list-subscriptions-by-topic \
  --topic-arn $SNS_USER_NOTIFICATIONS_ARN

# View CloudWatch metrics for SQS
aws cloudwatch get-metric-statistics \
  --namespace AWS/SQS \
  --metric-name ApproximateNumberOfMessagesVisible \
  --dimensions Name=QueueName,Value=nashtto-email-queue \
  --start-time $(date -u -d '1 hour ago' +%Y-%m-%dT%H:%M:%S) \
  --end-time $(date -u +%Y-%m-%dT%H:%M:%S) \
  --period 300 \
  --statistics Average

EOF

###############################################################################
# INTEGRATION WITH APPLICATION
###############################################################################

echo -e "${BLUE}=== Application Integration ===${NC}"
echo ""

cat << 'EOF'
To integrate SQS and SNS with your application:

1. Python (boto3):
   ```python
   import boto3
   import json
   
   # SQS Client
   sqs = boto3.client('sqs')
   
   # Send message
   response = sqs.send_message(
       QueueUrl=os.getenv('SQS_EMAIL_QUEUE_URL'),
       MessageBody=json.dumps({
           'type': 'email',
           'to': 'user@example.com',
           'subject': 'Test'
       })
   )
   
   # Receive messages
   messages = sqs.receive_message(
       QueueUrl=os.getenv('SQS_EMAIL_QUEUE_URL'),
       MaxNumberOfMessages=10,
       WaitTimeSeconds=20
   )
   
   # SNS Client
   sns = boto3.client('sns')
   
   # Publish message
   response = sns.publish(
       TopicArn=os.getenv('SNS_USER_NOTIFICATIONS_ARN'),
       Subject='Notification',
       Message=json.dumps({'type': 'notification', 'data': {}})
   )
   ```

2. Node.js (AWS SDK v3):
   ```javascript
   const { SQSClient, SendMessageCommand } = require('@aws-sdk/client-sqs');
   const { SNSClient, PublishCommand } = require('@aws-sdk/client-sns');
   
   // SQS
   const sqsClient = new SQSClient();
   await sqsClient.send(new SendMessageCommand({
       QueueUrl: process.env.SQS_EMAIL_QUEUE_URL,
       MessageBody: JSON.stringify({ type: 'email', ... })
   }));
   
   // SNS
   const snsClient = new SNSClient();
   await snsClient.send(new PublishCommand({
       TopicArn: process.env.SNS_USER_NOTIFICATIONS_ARN,
       Subject: 'Notification',
       Message: JSON.stringify({ type: 'notification', ... })
   }));
   ```

3. Java (AWS SDK v2):
   ```java
   import software.amazon.awssdk.services.sqs.SqsClient;
   import software.amazon.awssdk.services.sns.SnsClient;
   
   // SQS
   SqsClient sqsClient = SqsClient.create();
   sqsClient.sendMessage(builder -> builder
       .queueUrl(System.getenv("SQS_EMAIL_QUEUE_URL"))
       .messageBody("{\"type\":\"email\"}")
   );
   
   // SNS
   SnsClient snsClient = SnsClient.create();
   snsClient.publish(builder -> builder
       .topicArn(System.getenv("SNS_USER_NOTIFICATIONS_ARN"))
       .subject("Notification")
       .message("{\"type\":\"notification\"}")
   );
   ```

EOF

###############################################################################
# COMPLETION
###############################################################################

echo -e "${GREEN}==================================${NC}"
echo -e "${GREEN}✓ SQS & SNS configuration complete!${NC}"
echo -e "${GREEN}==================================${NC}"
echo ""
echo "Queue URLs:"
echo "  Email Queue: $SQS_EMAIL_QUEUE_URL"
echo "  Image Queue: $SQS_IMAGE_QUEUE_URL"
echo "  Backup Queue: $SQS_BACKUP_QUEUE_URL"
echo "  Dead Letter Queue: $SQS_DLQ_URL"
echo ""
echo "Topic ARNs:"
echo "  User Notifications: $SNS_USER_NOTIFICATIONS_ARN"
echo "  Admin Notifications: $SNS_ADMIN_NOTIFICATIONS_ARN"
echo "  System Alerts: $SNS_SYSTEM_ALERTS_ARN"
echo ""
echo "Next Steps:"
echo "  1. Confirm email subscriptions (check inbox)"
echo "  2. Integrate queue processing in your application"
echo "  3. Set up workers to process messages"
echo "  4. Monitor queues in CloudWatch"
echo ""


