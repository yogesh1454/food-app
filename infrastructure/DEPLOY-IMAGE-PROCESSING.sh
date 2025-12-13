#!/bin/bash
#
# Deploy Image Processing Infrastructure (S3, CloudFront, SQS)
# Based on IMAGE_STORAGE_AND_RENDERING_SPECIFICATION_REVISED.md
#
# Phase 1: S3 + CloudFront + SQS (Lambda disabled)
# Phase 2: Enable Lambda processing
#

set -e

# Configuration
STACK_NAME="nashtto-image-processing"
TEMPLATE_FILE="cloudformation/image-processing-stack.yaml"
REGION="us-east-1"  # Same region as other infrastructure stacks
PROJECT_NAME="nashtto"
ENVIRONMENT="prod"
EC2_ROLE_NAME="nashtto-ec2-role"
ENABLE_LAMBDA="false"  # Set to "true" for Phase 2

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Image Processing Infrastructure      ${NC}"
echo -e "${GREEN}  (S3 + CloudFront + SQS)              ${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Stack Name:     $STACK_NAME"
echo "Template:       $TEMPLATE_FILE"
echo "Region:         $REGION"
echo "Lambda Enabled: $ENABLE_LAMBDA"
echo ""

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo -e "${RED}Error: AWS CLI is not installed${NC}"
    exit 1
fi

# Check if template file exists
if [ ! -f "$TEMPLATE_FILE" ]; then
    echo -e "${RED}Error: Template file not found: $TEMPLATE_FILE${NC}"
    echo -e "${YELLOW}Make sure you're running this from the infrastructure/ directory${NC}"
    exit 1
fi

# Validate the template first
echo -e "${YELLOW}Validating CloudFormation template...${NC}"
aws cloudformation validate-template \
    --template-body file://$TEMPLATE_FILE \
    --region $REGION > /dev/null

echo -e "${GREEN}✓ Template is valid${NC}"
echo ""

# Check if stack exists and get its status
STACK_STATUS=""
STACK_EXISTS=$(aws cloudformation describe-stacks \
    --stack-name $STACK_NAME \
    --region $REGION 2>/dev/null || echo "")

if [ -n "$STACK_EXISTS" ]; then
    STACK_STATUS=$(echo "$STACK_EXISTS" | grep -o '"StackStatus": "[^"]*"' | head -1 | cut -d'"' -f4)
    echo -e "${YELLOW}Existing stack found: $STACK_NAME (Status: $STACK_STATUS)${NC}"
    
    # Check if stack is in a failed state that requires deletion
    if [[ "$STACK_STATUS" == "ROLLBACK_COMPLETE" || "$STACK_STATUS" == "DELETE_FAILED" || "$STACK_STATUS" == "CREATE_FAILED" ]]; then
        echo -e "${YELLOW}Stack is in $STACK_STATUS state. Deleting before re-creating...${NC}"
        
        # Empty the S3 buckets first (required before deletion)
        echo -e "${YELLOW}Emptying S3 buckets (if any)...${NC}"
        AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
        aws s3 rm s3://${PROJECT_NAME}-media-${ENVIRONMENT}-${AWS_ACCOUNT_ID} --recursive 2>/dev/null || true
        aws s3 rm s3://${PROJECT_NAME}-documents-${ENVIRONMENT}-${AWS_ACCOUNT_ID} --recursive 2>/dev/null || true
        
        aws cloudformation delete-stack \
            --stack-name $STACK_NAME \
            --region $REGION
        
        echo -e "${YELLOW}Waiting for stack deletion to complete...${NC}"
        aws cloudformation wait stack-delete-complete \
            --stack-name $STACK_NAME \
            --region $REGION 2>/dev/null || true
        
        echo -e "${GREEN}✓ Old stack deleted${NC}"
        STACK_EXISTS=""
    else
        echo -e "${YELLOW}Updating existing stack...${NC}"
    fi
fi

if [ -z "$STACK_EXISTS" ]; then
    echo -e "${YELLOW}Creating new stack: $STACK_NAME${NC}"
else
    echo -e "${YELLOW}Updating existing stack: $STACK_NAME${NC}"
fi

# ALWAYS Enable Lambda (Deploy Everything)
OVERRIDE_ENABLE_LAMBDA="true"

