# AWS Infrastructure Cleanup Instructions

## ⚠️ WARNING: DATA LOSS

**IMPORTANT:** Deleting the CloudFormation stack will **permanently destroy**:
- All data in RDS PostgreSQL database
- All cached data in Redis
- All files in S3 buckets (if not emptied first)
- All EC2 configurations and logs
- All CloudWatch logs and metrics history

**Before proceeding:**
1. ✅ Backup all critical data
2. ✅ Export RDS database
3. ✅ Download important S3 files
4. ✅ Save application configurations
5. ✅ Notify team members
6. ✅ Verify you have backups

---

## Pre-Cleanup: Create Backups

### 1. Backup RDS Database

SSH into EC2 instance and create a database dump:

```bash
# Connect to EC2
ssh -i nastto-key.pem ec2-user@<EC2_PUBLIC_IP>

# Load environment variables
source /opt/nashtto/.env

# Create backup directory
mkdir -p /tmp/backups

# Dump database
pg_dump -h $DB_HOST -U $DB_USERNAME -d $DB_NAME > /tmp/backups/database_backup_$(date +%Y%m%d).sql

# Compress backup
gzip /tmp/backups/database_backup_*.sql

# Download to local machine (run from your local terminal)
scp -i nastto-key.pem ec2-user@<EC2_PUBLIC_IP>:/tmp/backups/*.sql.gz ./
```

### 2. Backup S3 Buckets

Download important files from S3:

```bash
# Get bucket names
aws cloudformation describe-stacks \
  --stack-name nashtto-infrastructure \
  --query 'Stacks[0].Outputs[?contains(OutputKey, `Bucket`)].{Name:OutputKey,Value:OutputValue}' \
  --output table

# Sync buckets to local storage
aws s3 sync s3://nashtto-static-assets-ACCOUNT_ID ./backups/static-assets/
aws s3 sync s3://nashtto-user-uploads-ACCOUNT_ID ./backups/user-uploads/
aws s3 sync s3://nashtto-backups-ACCOUNT_ID ./backups/backups/
```

### 3. Export CloudWatch Logs (Optional)

```bash
# Export logs to S3 (they will be in the logs bucket)
aws logs create-export-task \
  --log-group-name /aws/ec2/nashtto-app \
  --from $(date -d '30 days ago' +%s)000 \
  --to $(date +%s)000 \
  --destination nashtto-logs-ACCOUNT_ID \
  --destination-prefix cloudwatch-exports
```

### 4. Save Stack Outputs

Save all stack outputs for reference:

```bash
aws cloudformation describe-stacks \
  --stack-name nashtto-infrastructure \
  --query 'Stacks[0].Outputs' \
  --output json > nashtto-stack-outputs-backup.json
```

---

## Cleanup Process

### Step 1: Empty S3 Buckets

**CloudFormation cannot delete non-empty S3 buckets.** You must empty them first.

#### Option A: Use Provided Script (Recommended)

```bash
cd infrastructure/scripts
chmod +x empty-s3-buckets.sh
./empty-s3-buckets.sh nashtto-infrastructure
```

#### Option B: Manual Deletion via AWS CLI

```bash
# Get bucket names from stack outputs
STATIC_BUCKET=$(aws cloudformation describe-stacks \
  --stack-name nashtto-infrastructure \
  --query 'Stacks[0].Outputs[?OutputKey==`StaticAssetsBucketName`].OutputValue' \
  --output text)

UPLOADS_BUCKET=$(aws cloudformation describe-stacks \
  --stack-name nashtto-infrastructure \
  --query 'Stacks[0].Outputs[?OutputKey==`UserUploadsBucketName`].OutputValue' \
  --output text)

BACKUPS_BUCKET=$(aws cloudformation describe-stacks \
  --stack-name nashtto-infrastructure \
  --query 'Stacks[0].Outputs[?OutputKey==`BackupsBucketName`].OutputValue' \
  --output text)

LOGS_BUCKET=$(aws cloudformation describe-stacks \
  --stack-name nashtto-infrastructure \
  --query 'Stacks[0].Outputs[?OutputKey==`LogsBucketName`].OutputValue' \
  --output text)

# Empty each bucket (handles versioned objects)
aws s3 rm s3://$STATIC_BUCKET --recursive
aws s3 rm s3://$UPLOADS_BUCKET --recursive
aws s3 rm s3://$BACKUPS_BUCKET --recursive
aws s3 rm s3://$LOGS_BUCKET --recursive

# If versioning is enabled, delete all versions
aws s3api delete-objects \
  --bucket $BACKUPS_BUCKET \
  --delete "$(aws s3api list-object-versions \
    --bucket $BACKUPS_BUCKET \
    --query '{Objects: Versions[].{Key:Key,VersionId:VersionId}}' \
    --output json)"

# Delete all delete markers
aws s3api delete-objects \
  --bucket $BACKUPS_BUCKET \
  --delete "$(aws s3api list-object-versions \
    --bucket $BACKUPS_BUCKET \
    --query '{Objects: DeleteMarkers[].{Key:Key,VersionId:VersionId}}' \
    --output json)"
```

