# AWS Infrastructure Deployment Instructions

## Prerequisites Checklist

Before deploying the CloudFormation stack, ensure you have completed the following:

### 1. AWS Account Setup
- ✅ AWS account created
- ✅ Root account MFA enabled
- ✅ IAM admin user created with MFA
- ✅ Billing alerts configured
- ✅ AWS CLI installed and configured

### 2. Local Setup
- ✅ AWS CLI v2 installed
- ✅ AWS credentials configured (`aws configure`)
- ✅ Verify access: `aws sts get-caller-identity`

### 3. SSH Key Pair
- ✅ EC2 Key Pair created named `nastto-key` (or your custom name)
- ✅ Private key file downloaded and secured (chmod 400)

### 4. Domain Name
- ✅ Domain purchased (nashtto.com from GoDaddy)
- ✅ Access to GoDaddy DNS management console

---

## Deployment Steps

### Step 1: Prepare Parameters

Edit the `parameters.json` file with your specific values:

```json
{
  "ParameterKey": "SSHAccessIP",
  "ParameterValue": "YOUR_PUBLIC_IP/32"
}
```

**Important Parameters to Update:**

1. **SSHAccessIP**: Find your public IP:
   ```bash
   curl ifconfig.me
   ```
   Then use: `YOUR_IP/32` (e.g., `203.0.113.10/32`)

2. **DBMasterPassword**: Change from default to a strong password:
   - Minimum 8 characters
   - Only alphanumeric characters
   - Store securely in a password manager

3. **AlarmEmail** (Optional): Your email for CloudWatch alarms

4. **KeyPairName**: Ensure this matches your EC2 key pair name

### Step 2: Validate the Template

Before deployment, validate the CloudFormation template:

```bash
cd infrastructure/cloudformation

aws cloudformation validate-template \
  --template-body file://infrastructure-stack.yaml
```

Expected output: Template validation successful with parameters list.

### Step 3: Deploy the Stack

Deploy using AWS CLI:

```bash
aws cloudformation create-stack \
  --stack-name nashtto-infrastructure \
  --template-body file://infrastructure-stack.yaml \
  --parameters file://parameters.json \
  --capabilities CAPABILITY_NAMED_IAM \
  --region us-east-1 \
  --tags \
    Key=Project,Value=Nashtto \
    Key=Environment,Value=Production \
    Key=ManagedBy,Value=CloudFormation
```

**Note:** 
- `--capabilities CAPABILITY_NAMED_IAM` is required because the template creates IAM roles
- Change `--region` if deploying to a different region

### Step 4: Monitor Stack Creation

Watch the stack creation progress:

```bash
aws cloudformation describe-stack-events \
  --stack-name nashtto-infrastructure \
  --region us-east-1 \
  --max-items 10
```

Or use the AWS Console:
1. Go to **CloudFormation** service
2. Select **nashtto-infrastructure** stack
3. Click **Events** tab
4. Refresh to see progress

**Expected Duration:**
- VPC and Networking: ~3-5 minutes
- RDS PostgreSQL: ~10-15 minutes
- ElastiCache Redis: ~5-10 minutes
- EC2 Instance: ~3-5 minutes
- SSL Certificate (ACM): ~5-30 minutes (depends on DNS validation)
- **Total: 25-60 minutes**

### Step 5: Wait for Stack Completion

Wait until stack status is `CREATE_COMPLETE`:

```bash
aws cloudformation wait stack-create-complete \
  --stack-name nashtto-infrastructure \
  --region us-east-1
```

This command will block until the stack is fully created.

---

## Post-Deployment Configuration

### Step 6: Retrieve Stack Outputs

Get all stack outputs:

```bash
aws cloudformation describe-stacks \
  --stack-name nashtto-infrastructure \
  --region us-east-1 \
  --query 'Stacks[0].Outputs' \
  --output table
```

**Important Outputs:**
- `NameServers`: Route 53 nameservers (needed for Step 7)
- `EC2PublicIP`: IP address to connect to your server
- `SSHCommand`: Command to SSH into the server
- `RDSEndpoint`: Database endpoint
- `RedisEndpoint`: Redis cache endpoint
- All S3 bucket names, SQS queue URLs, SNS topic ARNs