# Deploy the stack
echo ""
echo -e "${YELLOW}Deploying stack (CloudFormation)...${NC}"
aws cloudformation deploy \
    --stack-name $STACK_NAME \
    --template-file $TEMPLATE_FILE \
    --parameter-overrides \
        ProjectName=$PROJECT_NAME \
        Environment=$ENVIRONMENT \
        EC2RoleName=$EC2_ROLE_NAME \
        EnableLambdaProcessing=$OVERRIDE_ENABLE_LAMBDA \
    --capabilities CAPABILITY_NAMED_IAM \
    --region $REGION \
    --no-fail-on-empty-changeset

echo ""
echo -e "${GREEN}✓ CloudFormation Stack Deployed${NC}"
echo ""

# Get Output Values
MEDIA_BUCKET=$(aws cloudformation describe-stacks --stack-name $STACK_NAME --region $REGION --query "Stacks[0].Outputs[?OutputKey=='MediaBucketName'].OutputValue" --output text)
DOCUMENTS_BUCKET=$(aws cloudformation describe-stacks --stack-name $STACK_NAME --region $REGION --query "Stacks[0].Outputs[?OutputKey=='DocumentsBucketName'].OutputValue" --output text)
CDN_BASE_URL=$(aws cloudformation describe-stacks --stack-name $STACK_NAME --region $REGION --query "Stacks[0].Outputs[?OutputKey=='CDNBaseUrl'].OutputValue" --output text)
CLOUDFRONT_ID=$(aws cloudformation describe-stacks --stack-name $STACK_NAME --region $REGION --query "Stacks[0].Outputs[?OutputKey=='CloudFrontDistributionId'].OutputValue" --output text)
IMAGE_QUEUE=$(aws cloudformation describe-stacks --stack-name $STACK_NAME --region $REGION --query "Stacks[0].Outputs[?OutputKey=='ImageProcessingQueueUrl'].OutputValue" --output text | awk -F/ '{print $NF}')
RESULT_QUEUE=$(aws cloudformation describe-stacks --stack-name $STACK_NAME --region $REGION --query "Stacks[0].Outputs[?OutputKey=='ImageProcessingResultQueueName'].OutputValue" --output text)

# Deploy Lambda Code
echo -e "${YELLOW}Deploying Lambda Function Code...${NC}"
if [ -f "./DEPLOY-LAMBDA-IMAGE-PROCESSOR.sh" ]; then
    chmod +x ./DEPLOY-LAMBDA-IMAGE-PROCESSOR.sh
    ./DEPLOY-LAMBDA-IMAGE-PROCESSOR.sh
else
    echo -e "${RED}Error: Lambda deployment script not found!${NC}"
fi

# Update Application Config Files
echo ""
echo -e "${YELLOW}Updating Application Configuration Files...${NC}"

CONFIG_DIR="../tea-snacks-delivery-aggregator/order-catalog-service/src/main/resources"
APP_LOCAL="${CONFIG_DIR}/application.yml"
APP_PROD="${CONFIG_DIR}/application-prod.yml"

update_config_file() {
    local file=$1
    echo "Updating $file..."
    
    # OS-specific sed command
    SED_CMD="sed -i"
    if [[ "$OSTYPE" == "darwin"* ]]; then
        SED_CMD="sed -i ''"
    fi

    # Update buckets
    $SED_CMD "s|media: .*|media: $MEDIA_BUCKET|g" "$file"
    $SED_CMD "s|documents: .*|documents: $DOCUMENTS_BUCKET|g" "$file"
    
    # Update CloudFront
    $SED_CMD "s|distribution-id: .*|distribution-id: $CLOUDFRONT_ID|g" "$file"
    $SED_CMD "s|base-url: .*|base-url: $CDN_BASE_URL|g" "$file"
    
    # Update Queues
    $SED_CMD "s|image-processing: .*|image-processing: $IMAGE_QUEUE|g" "$file"
    $SED_CMD "s|image-processing-results: .*|image-processing-results: $RESULT_QUEUE|g" "$file"
}

