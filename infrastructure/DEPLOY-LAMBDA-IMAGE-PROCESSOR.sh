#!/bin/bash
###############################################################################
# Deploy Lambda Image Processor
# Builds and deploys the Sharp-based image processing Lambda function
###############################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_NAME="nashtto"
ENVIRONMENT="prod"
REGION="us-east-1"
LAMBDA_DIR="lambda/image-processor"
FUNCTION_NAME="${PROJECT_NAME}-image-processor-${ENVIRONMENT}"

# Lambda configuration (matches CloudFormation stack)
MEMORY_SIZE=512
TIMEOUT=10

echo -e "${BLUE}========================================"
echo -e "  Lambda Image Processor Deployment     "
echo -e "========================================${NC}"
echo ""

# Check if we're in the right directory
if [ ! -d "$LAMBDA_DIR" ]; then
    echo -e "${RED}Error: Lambda directory not found: $LAMBDA_DIR${NC}"
    echo "Please run from the infrastructure/ directory"
    exit 1
fi

cd "$LAMBDA_DIR"

echo -e "${YELLOW}Step 1: Installing Node.js dependencies for Lambda (using Docker)...${NC}"

# Check if Docker is available
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Error: Docker is not installed or not running.${NC}"
    echo "Docker is required to build Lambda functions with native dependencies like Sharp."
    echo "Please install Docker Desktop from: https://www.docker.com/products/docker-desktop"
    exit 1
fi

# Remove existing node_modules to ensure clean install
rm -rf node_modules

# Use AWS Lambda Node.js 20 Docker image to install dependencies
# This ensures Sharp is compiled for the correct Linux x64 runtime
echo "Building Lambda package using AWS Lambda Docker image (x86_64/amd64)..."
docker run --rm \
    --platform linux/amd64 \
    --entrypoint /bin/bash \
    -v "$PWD":/var/task \
    -w /var/task \
    public.ecr.aws/lambda/nodejs:20 \
    -c "npm install --production"

if [ $? -ne 0 ]; then
    echo -e "${RED}Failed to install dependencies!${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Dependencies installed (Sharp compiled for Lambda runtime)${NC}"
echo ""

echo -e "${YELLOW}Step 2: Creating deployment package...${NC}"
rm -f function.zip
zip -r function.zip . -x "*.git*" -x "package-lock.json" -x ".DS_Store"

if [ ! -f "function.zip" ]; then
    echo -e "${RED}Failed to create deployment package!${NC}"
    exit 1
fi

PACKAGE_SIZE=$(ls -lh function.zip | awk '{print $5}')
echo -e "${GREEN}✓ Deployment package created: function.zip (${PACKAGE_SIZE})${NC}"
echo ""

# Check if Lambda function exists
echo -e "${YELLOW}Step 3: Checking if Lambda function exists...${NC}"
FUNCTION_EXISTS=$(aws lambda get-function --function-name "$FUNCTION_NAME" --region "$REGION" 2>&1 || echo "NOT_FOUND")

