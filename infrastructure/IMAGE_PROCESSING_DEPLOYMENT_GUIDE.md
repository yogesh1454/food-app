# Image Processing Infrastructure - Deployment Guide

## Overview
This guide documents the complete setup for the Nashtto image processing pipeline using AWS services.

## Architecture

```
User Upload → Backend API → S3 (originals/)
                              ↓
                         S3 Event Notification
                              ↓
                         SQS Queue (image-processing-queue)
                              ↓
                         Lambda Function (image-processor)
                              ├→ Downloads original from S3
                              ├→ Generates variants (WebP + JPEG)
                              │   • thumbnail (150x150)
                              │   • small (300x300)
                              │   • medium (600x600)
                              │   • large (1200x1200) - skipped for logos
                              ├→ Uploads to S3 (processed/)
                              └→ Publishes result to SQS (image-processing-results)
                                      ↓
                                 Backend SQS Listener
                                      ↓
                                 Updates Database (Vendor/Branch/MenuItem)
                                      ↓
                                 Triggers Search Index Sync
```

## Prerequisites

1. **Docker Desktop** - Required for building Lambda with Sharp (native dependencies)
   - Install from: https://www.docker.com/products/docker-desktop
   - Must be running before deployment

2. **AWS CLI** - Configured with credentials
   ```bash
   aws configure
   ```

3. **AWS Account** - Free tier eligible

## Infrastructure Components

### 1. S3 Buckets
- **Media Bucket**: `nashtto-media-prod-{AccountId}`
  - `originals/` - Raw uploaded images
  - `processed/` - Generated variants
- **Documents Bucket**: `nashtto-documents-prod-{AccountId}`

### 2. CloudFront CDN
- Serves processed images with caching
- Cache-Control: 7 days
- Automatic HTTPS

### 3. SQS Queues
- **image-processing-queue**: S3 → Lambda trigger
- **image-processing-results**: Lambda → Backend results
- **DLQs**: For both queues (14-day retention)

### 4. Lambda Function
- **Runtime**: Node.js 20.x
- **Memory**: 512 MB (Free Tier optimized)
- **Timeout**: 10 seconds
- **Trigger**: SQS (batch size: 5)
- **Dependencies**: Sharp (Docker-compiled for Linux x86_64)

### 5. CloudWatch Alarms
- Lambda Free Tier usage (80% threshold)
- Lambda errors
- DLQ messages
- SQS queue depth
- S3 storage size

## Deployment Steps

### Step 1: Deploy Infrastructure

```bash
cd infrastructure
./DEPLOY-IMAGE-PROCESSING.sh
```

This script will:
1. ✅ Validate CloudFormation template
2. ✅ Deploy/update stack (S3, CloudFront, SQS, IAM, Alarms)
3. ✅ Build Lambda with Docker (Sharp for Linux x86_64)
4. ✅ Deploy Lambda function code
5. ✅ Configure SQS event source mapping
6. ✅ Update `application.yml` and `application-prod.yml`
7. ✅ Subscribe email to SNS alerts

**Duration**: ~5-10 minutes (first time includes Docker image pull)

### Step 2: Confirm SNS Subscription

Check your email (`yogesh.bardia@gmail.com`) and confirm the SNS subscription to receive CloudWatch alerts.

### Step 3: Verify Deployment

```bash
# Check CloudFormation stack
aws cloudformation describe-stacks \
  --stack-name nashtto-image-processing \
  --region us-east-1

# Check Lambda function
aws lambda get-function \
  --function-name nashtto-image-processor-prod \
  --region us-east-1

# Check SQS queues
aws sqs list-queues --region us-east-1 | grep nashtto-image
```

## Testing

### Manual Test

```bash
# Upload test image
aws s3 cp test-image.jpg \
  s3://nashtto-media-prod-{AccountId}/originals/vendors/123/logo_original.jpg

# Wait 10-15 seconds, then check Lambda logs
aws logs tail /aws/lambda/nashtto-image-processor-prod \
  --since 2m \
  --region us-east-1 \
  --follow

# Verify processed images
aws s3 ls s3://nashtto-media-prod-{AccountId}/processed/vendors/123/

# Test CDN URL
curl -I https://{CloudFrontDomain}/vendors/123/logo_thumbnail.webp
```

### Via Backend API

```bash
# Start backend
cd tea-snacks-delivery-aggregator
./gradlew :order-catalog-service:bootRun

# Upload via Swagger UI
open http://54.87.117.181:8080/swagger-ui.html
# POST /api/v1/vendors/{id}/images
```

## Key Naming Conventions