# Add missing sections to prod config if needed
add_missing_prod_config() {
    local file=$1
    # Check if s3 section exists, if not append it
    if ! grep -q "s3:" "$file"; then
        echo "Appending S3/CloudFront config to $file..."
        cat <<EOL >> "$file"

  # Added by deployment script
  s3:
    region: $REGION
    buckets:
      media: $MEDIA_BUCKET
      documents: $DOCUMENTS_BUCKET
  cloudfront:
    distribution-id: $CLOUDFRONT_ID
    base-url: $CDN_BASE_URL
EOL
    fi
    
    # Add image processing queue if missing
    if ! grep -q "image-processing:" "$file"; then
         if grep -q "queues:" "$file"; then
            # Insert after queues
            $SED_CMD "/queues:/a\\
      image-processing: $IMAGE_QUEUE" "$file"
         fi
    fi
}

update_config_file "$APP_LOCAL"
add_missing_prod_config "$APP_PROD"
update_config_file "$APP_PROD" # Run update again just in case

echo -e "${GREEN}✓ Configuration files updated${NC}"
echo ""

# Subscribe email to alerts automatically
ALERT_TOPIC_ARN=$(aws cloudformation describe-stacks --stack-name $STACK_NAME --region $REGION --query "Stacks[0].Outputs[?OutputKey=='AlertTopicArn'].OutputValue" --output text)

if [ "$ALERT_TOPIC_ARN" != "None" ] && [ -n "$ALERT_TOPIC_ARN" ]; then
    DEFAULT_EMAIL="yogesh.bardia@gmail.com"
    echo -e "${YELLOW}Subscribing $DEFAULT_EMAIL to CloudWatch alerts...${NC}"
    
    # Check if already subscribed
    SUBSCRIPTION=$(aws sns list-subscriptions-by-topic --topic-arn "$ALERT_TOPIC_ARN" --region $REGION --query "Subscriptions[?Endpoint=='$DEFAULT_EMAIL']" --output text 2>/dev/null)
    
    if [ -z "$SUBSCRIPTION" ] || [ "$SUBSCRIPTION" == "None" ]; then
        aws sns subscribe \
            --topic-arn "$ALERT_TOPIC_ARN" \
            --protocol email \
            --notification-endpoint "$DEFAULT_EMAIL" \
            --region $REGION > /dev/null 2>&1
        
        echo -e "${GREEN}✓ Subscription request sent to $DEFAULT_EMAIL${NC}"
        echo -e "${YELLOW}  📧 Check your email and confirm the subscription!${NC}"
    else
        echo -e "${GREEN}✓ $DEFAULT_EMAIL is already subscribed${NC}"
    fi
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  ✅ DEPLOYMENT COMPLETE!               ${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${BLUE}What was deployed:${NC}"
echo "  ✓ S3 Buckets (Media & Documents)"
echo "  ✓ CloudFront CDN Distribution"
echo "  ✓ SQS Queue for Image Processing"
echo "  ✓ Lambda Function (Image Processor)"
echo "  ✓ CloudWatch Alarms (Free Tier, DLQ, Queue Depth, Storage)"
echo "  ✓ SNS Alert Topic"
echo ""
echo -e "${BLUE}What was configured:${NC}"
echo "  ✓ application.yml updated with AWS resources"
echo "  ✓ application-prod.yml updated with AWS resources"
echo "  ✓ Email alerts subscribed (check inbox)"
echo ""
echo -e "${BLUE}Resources Created:${NC}"
echo "  Media Bucket:     $MEDIA_BUCKET"
echo "  Documents Bucket: $DOCUMENTS_BUCKET"
echo "  CDN URL:          $CDN_BASE_URL"
echo "  CloudFront ID:    $CLOUDFRONT_ID"
echo "  SQS Queue:        $IMAGE_QUEUE"
echo ""
echo -e "${CYAN}========================================${NC}"
echo -e "${CYAN}  Next: Test the Image Upload Flow    ${NC}"
echo -e "${CYAN}========================================${NC}"
echo ""
echo "1. Start your backend application:"
echo "   cd ../tea-snacks-delivery-aggregator"
echo "   ./gradlew :order-catalog-service:bootRun"
echo ""
echo "2. Upload a test image via Swagger UI:"
echo "   http://54.87.117.181:8080/swagger-ui.html"
echo "   POST /api/v1/vendors/{id}/images"
echo ""
echo "3. Monitor Lambda processing:"
echo "   aws logs tail /aws/lambda/nashtto-image-processor-prod --follow"
echo ""
echo "4. View CloudWatch Alarms:"
echo "   https://console.aws.amazon.com/cloudwatch/home?region=us-east-1#alarmsV2:"
echo ""
echo -e "${GREEN}All systems ready! 🚀${NC}"