#### Option C: Manual Deletion via AWS Console

1. Go to **S3** in AWS Console
2. For each bucket:
   - Select the bucket
   - Click **Empty**
   - Type "permanently delete" to confirm
   - Click **Empty**

### Step 2: Disable RDS Deletion Protection (if enabled)

Check if deletion protection is enabled:

```bash
aws rds describe-db-instances \
  --db-instance-identifier nashtto-postgres \
  --query 'DBInstances[0].DeletionProtection'
```

If `true`, disable it:

```bash
aws rds modify-db-instance \
  --db-instance-identifier nashtto-postgres \
  --no-deletion-protection \
  --apply-immediately
```

### Step 3: Delete CloudFormation Stack

Now delete the stack:

```bash
aws cloudformation delete-stack \
  --stack-name nashtto-infrastructure \
  --region us-east-1
```

### Step 4: Wait for Deletion to Complete

Monitor deletion progress:

```bash
aws cloudformation wait stack-delete-complete \
  --stack-name nashtto-infrastructure \
  --region us-east-1
```

Or watch events:

```bash
aws cloudformation describe-stack-events \
  --stack-name nashtto-infrastructure \
  --max-items 20
```

**Typical deletion time: 10-20 minutes**

---

## Step 5: Verify Complete Deletion

### Check Stack Status

```bash
aws cloudformation describe-stacks \
  --stack-name nashtto-infrastructure
```

Expected result: Stack not found error (good!)

### Verify Resource Deletion

Check individual resources are deleted:

```bash
# Check EC2 instances
aws ec2 describe-instances \
  --filters "Name=tag:ManagedBy,Values=CloudFormation" \
  --query 'Reservations[*].Instances[?State.Name!=`terminated`]'

# Check RDS instances
aws rds describe-db-instances \
  --query 'DBInstances[?DBInstanceIdentifier==`nashtto-postgres`]'

# Check ElastiCache clusters
aws elasticache describe-cache-clusters \
  --query 'CacheClusters[?CacheClusterId==`nashtto-redis`]'

# Check S3 buckets
aws s3 ls | grep nashtto

# Check VPC
aws ec2 describe-vpcs \
  --filters "Name=tag:Name,Values=nashtto-vpc"
```

All commands should return empty results.

---

## Manual Cleanup (If Automated Deletion Fails)

### If Stack Deletion Fails

Sometimes CloudFormation stack deletion can fail. Common reasons:

1. **S3 buckets not empty** → Empty them (Step 1)
2. **ENI still attached** → Manually detach network interfaces
3. **RDS deletion protection** → Disable it (Step 2)
4. **Security groups in use** → Delete dependent resources first

### Check for Stuck Resources

```bash
aws cloudformation describe-stack-resources \
  --stack-name nashtto-infrastructure \
  --query 'StackResources[?ResourceStatus==`DELETE_FAILED`]'
```

### Force Delete VPC (Last Resort)

If VPC won't delete:

