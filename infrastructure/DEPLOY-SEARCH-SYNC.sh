#!/bin/bash
#
# Deploy Search Sync Infrastructure (SNS/SQS)
# This script deploys the search-sync-stack.yaml CloudFormation template
#

set -e

# Configuration
STACK_NAME="nashtto-search-sync"
TEMPLATE_FILE="cloudformation/search-sync-stack.yaml"
REGION="us-east-1"
PROJECT_NAME="nashtto"
ENVIRONMENT="prod"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Search Sync Infrastructure Deployment ${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Stack Name: $STACK_NAME"
echo "Template:   $TEMPLATE_FILE"
echo "Region:     $REGION"
echo ""

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo -e "${RED}Error: AWS CLI is not installed${NC}"
    exit 1
fi

# Check if template file exists
if [ ! -f "$TEMPLATE_FILE" ]; then
    echo -e "${RED}Error: Template file not found: $TEMPLATE_FILE${NC}"
    exit 1
fi

# Validate the template first
echo -e "${YELLOW}Validating CloudFormation template...${NC}"
aws cloudformation validate-template \
    --template-body file://$TEMPLATE_FILE \
    --region $REGION > /dev/null

echo -e "${GREEN}✓ Template is valid${NC}"
echo ""

# Check if stack exists
STACK_EXISTS=$(aws cloudformation describe-stacks \
    --stack-name $STACK_NAME \
    --region $REGION 2>/dev/null || echo "")

if [ -z "$STACK_EXISTS" ]; then
    echo -e "${YELLOW}Creating new stack: $STACK_NAME${NC}"
else
    echo -e "${YELLOW}Updating existing stack: $STACK_NAME${NC}"
fi

# Deploy the stack
echo ""
echo -e "${YELLOW}Deploying stack...${NC}"
aws cloudformation deploy \
    --stack-name $STACK_NAME \
    --template-file $TEMPLATE_FILE \
    --parameter-overrides \
        ProjectName=$PROJECT_NAME \
        Environment=$ENVIRONMENT \
    --capabilities CAPABILITY_IAM \
    --region $REGION \
    --no-fail-on-empty-changeset

echo ""
echo -e "${GREEN}✓ Deployment complete!${NC}"
echo ""

# Get and display outputs
echo -e "${YELLOW}Stack Outputs:${NC}"
echo "----------------------------------------"
aws cloudformation describe-stacks \
    --stack-name $STACK_NAME \
    --region $REGION \
    --query 'Stacks[0].Outputs[*].[OutputKey, OutputValue]' \
    --output table

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Next Steps:${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "1. Update application-prod.yml with the values above:"
echo ""
echo "   aws:"
echo "     sns:"
echo "       topics:"
echo "         search-index-events: <TopicArn>"
echo "     sqs:"
echo "       queues:"
echo "         search-index: <QueueUrl>"
echo ""
echo "2. Ensure your EC2 IAM role has permissions for SNS/SQS"
echo ""
