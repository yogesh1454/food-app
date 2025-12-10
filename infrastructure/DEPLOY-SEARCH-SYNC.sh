#!/bin/bash
#
# Deploy Event-Driven Infrastructure (SNS/SQS)
# This script deploys/updates the event infrastructure for:
# - Search Index Sync
# - Order Events (Order ↔ Delivery integration)
# - Delivery Events (Order ↔ Delivery integration)
#

set -e

# Configuration
STACK_NAME="nashtto-search-sync"  # Use existing stack name
TEMPLATE_FILE="cloudformation/search-sync-stack.yaml"
REGION="us-east-1"
PROJECT_NAME="nashtto"
ENVIRONMENT="prod"
EC2_ROLE_NAME="nashtto-ec2-role"

# Cleanup: Delete any failed stacks with different names
CLEANUP_STACKS=("nashtto-event-infra")

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Event Infrastructure Deployment       ${NC}"
echo -e "${GREEN}  (SNS/SQS for Order ↔ Delivery FSM)   ${NC}"
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

# Cleanup any failed stacks with different names
for CLEANUP_STACK in "${CLEANUP_STACKS[@]}"; do
    CLEANUP_EXISTS=$(aws cloudformation describe-stacks \
        --stack-name "$CLEANUP_STACK" \
        --region $REGION 2>/dev/null || echo "")
    
    if [ -n "$CLEANUP_EXISTS" ]; then
        CLEANUP_STATUS=$(echo "$CLEANUP_EXISTS" | grep -o '"StackStatus": "[^"]*"' | head -1 | cut -d'"' -f4)
        if [[ "$CLEANUP_STATUS" == "ROLLBACK_COMPLETE" || "$CLEANUP_STATUS" == "DELETE_FAILED" || "$CLEANUP_STATUS" == "CREATE_FAILED" ]]; then
            echo -e "${YELLOW}Cleaning up failed stack: $CLEANUP_STACK ($CLEANUP_STATUS)${NC}"
            aws cloudformation delete-stack \
                --stack-name "$CLEANUP_STACK" \
                --region $REGION 2>/dev/null || true
            echo -e "${GREEN}✓ Cleanup initiated for $CLEANUP_STACK${NC}"
        fi
    fi
done

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
        echo -e "${YELLOW}Updating existing stack with new resources...${NC}"
    fi
fi

if [ -z "$STACK_EXISTS" ]; then
    echo -e "${YELLOW}Creating new stack: $STACK_NAME${NC}"
else
    echo -e "${YELLOW}Updating existing stack: $STACK_NAME${NC}"
fi

# Deploy the stack
echo ""
echo -e "${YELLOW}Deploying stack (this may take 2-3 minutes)...${NC}"
aws cloudformation deploy \
    --stack-name $STACK_NAME \
    --template-file $TEMPLATE_FILE \
    --parameter-overrides \
        ProjectName=$PROJECT_NAME \
        Environment=$ENVIRONMENT \
        EC2RoleName=$EC2_ROLE_NAME \
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

# Extract specific ARNs for display
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Resource ARNs (copy to app config)   ${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Get SNS Topic ARNs
SEARCH_TOPIC_ARN=$(aws cloudformation describe-stacks \
    --stack-name $STACK_NAME \
    --region $REGION \
    --query "Stacks[0].Outputs[?OutputKey=='SearchIndexTopicArn'].OutputValue" \
    --output text 2>/dev/null || echo "N/A")

ORDER_TOPIC_ARN=$(aws cloudformation describe-stacks \
    --stack-name $STACK_NAME \
    --region $REGION \
    --query "Stacks[0].Outputs[?OutputKey=='OrderEventsTopicArn'].OutputValue" \
    --output text 2>/dev/null || echo "N/A")

DELIVERY_TOPIC_ARN=$(aws cloudformation describe-stacks \
    --stack-name $STACK_NAME \
    --region $REGION \
    --query "Stacks[0].Outputs[?OutputKey=='DeliveryEventsTopicArn'].OutputValue" \
    --output text 2>/dev/null || echo "N/A")

# Get SQS Queue URLs
SEARCH_QUEUE_URL=$(aws cloudformation describe-stacks \
    --stack-name $STACK_NAME \
    --region $REGION \
    --query "Stacks[0].Outputs[?OutputKey=='SearchIndexQueueUrl'].OutputValue" \
    --output text 2>/dev/null || echo "N/A")

ORDER_FOR_DELIVERY_QUEUE_URL=$(aws cloudformation describe-stacks \
    --stack-name $STACK_NAME \
    --region $REGION \
    --query "Stacks[0].Outputs[?OutputKey=='OrderEventsForDeliveryQueueUrl'].OutputValue" \
    --output text 2>/dev/null || echo "N/A")

DELIVERY_FOR_ORDER_QUEUE_URL=$(aws cloudformation describe-stacks \
    --stack-name $STACK_NAME \
    --region $REGION \
    --query "Stacks[0].Outputs[?OutputKey=='DeliveryEventsForOrderQueueUrl'].OutputValue" \
    --output text 2>/dev/null || echo "N/A")

echo -e "${YELLOW}SNS Topics:${NC}"
echo "  search-index-events: $SEARCH_TOPIC_ARN"
echo "  order-events:        $ORDER_TOPIC_ARN"
echo "  delivery-events:     $DELIVERY_TOPIC_ARN"
echo ""
echo -e "${YELLOW}SQS Queues:${NC}"
echo "  search-index:              $SEARCH_QUEUE_URL"
echo "  order-events-for-delivery: $ORDER_FOR_DELIVERY_QUEUE_URL"
echo "  delivery-events-for-order: $DELIVERY_FOR_ORDER_QUEUE_URL"
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
echo "         search-index-events: $SEARCH_TOPIC_ARN"
echo "         order-events: $ORDER_TOPIC_ARN"
echo "         delivery-events: $DELIVERY_TOPIC_ARN"
echo "     sqs:"
echo "       queues:"
echo "         search-index: ${PROJECT_NAME}-search-index-queue"
echo "         order-events-for-delivery: ${PROJECT_NAME}-order-events-for-delivery"
echo "         delivery-events-for-order: ${PROJECT_NAME}-delivery-events-for-order"
echo ""
echo "2. Enable the feature flags in application-prod.yml:"
echo ""
echo "   features:"
echo "     sns:"
echo "       order-delivery-events:"
echo "         enabled: true"
echo "     sqs:"
echo "       order-delivery-events:"
echo "         enabled: true"
echo ""
echo "3. Deploy the application to EC2:"
echo ""
echo "   ./deploy-to-ec2.sh"
echo ""
echo -e "${GREEN}Deployment complete! ✓${NC}"