```bash
# Get VPC ID
VPC_ID=$(aws ec2 describe-vpcs \
  --filters "Name=tag:Name,Values=nashtto-vpc" \
  --query 'Vpcs[0].VpcId' \
  --output text)

# Delete NAT Gateway (wait 5 minutes after deletion)
aws ec2 describe-nat-gateways \
  --filter "Name=vpc-id,Values=$VPC_ID" \
  --query 'NatGateways[*].NatGatewayId' \
  --output text | xargs -n1 aws ec2 delete-nat-gateway --nat-gateway-id

# Release Elastic IPs
aws ec2 describe-addresses \
  --filters "Name=domain,Values=vpc" \
  --query 'Addresses[*].AllocationId' \
  --output text | xargs -n1 aws ec2 release-address --allocation-id

# Delete Internet Gateway
IGW_ID=$(aws ec2 describe-internet-gateways \
  --filters "Name=attachment.vpc-id,Values=$VPC_ID" \
  --query 'InternetGateways[0].InternetGatewayId' \
  --output text)

aws ec2 detach-internet-gateway --internet-gateway-id $IGW_ID --vpc-id $VPC_ID
aws ec2 delete-internet-gateway --internet-gateway-id $IGW_ID

# Delete subnets
aws ec2 describe-subnets \
  --filters "Name=vpc-id,Values=$VPC_ID" \
  --query 'Subnets[*].SubnetId' \
  --output text | xargs -n1 aws ec2 delete-subnet --subnet-id

# Delete route tables
aws ec2 describe-route-tables \
  --filters "Name=vpc-id,Values=$VPC_ID" \
  --query 'RouteTables[?Associations[0].Main==`false`].RouteTableId' \
  --output text | xargs -n1 aws ec2 delete-route-table --route-table-id

# Delete security groups
aws ec2 describe-security-groups \
  --filters "Name=vpc-id,Values=$VPC_ID" \
  --query 'SecurityGroups[?GroupName!=`default`].GroupId' \
  --output text | xargs -n1 aws ec2 delete-security-group --group-id

# Finally, delete VPC
aws ec2 delete-vpc --vpc-id $VPC_ID
```

---

## Post-Cleanup Tasks

### 1. Remove Route 53 Hosted Zone (Optional)

The hosted zone costs $0.50/month. Delete if not needed:

```bash
# Get Hosted Zone ID
ZONE_ID=$(aws route53 list-hosted-zones \
  --query 'HostedZones[?Name==`nashtto.com.`].Id' \
  --output text | cut -d'/' -f3)

# List all record sets
aws route53 list-resource-record-sets \
  --hosted-zone-id $ZONE_ID

# Delete non-default record sets (NS and SOA cannot be deleted)
# You'll need to manually delete A records, CNAMEs, etc.

# Finally delete the hosted zone
aws route53 delete-hosted-zone --id $ZONE_ID
```

### 2. Revert GoDaddy Nameservers

If you're done with AWS hosting:

1. Login to GoDaddy
2. Go to DNS management for nashtto.com
3. Change nameservers back to GoDaddy default
4. Remove AWS Route 53 nameservers

### 3. Delete IAM Resources (If created manually)

If you created IAM users or roles outside CloudFormation:

```bash
# List IAM roles with "nashtto" in name
aws iam list-roles --query 'Roles[?contains(RoleName, `nashtto`)]'

# Delete IAM role (if any remain)
aws iam delete-role --role-name nashtto-custom-role
```

### 4. Delete CloudWatch Logs (Optional)

CloudWatch log groups might persist:

```bash
# List log groups
aws logs describe-log-groups --log-group-name-prefix /aws/ec2/nashtto

# Delete log group
aws logs delete-log-group --log-group-name /aws/ec2/nashtto-app
```

### 5. Delete ACM Certificate (If it remains)

Usually deleted with stack, but check:

```bash
# List certificates
aws acm list-certificates --query 'CertificateSummaryList[?DomainName==`nashtto.com`]'

# Delete if found
aws acm delete-certificate --certificate-arn <CERTIFICATE_ARN>
```

### 6. Delete SSH Key Pair (Optional)

If you want to remove the EC2 key pair:

```bash
# Delete from AWS
aws ec2 delete-key-pair --key-name nastto-key

# Delete local file
rm -f nastto-key.pem
```

---

## Cost Verification

### Check for Remaining Resources

After cleanup, verify no charges:

