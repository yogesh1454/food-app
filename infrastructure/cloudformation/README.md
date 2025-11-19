# AWS CloudFormation Infrastructure Automation

This directory contains a complete CloudFormation template and supporting scripts to automate the deployment of the entire AWS infrastructure for the Nashtto food delivery application.

## 📁 Directory Structure

```
cloudformation/
├── infrastructure-stack.yaml         # Main CloudFormation template
├── parameters.json                    # Parameter configuration file
├── environment-variables-template.txt # Environment variables template
└── README.md                          # This file

docs/
├── deployment-instructions.md         # Step-by-step deployment guide
└── cleanup-instructions.md            # Infrastructure teardown guide

scripts/
├── empty-s3-buckets.sh               # Script to empty S3 buckets before deletion
├── init-database.sh                  # Database initialization script (placeholder)
└── init-sqs-sns.sh                   # SQS/SNS configuration script (placeholder)
```

## 🎯 What Gets Deployed

The CloudFormation stack creates the following AWS resources:

### Network Infrastructure (Phase 2)
- ✅ VPC (10.0.0.0/16)
- ✅ 2 Public Subnets across 2 Availability Zones
- ✅ 2 Private Subnets across 2 Availability Zones
- ✅ Internet Gateway
- ✅ NAT Gateway (1 AZ for cost optimization)
- ✅ Route Tables (public and private)
- ✅ Security Groups (Web, Database, Cache)

### Database Infrastructure (Phase 3)
- ✅ RDS PostgreSQL (db.t3.micro, 20GB, encrypted)
- ✅ DB Subnet Group
- ✅ Automated backups (7-day retention)
- ✅ CloudWatch logs enabled

### Cache Infrastructure (Phase 4)
- ✅ ElastiCache Redis (cache.t3.micro, Redis 7.0)
- ✅ Cache Subnet Group
- ✅ Single node configuration

