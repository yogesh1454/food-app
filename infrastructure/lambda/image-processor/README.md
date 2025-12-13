# Nashtto Image Processor Lambda

AWS Lambda function for processing uploaded images. Generates multiple size variants in WebP and JPEG formats.

## Overview

This Lambda function:
1. Listens to SQS queue for S3 upload events
2. Downloads original image from S3 `originals/` folder
3. Generates multiple size variants using Sharp
4. Uploads processed images to S3 `processed/` folder
5. Sends callback to backend API with CDN URLs

## Image Variants Generated

| Size | Dimensions | Use Case |
|------|------------|----------|
| thumbnail | 150x150 (cover) | List view, grid |
| small | 300x300 (contain) | Search results |
| medium | 600x600 (contain) | Detail view |
| large | 1200x1200 (contain) | Full screen (not for logos) |

Both WebP and JPEG formats are generated for browser compatibility.

## Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `MEDIA_BUCKET` | S3 bucket for images | `nashtto-media-prod` |
| `CLOUDFRONT_DOMAIN` | CloudFront distribution domain | `d123abc.cloudfront.net` |
| `CALLBACK_URL` | Backend API callback URL | `https://api.nashtto.com/api/internal/callbacks/image-processing` |
| `AWS_REGION` | AWS region | `us-east-1` |

## S3 Folder Structure

```
nashtto-media-prod/
├── originals/              # Raw uploads (Lambda trigger source)
│   ├── vendors/{id}/
│   │   ├── logo_original.jpg
│   │   └── cover_original.png
│   ├── branches/{id}/
│   │   ├── storefront_original.jpg
│   │   └── interior_original.webp
│   └── menu-items/{id}/
│       ├── primary_original.jpg
│       └── gallery_1_original.png
├── processed/              # Lambda output (CDN served)
│   ├── vendors/{id}/
│   │   ├── logo_thumbnail.webp
│   │   ├── logo_thumbnail.jpg
│   │   ├── logo_small.webp
│   │   └── ...
│   ├── branches/{id}/
│   │   └── ...
│   └── menu-items/{id}/
│       └── ...
└── temp/                   # Temporary staging (TTL: 24h)
```

## Deployment

### Prerequisites
- Node.js 20.x
- AWS CLI configured
- Lambda function created with proper IAM role

### Build & Deploy

```bash
# Install dependencies
npm install --production

# Create deployment package
zip -r function.zip .

# Deploy to Lambda
aws lambda update-function-code \
  --function-name nashtto-image-processor \
  --zip-file fileb://function.zip \
  --region us-east-1
```

### Lambda Configuration

| Setting | Value |
|---------|-------|
| Runtime | Node.js 20.x |
| Memory | 512 MB |
| Timeout | 30 seconds |
| Trigger | SQS (nashtto-image-processing-queue-prod) |

## IAM Policy Required

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject"
      ],
      "Resource": [
        "arn:aws:s3:::nashtto-media-prod/*"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "sqs:ReceiveMessage",
        "sqs:DeleteMessage",
        "sqs:GetQueueAttributes"
      ],
      "Resource": "arn:aws:sqs:us-east-1:*:nashtto-image-processing-queue-prod"
    },
    {
      "Effect": "Allow",
      "Action": "logs:*",
      "Resource": "arn:aws:logs:*:*:*"
    }
  ]
}
```

## Callback Payload

The Lambda sends a POST request to `CALLBACK_URL` with:

```json
{
  "success": true,
  "entityType": "vendors",
  "entityId": "123",
  "imageType": "logo",
  "originalKey": "originals/vendors/123/logo_original.jpg",
  "variants": {
    "thumbnail": {
      "webp": "https://cdn.nashtto.com/vendors/123/logo_thumbnail.webp",
      "jpeg": "https://cdn.nashtto.com/vendors/123/logo_thumbnail.jpg"
    },
    "small": {
      "webp": "https://cdn.nashtto.com/vendors/123/logo_small.webp",
      "jpeg": "https://cdn.nashtto.com/vendors/123/logo_small.jpg"
    },
    "medium": {
      "webp": "https://cdn.nashtto.com/vendors/123/logo_medium.webp",
      "jpeg": "https://cdn.nashtto.com/vendors/123/logo_medium.jpg"
    }
  },
  "processedAt": "2024-01-15T10:30:00.000Z"
}
```

## Local Testing

```bash
# Install dependencies
npm install

# Test with sample event
node -e "
  const handler = require('./index').handler;
  const event = {
    Records: [{
      body: JSON.stringify({
        Records: [{
          s3: {
            bucket: { name: 'nashtto-media-prod' },
            object: { key: 'originals/vendors/1/logo_original.jpg' }
          }
        }]
      })
    }]
  };
  handler(event).then(console.log);
"
```

## Monitoring

- **CloudWatch Logs**: `/aws/lambda/nashtto-image-processor`
- **CloudWatch Metrics**: Duration, Errors, Invocations
- **DLQ**: `nashtto-image-processing-dlq-prod` for failed messages