if [[ "$FUNCTION_EXISTS" == *"NOT_FOUND"* ]] || [[ "$FUNCTION_EXISTS" == *"ResourceNotFoundException"* ]]; then
    echo -e "${YELLOW}Lambda function does not exist. Creating...${NC}"
    
    # Get the execution role ARN from CloudFormation stack
    ROLE_ARN=$(aws cloudformation describe-stacks \
        --stack-name "${PROJECT_NAME}-image-processing" \
        --region "$REGION" \
        --query 'Stacks[0].Outputs[?OutputKey==`LambdaExecutionRoleArn`].OutputValue' \
        --output text 2>/dev/null)
    
    if [ -z "$ROLE_ARN" ] || [ "$ROLE_ARN" == "None" ]; then
        echo -e "${RED}Error: Lambda execution role not found.${NC}"
        echo "Make sure the image-processing stack is deployed with EnableLambdaProcessing=true"
        exit 1
    fi
    
    # Get environment variables from CloudFormation outputs
    MEDIA_BUCKET=$(aws cloudformation describe-stacks \
        --stack-name "${PROJECT_NAME}-image-processing" \
        --region "$REGION" \
        --query 'Stacks[0].Outputs[?OutputKey==`MediaBucketName`].OutputValue' \
        --output text)
    
    CLOUDFRONT_DOMAIN=$(aws cloudformation describe-stacks \
        --stack-name "${PROJECT_NAME}-image-processing" \
        --region "$REGION" \
        --query 'Stacks[0].Outputs[?OutputKey==`CloudFrontDomainName`].OutputValue' \
        --output text)
    
    SQS_QUEUE_ARN=$(aws cloudformation describe-stacks \
        --stack-name "${PROJECT_NAME}-image-processing" \
        --region "$REGION" \
        --query 'Stacks[0].Outputs[?OutputKey==`ImageProcessingQueueArn`].OutputValue' \
        --output text)
    
    RESULT_QUEUE_URL=$(aws cloudformation describe-stacks \
        --stack-name "${PROJECT_NAME}-image-processing" \
        --region "$REGION" \
        --query 'Stacks[0].Outputs[?OutputKey==`ImageProcessingResultQueueUrl`].OutputValue' \
        --output text)
    
    # Create Lambda function
    echo -e "${YELLOW}Creating Lambda function...${NC}"
    aws lambda create-function \
        --function-name "$FUNCTION_NAME" \
        --runtime nodejs20.x \
        --handler index.handler \
        --role "$ROLE_ARN" \
        --zip-file fileb://function.zip \
        --memory-size $MEMORY_SIZE \
        --timeout $TIMEOUT \
        --region "$REGION" \
        --environment "Variables={MEDIA_BUCKET=${MEDIA_BUCKET},CLOUDFRONT_DOMAIN=${CLOUDFRONT_DOMAIN},RESULT_QUEUE_URL=${RESULT_QUEUE_URL}}" \
        --tags "Project=${PROJECT_NAME},Environment=${ENVIRONMENT}" \
        --no-cli-pager > /dev/null
    
    echo -e "${GREEN}✓ Lambda function created${NC}"
    
    # Add SQS trigger
    echo -e "${YELLOW}Adding SQS trigger...${NC}"
    aws lambda create-event-source-mapping \
        --function-name "$FUNCTION_NAME" \
        --event-source-arn "$SQS_QUEUE_ARN" \
        --batch-size 5 \
        --region "$REGION" \
        --no-cli-pager > /dev/null
    
    echo -e "${GREEN}✓ SQS trigger added${NC}"
    
else
    echo -e "${GREEN}Lambda function exists. Updating code...${NC}"
    
    aws lambda update-function-code \
        --function-name "$FUNCTION_NAME" \
        --zip-file fileb://function.zip \
        --region "$REGION" \
        --no-cli-pager > /dev/null
    
    echo -e "${GREEN}✓ Lambda function code updated${NC}"
fi

echo ""

# Wait for function to be ready
echo -e "${YELLOW}Step 4: Waiting for Lambda to be ready...${NC}"
aws lambda wait function-updated --function-name "$FUNCTION_NAME" --region "$REGION" 2>/dev/null || true
aws lambda wait function-active --function-name "$FUNCTION_NAME" --region "$REGION" 2>/dev/null || true
echo -e "${GREEN}✓ Lambda function is ready${NC}"
echo ""

# Get function details
echo -e "${YELLOW}Step 5: Getting function details...${NC}"
FUNCTION_ARN=$(aws lambda get-function --function-name "$FUNCTION_NAME" --region "$REGION" \
    --query 'Configuration.FunctionArn' --output text)
LAST_MODIFIED=$(aws lambda get-function --function-name "$FUNCTION_NAME" --region "$REGION" \
    --query 'Configuration.LastModified' --output text)

echo ""
echo -e "${GREEN}========================================"
echo -e "  ✅ Lambda Deployment Complete!        "
echo -e "========================================${NC}"
echo ""
echo -e "${BLUE}Function Details:${NC}"
echo "  Name:          $FUNCTION_NAME"
echo "  ARN:           $FUNCTION_ARN"
echo "  Memory:        ${MEMORY_SIZE} MB"
echo "  Timeout:       ${TIMEOUT} seconds"
echo "  Last Modified: $LAST_MODIFIED"
echo ""
echo -e "${BLUE}To test the function:${NC}"
echo "  1. Upload an image via the API"
echo "  2. Monitor in CloudWatch Logs:"
echo "     aws logs tail /aws/lambda/$FUNCTION_NAME --follow"
echo ""
echo -e "${BLUE}To update the function code later:${NC}"
echo "  ./DEPLOY-LAMBDA-IMAGE-PROCESSOR.sh"
echo ""

cd ../..