### Storage Infrastructure (Phase 5)
- ✅ S3 Bucket for static assets (with public read for /public/*)
- ✅ S3 Bucket for user uploads (private)
- ✅ S3 Bucket for backups (versioned, lifecycle policies)
- ✅ S3 Bucket for logs (lifecycle policies: IA after 30 days, Glacier after 90 days)

### Messaging Infrastructure (Phase 6)
- ✅ SQS Queues: email, image-processing, backup, dead-letter
- ✅ Dead Letter Queue configuration (maxReceiveCount=3)
- ✅ SNS Topics: user-notifications, admin-notifications, system-alerts
- ✅ SNS-to-SQS subscriptions

### Compute Infrastructure (Phase 7)
- ✅ EC2 Instance (t3.micro, Amazon Linux 2023)
- ✅ Elastic IP
- ✅ IAM Role with permissions for S3, SQS, SNS, CloudWatch
- ✅ UserData script installing Docker, Git, PostgreSQL client, Redis CLI, CloudWatch Agent
- ✅ Environment file with all resource endpoints

### Domain & SSL (Phase 9)
- ✅ Route 53 Hosted Zone
- ✅ DNS A records (root and www)
- ✅ ACM SSL Certificate (wildcard + root domain)
- ✅ Automatic DNS validation

### Monitoring (Phase 10)
- ✅ CloudWatch Alarms (EC2 CPU, RDS CPU, RDS Storage, RDS Connections, Redis Memory)
- ✅ CloudWatch Log Group for application logs
- ✅ SNS notifications for alarms

## 🚀 Quick Start

### 1. Prerequisites

Before deploying, ensure you have:
- AWS CLI v2 installed and configured
- AWS account with appropriate permissions
- EC2 Key Pair created (default name: `nastto-key`)
- Domain purchased (nashtto.com)

### 2. Configure Parameters

Edit `parameters.json` and update:
```json
{
  "ParameterKey": "SSHAccessIP",
  "ParameterValue": "YOUR_PUBLIC_IP/32"
},
{
  "ParameterKey": "DBMasterPassword",
  "ParameterValue": "YOUR_STRONG_PASSWORD"
}
```

**Find your public IP:**
```bash
curl ifconfig.me
```

### 3. Validate Template

```bash
aws cloudformation validate-template \
  --template-body file://infrastructure-stack.yaml
```

### 4. Deploy Stack

```bash
aws cloudformation create-stack \
  --stack-name nashtto-infrastructure \
  --template-body file://infrastructure-stack.yaml \
  --parameters file://parameters.json \
  --capabilities CAPABILITY_NAMED_IAM \
  --region us-east-1
```

### 5. Monitor Deployment

```bash
# Watch stack creation
aws cloudformation wait stack-create-complete \
  --stack-name nashtto-infrastructure \
  --region us-east-1

# Or check status
aws cloudformation describe-stacks \
  --stack-name nashtto-infrastructure \
  --query 'Stacks[0].StackStatus'
```

**Expected Duration:** 25-60 minutes

### 6. Get Stack Outputs

```bash
aws cloudformation describe-stacks \
  --stack-name nashtto-infrastructure \
  --query 'Stacks[0].Outputs' \
  --output table
```

## 📖 Detailed Documentation

For complete step-by-step instructions:
- **Deployment:** See `../docs/deployment-instructions.md`
- **Cleanup:** See `../docs/cleanup-instructions.md`

## 🔧 Post-Deployment Tasks

### 1. Update Domain Nameservers

Get Route 53 nameservers:
```bash
aws cloudformation describe-stacks \
  --stack-name nashtto-infrastructure \
  --query 'Stacks[0].Outputs[?OutputKey==`NameServers`].OutputValue' \
  --output text
```

Update nameservers in GoDaddy:
1. Login to GoDaddy
2. Go to DNS Management for nashtto.com
3. Change nameservers to the 4 Route 53 nameservers
4. Wait 24-48 hours for DNS propagation

### 2. SSH into EC2 Instance

```bash
# Get SSH command from outputs
aws cloudformation describe-stacks \
  --stack-name nashtto-infrastructure \
  --query 'Stacks[0].Outputs[?OutputKey==`SSHCommand`].OutputValue' \
  --output text
```

### 3. Initialize Database (Optional)

```bash
# On EC2 instance
cd /opt/nashtto
# Copy init-database.sh from infrastructure/scripts/
./init-database.sh
```

### 4. Configure SQS/SNS (Optional)

```bash
# On EC2 instance
cd /opt/nashtto
# Copy init-sqs-sns.sh from infrastructure/scripts/
./init-sqs-sns.sh
```

## 🗑️ Cleanup / Teardown

### Quick Cleanup

```bash
# 1. Empty S3 buckets
cd ../scripts
./empty-s3-buckets.sh nashtto-infrastructure

# 2. Delete stack
aws cloudformation delete-stack \
  --stack-name nashtto-infrastructure \
  --region us-east-1

# 3. Wait for deletion
aws cloudformation wait stack-delete-complete \
  --stack-name nashtto-infrastructure \
  --region us-east-1
```

For detailed cleanup instructions: `../docs/cleanup-instructions.md`

## ⚙️ Customization

### Change Instance Types

Edit `parameters.json`:
```json
{
  "ParameterKey": "EC2InstanceType",
  "ParameterValue": "t3.small"
},
{
  "ParameterKey": "RDSInstanceType",
  "ParameterValue": "db.t3.small"
}
```

### Add Email Alerts

Edit `parameters.json`:
```json
{
  "ParameterKey": "AlarmEmail",
  "ParameterValue": "your-email@example.com"
}
```

You'll receive CloudWatch alarm notifications at this email.

### Modify VPC CIDR

Edit `infrastructure-stack.yaml` line ~70:
```yaml
VPC:
  Type: AWS::EC2::VPC
  Properties:
    CidrBlock: 10.0.0.0/16  # Change this
```

## 🔄 Update Stack

After modifying the template or parameters:

```bash
aws cloudformation update-stack \
  --stack-name nashtto-infrastructure \
  --template-body file://infrastructure-stack.yaml \
  --parameters file://parameters.json \
  --capabilities CAPABILITY_NAMED_IAM
```

## 📊 Cost Estimation

**Estimated Monthly Costs:**

| Service | Instance Type | Estimated Cost |
|---------|--------------|----------------|
| EC2 | t3.micro | $0-7 (free tier) |
| RDS PostgreSQL | db.t3.micro | $0-15 (free tier) |
| ElastiCache Redis | cache.t3.micro | ~$12 |
| NAT Gateway | - | ~$32 |
| S3 Storage | - | $0-2 |
| Data Transfer | - | $0-9 |
| Route 53 | Hosted Zone | $0.50 |

**Total: ~$51-77/month**

*Lower if within AWS Free Tier (first 12 months)*

### Cost Optimization Tips:
1. Use reserved instances (up to 60% savings)
2. Remove NAT Gateway if not needed (saves $32/month)
3. Use spot instances for non-critical workloads
4. Enable S3 Intelligent-Tiering
5. Set up CloudWatch billing alarms

## 🔐 Security Best Practices

### 1. Restrict SSH Access

Update `SSHAccessIP` in `parameters.json` to your specific IP:
```json
{
  "ParameterKey": "SSHAccessIP",
  "ParameterValue": "203.0.113.10/32"
}
```

### 2. Use Strong Database Password

Never use default passwords. Generate strong password:
```bash
openssl rand -base64 32
```

### 3. Enable MFA for IAM Users

Already covered in Phase 1 of deployment guide.

### 4. Store Secrets in AWS Secrets Manager

```bash
# Store database password
aws secretsmanager create-secret \
  --name nashtto/db/password \
  --secret-string "your-secure-password"

# Update application to retrieve from Secrets Manager
```

### 5. Enable CloudTrail

Track API calls for security auditing:
```bash
aws cloudtrail create-trail \
  --name nashtto-trail \
  --s3-bucket-name nashtto-logs-ACCOUNT_ID
```

## 🐛 Troubleshooting

### Stack Creation Failed

Check events for errors:
```bash
aws cloudformation describe-stack-events \
  --stack-name nashtto-infrastructure \
  --query 'StackEvents[?ResourceStatus==`CREATE_FAILED`]'
```

### Common Issues:

1. **Key pair doesn't exist**
   - Create it first: `aws ec2 create-key-pair --key-name nastto-key`

2. **Invalid CIDR format**
   - Use `x.x.x.x/32` format for single IP

3. **RDS password too weak**
   - Min 8 characters, alphanumeric only

4. **Insufficient permissions**
   - Ensure IAM user has `AdministratorAccess`

### Cannot SSH to EC2

1. Check security group allows your IP
2. Verify key permissions: `chmod 400 nastto-key.pem`
3. Use correct username: `ec2-user`

### SSL Certificate Pending Validation

1. Ensure nameservers updated in GoDaddy
2. Wait for DNS propagation (24-48 hours)
3. Check: `dig nashtto.com NS`

## 📚 Additional Resources

- [AWS CloudFormation Documentation](https://docs.aws.amazon.com/cloudformation/)
- [AWS Well-Architected Framework](https://aws.amazon.com/architecture/well-architected/)
- [AWS Free Tier Details](https://aws.amazon.com/free/)
- [AWS Pricing Calculator](https://calculator.aws/)

## 📝 Template Features

### Best Practices Implemented:
- ✅ Multi-AZ architecture for high availability
- ✅ Private subnets for databases and caches
- ✅ Security groups with least privilege
- ✅ Encrypted storage (RDS, EBS)
- ✅ Automated backups
- ✅ CloudWatch monitoring and alarms
- ✅ IAM roles with minimal required permissions
- ✅ Resource tagging for cost allocation
- ✅ DeletionPolicy for critical resources
- ✅ Comprehensive outputs for easy integration

### Production-Ready Features:
- 🔒 SSL/TLS certificates
- 📊 Monitoring and alerting
- 💾 Automated backups
- 🔄 Message queuing (SQS)
- 📧 Notification system (SNS)
- 🗄️ Object storage (S3)
- 🚀 CDN-ready static assets
- ⚡ Redis caching
- 🔐 IAM security

## 🤝 Contributing

To customize this template:

1. Edit `infrastructure-stack.yaml`
2. Validate: `aws cloudformation validate-template --template-body file://infrastructure-stack.yaml`
3. Test in dev environment first
4. Update documentation
5. Version control your changes

## 📄 License

This infrastructure template is part of the Nashtto food delivery application.

## 📞 Support

For issues or questions:
1. Check troubleshooting section above
2. Review AWS CloudFormation documentation
3. Check CloudFormation Events tab in AWS Console
4. Contact AWS Support (if you have a support plan)

---

**Version:** 1.0  
**Last Updated:** November 2025  
**Stack Name:** nashtto-infrastructure  
**Region:** us-east-1 (configurable)