Save these outputs - you'll need them for application configuration.

### Step 7: Update Domain Nameservers in GoDaddy

**Critical Step:** Update your domain's nameservers to point to AWS Route 53.

1. **Get Route 53 Nameservers:**
   ```bash
   aws cloudformation describe-stacks \
     --stack-name nashtto-infrastructure \
     --query 'Stacks[0].Outputs[?OutputKey==`NameServers`].OutputValue' \
     --output text
   ```

   You'll get something like:
   ```
   ns-123.awsdns-12.com, ns-456.awsdns-45.net, ns-789.awsdns-78.org, ns-012.awsdns-01.co.uk
   ```

2. **Login to GoDaddy:**
   - Go to https://dcc.godaddy.com/manage/nashtto.com/dns
   - Click on **Nameservers** section
   - Choose "Change Nameservers"
   - Select "I'll use my own nameservers"

3. **Enter AWS Nameservers:**
   Enter all 4 nameservers from Route 53 (one per line)

4. **Save Changes**

5. **Wait for DNS Propagation:**
   - Can take 24-48 hours (usually much faster)
   - Check propagation: `dig nashtto.com NS`

### Step 8: Wait for SSL Certificate Validation

The ACM SSL certificate will automatically validate via DNS once nameservers are updated:

```bash
aws acm describe-certificate \
  --certificate-arn $(aws cloudformation describe-stacks \
    --stack-name nashtto-infrastructure \
    --query 'Stacks[0].Outputs[?OutputKey==`SSLCertificateARN`].OutputValue' \
    --output text) \
  --region us-east-1 \
  --query 'Certificate.Status'
```

Wait for status: `ISSUED`

### Step 9: SSH into EC2 Instance

Connect to your EC2 instance:

```bash
# Get the SSH command from outputs
aws cloudformation describe-stacks \
  --stack-name nashtto-infrastructure \
  --query 'Stacks[0].Outputs[?OutputKey==`SSHCommand`].OutputValue' \
  --output text

# Or manually:
ssh -i nastto-key.pem ec2-user@<EC2_PUBLIC_IP>
```

### Step 10: Verify Installed Software

Once connected to EC2, verify installations:

```bash
# Check Docker
docker --version
docker-compose --version

# Check database client
psql --version

# Check Redis CLI
redis-cli --version

# Check Git
git --version

# Check AWS CLI
aws --version

# Read the README
cat ~/README.txt
```

### Step 11: Test Database Connection

Test PostgreSQL connection from EC2:

```bash
# Load environment variables
source /opt/nashtto/.env

# Connect to RDS
psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME

# Enter password when prompted
```

If connection successful, you'll see the PostgreSQL prompt: `nastto_db=>`

### Step 12: Test Redis Connection

Test Redis connection from EC2:

```bash
# Connect to ElastiCache Redis
redis-cli -h $REDIS_HOST -p $REDIS_PORT

# Test with PING command
127.0.0.1:6379> PING
# Should return: PONG
```

### Step 13: Initialize Database (Optional)

If you have database schema or seed data:

```bash
cd /opt/nashtto
# Copy your init-database.sh script here
# Or use the placeholder script from infrastructure/scripts/
./init-database.sh
```

---

## Verification Checklist

Before deploying your application, verify:

- ✅ Stack status: `CREATE_COMPLETE`
- ✅ EC2 instance running and accessible via SSH
- ✅ Database connection successful from EC2
- ✅ Redis connection successful from EC2
- ✅ All S3 buckets created
- ✅ All SQS queues created
- ✅ All SNS topics created
- ✅ Domain nameservers updated in GoDaddy
- ✅ DNS resolves: `dig nashtto.com` returns EC2 IP
- ✅ SSL certificate status: `ISSUED`
- ✅ CloudWatch alarms active
- ✅ Environment file exists: `/opt/nashtto/.env`

---

## Next Steps

Your infrastructure is now ready! You can now:

1. **Deploy Your Application:**
   - Clone your application code to `/opt/nashtto/`
   - Configure docker-compose.yml
   - Start your application

2. **Configure Nginx/Load Balancer:**
   - Set up reverse proxy
   - Configure SSL termination
   - Add your SSL certificate from ACM

3. **Set Up CI/CD:**
   - GitHub Actions
   - AWS CodePipeline
   - Jenkins

4. **Initialize Data:**
   - Run database migrations
   - Load seed data
   - Configure SQS message schemas

---

## Common Issues & Troubleshooting

### Issue: Stack Creation Failed

**Check Events:**
```bash
aws cloudformation describe-stack-events \
  --stack-name nashtto-infrastructure \
  --query 'StackEvents[?ResourceStatus==`CREATE_FAILED`]'
```

**Common Causes:**
1. Key pair doesn't exist → Create it first
2. Invalid CIDR format → Use `x.x.x.x/32` format
3. RDS password too weak → Min 8 alphanumeric characters
4. Insufficient permissions → Ensure IAM user has admin access

### Issue: Cannot SSH to EC2

**Troubleshooting:**
1. Check security group allows your IP
2. Verify key pair permissions: `chmod 400 nastto-key.pem`
3. Use correct username: `ec2-user` for Amazon Linux
4. Verify instance is running: `aws ec2 describe-instances`

### Issue: SSL Certificate Stuck in "Pending Validation"

**Solution:**
1. Verify nameservers updated in GoDaddy
2. Wait for DNS propagation (24-48 hours)
3. Check DNS propagation: `dig nashtto.com NS`
4. ACM will auto-validate once DNS is correct

### Issue: Cannot Connect to RDS from EC2

**Troubleshooting:**
1. Verify security group allows traffic from Web SG
2. Check RDS is in `available` state
3. Verify endpoint is correct
4. Ensure EC2 and RDS are in same VPC

---

## Useful Commands

### Get Stack Status
```bash
aws cloudformation describe-stacks \
  --stack-name nashtto-infrastructure \
  --query 'Stacks[0].StackStatus'
```

### Get All Outputs as JSON
```bash
aws cloudformation describe-stacks \
  --stack-name nashtto-infrastructure \
  --query 'Stacks[0].Outputs' \
  --output json > stack-outputs.json
```

### Update Stack (after template changes)
```bash
aws cloudformation update-stack \
  --stack-name nashtto-infrastructure \
  --template-body file://infrastructure-stack.yaml \
  --parameters file://parameters.json \
  --capabilities CAPABILITY_NAMED_IAM
```

### Get Stack Resources
```bash
aws cloudformation list-stack-resources \
  --stack-name nashtto-infrastructure
```

---

## Cost Estimation

**Monthly AWS Costs (Free Tier Eligible):**

| Service | Instance Type | Free Tier | Estimated Cost |
|---------|--------------|-----------|----------------|
| EC2 | t3.micro | 750 hrs/month | $0-7 |
| RDS PostgreSQL | db.t3.micro | 750 hrs/month | $0-15 |
| ElastiCache Redis | cache.t3.micro | - | ~$12 |
| NAT Gateway | - | - | ~$32 |
| S3 Storage | - | 5GB | $0-2 |
| Data Transfer | - | 100GB out | $0-9 |
| Route 53 | Hosted Zone | - | $0.50 |

**Total Estimated: $51-77/month** (lower if within free tier)

**Cost Optimization Tips:**
- Use reserved instances for long-term (up to 60% savings)
- Remove NAT Gateway if not needed (saves $32/month)
- Use S3 lifecycle policies (already configured)
- Enable RDS storage autoscaling

---

## Support & Resources

- AWS CloudFormation Documentation: https://docs.aws.amazon.com/cloudformation/
- AWS Well-Architected Framework: https://aws.amazon.com/architecture/well-architected/
- AWS Free Tier: https://aws.amazon.com/free/

---

**Deployment Guide Version:** 1.0  
**Last Updated:** November 2025  
**Infrastructure Stack:** nashtto-infrastructure