### S3 Key Format
```
originals/{entityType}/{entityId}/{imageType}_original.{ext}
processed/{entityType}/{entityId}/{imageType}_{size}.{format}
```

**Examples**:
- `originals/vendors/123/logo_original.jpg`
- `processed/vendors/123/logo_thumbnail.webp`
- `originals/menu-items/456/photo_original.png`
- `processed/menu-items/456/photo_large.jpg`

### Entity Types
- `vendors` - Vendor logos
- `vendor-branches` - Branch images
- `menu-items` - Food item photos

### Image Types
- `logo` - Vendor/branch logos (no large variant)
- `photo` - Menu item photos (all variants)
- `banner` - Promotional banners (all variants)

### Sizes
- `thumbnail` - 150x150 (cover fit)
- `small` - 300x300 (inside fit)
- `medium` - 600x600 (inside fit)
- `large` - 1200x1200 (inside fit, skipped for logos)

## Troubleshooting

### Lambda Fails with Sharp Error

**Problem**: `Could not load the "sharp" module using the linux-x64 runtime`

**Solution**: Ensure Docker is running and redeploy:
```bash
# Check Docker
docker info

# If not running, start Docker Desktop, then:
cd infrastructure
./DEPLOY-LAMBDA-IMAGE-PROCESSOR.sh
```

### S3 Event Not Triggering Lambda

**Problem**: Image uploaded but Lambda not invoked

**Checks**:
1. Verify S3 event notification is configured:
   ```bash
   aws s3api get-bucket-notification-configuration \
     --bucket nashtto-media-prod-{AccountId}
   ```

2. Check SQS queue for messages:
   ```bash
   aws sqs get-queue-attributes \
     --queue-url https://sqs.us-east-1.amazonaws.com/{AccountId}/nashtto-image-processing-queue-prod \
     --attribute-names ApproximateNumberOfMessages
   ```

3. Verify Lambda event source mapping:
   ```bash
   aws lambda list-event-source-mappings \
     --function-name nashtto-image-processor-prod
   ```

### Images Not Accessible via CDN

**Problem**: 403 Forbidden from CloudFront

**Solution**: CloudFront needs time to propagate (5-10 minutes). Check:
```bash
# Test S3 direct access first
aws s3 presign s3://nashtto-media-prod-{AccountId}/processed/vendors/123/logo_thumbnail.webp

# Wait for CloudFront propagation, then test CDN
curl -I https://{CloudFrontDomain}/vendors/123/logo_thumbnail.webp
```

## Cost Monitoring

### Free Tier Limits
- **Lambda**: 1M requests + 400,000 GB-seconds/month
- **S3**: 5GB storage + 20,000 GET + 2,000 PUT requests
- **CloudFront**: 1TB transfer (12 months)
- **SQS**: 1M requests
- **CloudWatch**: 10 alarms

### Current Usage (512MB Lambda)
- **Per invocation**: ~800ms = 0.4 GB-seconds
- **Free tier allows**: ~1M images/month
- **Estimated cost**: $0/month (within free tier)

### Alerts Configured
- Lambda duration approaching free tier limit (80%)
- DLQ messages (any failures)
- SQS queue depth >100 (backlog)
- S3 storage >4GB (approaching limit)

## Maintenance

### Update Lambda Code

```bash
cd infrastructure
./DEPLOY-LAMBDA-IMAGE-PROCESSOR.sh
```

### Update Infrastructure

```bash
cd infrastructure
./DEPLOY-IMAGE-PROCESSING.sh
```

### View Logs

```bash
# Lambda logs
aws logs tail /aws/lambda/nashtto-image-processor-prod --follow

# CloudFormation events
aws cloudformation describe-stack-events \
  --stack-name nashtto-image-processing \
  --max-items 20
```

### Purge Failed Messages

```bash
# Purge DLQ
aws sqs purge-queue \
  --queue-url https://sqs.us-east-1.amazonaws.com/{AccountId}/nashtto-image-processing-dlq-prod
```

## Production Checklist

- [ ] Docker Desktop installed and running
- [ ] AWS CLI configured
- [ ] CloudFormation stack deployed successfully
- [ ] Lambda function deployed with correct Sharp binaries
- [ ] SQS event source mapping enabled
- [ ] SNS email subscription confirmed
- [ ] Test image processed successfully
- [ ] CDN URLs accessible
- [ ] Backend SQS listener configured
- [ ] CloudWatch alarms active
- [ ] Documentation reviewed

## Support

For issues or questions:
1. Check CloudWatch Logs
2. Review CloudFormation events
3. Verify all prerequisites
4. Consult this guide's troubleshooting section