```bash
# Check EC2 instances
aws ec2 describe-instances --query 'Reservations[*].Instances[?State.Name!=`terminated`]'

# Check RDS instances
aws rds describe-db-instances

# Check ElastiCache clusters
aws elasticache describe-cache-clusters

# Check NAT Gateways (costly!)
aws ec2 describe-nat-gateways --filter "Name=state,Values=available"

# Check Elastic IPs (charges if not attached)
aws ec2 describe-addresses --query 'Addresses[?AssociationId==null]'

# Check S3 buckets
aws s3 ls

# Check EBS volumes (orphaned volumes)
aws ec2 describe-volumes --filters "Name=status,Values=available"

# Check snapshots
aws ec2 describe-snapshots --owner-ids self
```

### Monitor AWS Billing

1. Go to **AWS Billing Dashboard**
2. Check **Bills** section
3. Verify charges dropping to $0
4. Set up a $1 budget alert to catch any remaining resources

---

## Emergency Stop (Minimize Costs Quickly)

If you need to stop costs immediately without full deletion:

```bash
# Stop EC2 instance (stops instance charges)
aws ec2 stop-instances --instance-ids <INSTANCE_ID>

# Delete NAT Gateway (stops $32/month)
aws ec2 delete-nat-gateway --nat-gateway-id <NAT_GATEWAY_ID>

# Delete RDS instance (saves ~$15/month)
aws rds delete-db-instance \
  --db-instance-identifier nashtto-postgres \
  --skip-final-snapshot

# Delete ElastiCache cluster (saves ~$12/month)
aws elasticache delete-cache-cluster \
  --cache-cluster-id nashtto-redis
```

**Note:** This keeps VPC, S3, and other cheap resources but stops major costs.

---

## Cleanup Checklist

After completing cleanup:

- ✅ CloudFormation stack deleted
- ✅ All S3 buckets emptied and deleted
- ✅ RDS database deleted (with final snapshot if needed)
- ✅ ElastiCache cluster deleted
- ✅ EC2 instances terminated
- ✅ Elastic IPs released
- ✅ NAT Gateway deleted
- ✅ VPC deleted
- ✅ Security groups deleted
- ✅ Route 53 hosted zone deleted (optional)
- ✅ ACM certificate deleted
- ✅ CloudWatch logs deleted (optional)
- ✅ IAM roles deleted
- ✅ SSH key pair deleted (optional)
- ✅ GoDaddy nameservers reverted (optional)
- ✅ Verified $0 AWS billing

---

## Frequently Asked Questions

### Q: Can I recreate the infrastructure later?

**A:** Yes! Just run the deployment again:
```bash
aws cloudformation create-stack \
  --stack-name nashtto-infrastructure \
  --template-body file://infrastructure-stack.yaml \
  --parameters file://parameters.json \
  --capabilities CAPABILITY_NAMED_IAM
```

### Q: Will I lose my data?

**A:** Yes, unless you:
1. Take an RDS snapshot before deletion
2. Backup S3 buckets locally
3. Export CloudWatch logs

RDS creates an automatic final snapshot on deletion (configured in template).

### Q: How do I restore from RDS snapshot?

```bash
# List snapshots
aws rds describe-db-snapshots --query 'DBSnapshots[?contains(DBSnapshotIdentifier, `nashtto`)]'

# Restore from snapshot (when recreating stack)
# Modify the CloudFormation template to use snapshot instead of creating new DB
```

### Q: What if I only want to delete some resources?

**A:** You can't selectively delete from CloudFormation stack. Options:
1. Delete entire stack and recreate without unwanted resources
2. Manually delete resources and remove from template
3. Use nested stacks for modular infrastructure

### Q: How long until AWS stops billing me?

**A:** Most resources stop billing immediately upon deletion. However:
- NAT Gateway: Charges per hour (prorated)
- EC2: Charges stop when terminated
- RDS: Charges stop when deleted
- S3: Charges for storage used (prorate monthly)
- Data transfer charges may appear for 1-2 days after

Check billing dashboard 2-3 days after cleanup.

---

## Support

If you encounter issues during cleanup:

1. Check CloudFormation Events tab for error details
2. Review AWS Service Health Dashboard
3. Contact AWS Support (if you have a support plan)
4. Check AWS Forums: https://forums.aws.amazon.com

---

**Cleanup Guide Version:** 1.0  
**Last Updated:** November 2025  
**Infrastructure Stack:** nashtto-infrastructure


