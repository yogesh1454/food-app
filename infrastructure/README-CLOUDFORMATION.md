# AWS CloudFormation Infrastructure Automation

## 🚀 Quick Start

This directory contains everything needed to deploy your complete AWS infrastructure with a single CloudFormation command.

### Deploy in 3 Steps:

```bash
# 1. Configure parameters
cd cloudformation
vim parameters.json  # Update SSHAccessIP and DBMasterPassword

# 2. Deploy stack
aws cloudformation create-stack \
  --stack-name nashtto-infrastructure \
  --template-body file://infrastructure-stack.yaml \
  --parameters file://parameters.json \
  --capabilities CAPABILITY_NAMED_IAM \
  --region us-east-1

# 3. Wait for completion (25-60 minutes)
aws cloudformation wait stack-create-complete \
  --stack-name nashtto-infrastructure
```

## 📁 What's Inside

```
infrastructure/
├── cloudformation/
│   ├── infrastructure-stack.yaml        # Main CloudFormation template (38KB)
│   ├── parameters.json                   # Configuration parameters
│   ├── environment-variables-template.txt # Env vars template
│   └── README.md                         # Detailed CloudFormation docs
│
├── docs/
│   ├── deployment-instructions.md        # Step-by-step deployment (11KB)
│   └── cleanup-instructions.md           # Teardown procedures (14KB)
│
├── scripts/
│   ├── empty-s3-buckets.sh              # S3 cleanup automation (6KB)
│   ├── init-database.sh                 # DB initialization (12KB)
│   └── init-sqs-sns.sh                  # Queue/topic setup (15KB)
│
└── AWS-CLOUDFORMATION-AUTOMATION-SUMMARY.md  # Complete overview
```

## 📖 Documentation

### Start Here:
1. **First Time?** → Read `docs/deployment-instructions.md`
2. **Quick Reference** → See `cloudformation/README.md`
3. **Cleanup Guide** → Read `docs/cleanup-instructions.md`
4. **Overview** → See `AWS-CLOUDFORMATION-AUTOMATION-SUMMARY.md`

## ✨ What Gets Deployed

One CloudFormation stack creates:

- ✅ **Network:** VPC, Subnets, NAT Gateway, Security Groups
- ✅ **Database:** RDS PostgreSQL (encrypted, automated backups)
- ✅ **Cache:** ElastiCache Redis
- ✅ **Storage:** 4 S3 Buckets (with lifecycle policies)
- ✅ **Messaging:** SQS Queues + SNS Topics
- ✅ **Compute:** EC2 Instance (with Docker, Git, PostgreSQL, Redis CLI)
- ✅ **Domain:** Route 53 Hosted Zone + SSL Certificate
- ✅ **Monitoring:** CloudWatch Alarms + Log Groups

**Total:** 50+ AWS resources fully configured

## 💰 Cost Estimate

~$46-68/month (lower with AWS Free Tier)

## 🔐 Security

- All databases in private subnets
- Encrypted storage (RDS, EBS)
- Security groups with least privilege
- SSL/TLS certificates
- IAM roles with minimal permissions

## 🗑️ Cleanup

```bash
# Empty S3 buckets
cd scripts
./empty-s3-buckets.sh nashtto-infrastructure

# Delete stack
aws cloudformation delete-stack --stack-name nashtto-infrastructure
```

## 📊 Stack Outputs

After deployment, get all resource endpoints:

```bash
aws cloudformation describe-stacks \
  --stack-name nashtto-infrastructure \
  --query 'Stacks[0].Outputs' \
  --output table
```

## 🎯 Next Steps After Deployment

1. **Update Domain Nameservers** in GoDaddy (from stack outputs)
2. **SSH into EC2:** `ssh -i nastto-key.pem ec2-user@<PUBLIC_IP>`
3. **Verify Environment:** `cat /opt/nashtto/.env`
4. **Initialize Database:** Run `scripts/init-database.sh`
5. **Deploy Application:** Clone your code to `/opt/nashtto/`

## 🆘 Need Help?

- **Deployment Issues?** → `docs/deployment-instructions.md` (Troubleshooting section)
- **Template Details?** → `cloudformation/README.md`
- **AWS Docs?** → https://docs.aws.amazon.com/cloudformation/

## ⚡ Key Features

- 🎯 **Single Command Deployment** - One CloudFormation stack
- 🔄 **Fully Automated** - 75-80% of infrastructure automated
- 📚 **Well Documented** - 1,600+ lines of documentation
- 🔒 **Production Ready** - Security best practices
- 💾 **Disaster Recovery** - Automated backups, snapshots
- 📊 **Monitoring** - CloudWatch alarms pre-configured
- 🔧 **Customizable** - Parameterized for flexibility

---

**Stack Name:** `nashtto-infrastructure`  
**Region:** `us-east-1` (configurable)  
**Template Size:** 38KB (1,200+ lines)  
**Resources:** 50+ AWS resources  

Start with: `docs/deployment-instructions.md`


