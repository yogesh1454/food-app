#!/bin/bash

###############################################################################
# AWS Infrastructure Cleanup Script
# This will DELETE ALL resources created by the CloudFormation stack
###############################################################################

set -e

STACK_NAME="nashtto-infrastructure"
REGION="us-east-1"

echo "⚠️  =========================================================================="
echo "⚠️  WARNING: This will DELETE ALL AWS resources!"
echo "⚠️  =========================================================================="
echo ""
echo "Resources that will be DELETED:"
echo "  ❌ VPC and all networking components"
echo "  ❌ RDS PostgreSQL database (with final snapshot)"
echo "  ❌ ElastiCache Redis cluster"
echo "  ❌ All 4 S3 buckets and their contents"
echo "  ❌ SQS queues and SNS topics"
echo "  ❌ EC2 instance"
echo "  ❌ Route 53 hosted zone"
echo "  ❌ SSL certificate"
echo "  ❌ CloudWatch alarms and log groups"
echo ""
echo "⚠️  THIS ACTION CANNOT BE UNDONE!"
echo ""
read -p "Are you ABSOLUTELY SURE you want to delete everything? Type 'DELETE' to confirm: " CONFIRM

if [ "$CONFIRM" != "DELETE" ]; then
    echo ""
    echo "❌ Cleanup cancelled. No resources were deleted."
    exit 0
fi

echo ""
echo "🗑️  Starting cleanup process..."
echo ""

# Step 1: Check if stack exists
echo "📋 Step 1/3: Checking CloudFormation stack..."
if ! aws cloudformation describe-stacks --stack-name $STACK_NAME --region $REGION &> /dev/null; then
    echo "⚠️  Stack '$STACK_NAME' does not exist. Nothing to delete."
    exit 0
fi
echo "✅ Stack found"
echo ""

# Step 2: Empty S3 buckets
echo "📦 Step 2/3: Emptying S3 buckets..."
echo "   (S3 buckets must be empty before stack deletion)"
echo ""

cd "$(dirname "$0")/scripts"

if [ -f "empty-s3-buckets.sh" ]; then
    echo "   Running: ./empty-s3-buckets.sh $STACK_NAME"
    ./empty-s3-buckets.sh $STACK_NAME || {
        echo ""
        echo "⚠️  Failed to empty S3 buckets. You may need to empty them manually."
        echo "   Continue anyway? (y/n)"
        read -p "   > " CONTINUE
        if [ "$CONTINUE" != "y" ]; then
            echo "❌ Cleanup cancelled."
            exit 1
        fi
    }
else
    echo "⚠️  empty-s3-buckets.sh not found. Attempting manual bucket cleanup..."
    
    # Get bucket names from stack
    BUCKETS=$(aws cloudformation describe-stacks \
        --stack-name $STACK_NAME \
        --region $REGION \
        --query 'Stacks[0].Outputs[?contains(OutputKey, `Bucket`)].OutputValue' \
        --output text)
    
    if [ -n "$BUCKETS" ]; then
        for BUCKET in $BUCKETS; do
            echo "   Emptying: $BUCKET"
            aws s3 rm s3://$BUCKET --recursive --quiet || echo "   ⚠️  Failed to empty $BUCKET"
        done
    fi
fi

cd - > /dev/null

echo ""
echo "✅ S3 buckets emptied"
echo ""

# Step 3: Delete CloudFormation stack
echo "☁️  Step 3/3: Deleting CloudFormation stack..."
echo "   This will take 10-20 minutes..."
echo ""

aws cloudformation delete-stack \
    --stack-name $STACK_NAME \
    --region $REGION

if [ $? -eq 0 ]; then
    echo "✅ Stack deletion initiated"
    echo ""
    echo "⏳ Waiting for stack deletion to complete..."
    echo "   (This may take 10-20 minutes)"
    echo ""
    
    # Wait for deletion to complete
    aws cloudformation wait stack-delete-complete \
        --stack-name $STACK_NAME \
        --region $REGION
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "🎉 =========================================================================="
        echo "🎉 SUCCESS! All AWS resources have been deleted."
        echo "🎉 =========================================================================="
        echo ""
        echo "Deleted resources:"
        echo "  ✅ VPC and networking"
        echo "  ✅ RDS PostgreSQL database"
        echo "  ✅ ElastiCache Redis"
        echo "  ✅ S3 buckets"
        echo "  ✅ SQS queues and SNS topics"
        echo "  ✅ EC2 instance"
        echo "  ✅ Route 53 hosted zone"
        echo "  ✅ SSL certificate"
        echo "  ✅ CloudWatch resources"
        echo ""
        echo "💰 Your AWS bill should return to $0 (or near $0) within 24 hours."
        echo ""
    else
        echo ""
        echo "⚠️  Stack deletion is in progress but wait command timed out."
        echo "   Check status with:"
        echo "   aws cloudformation describe-stacks --stack-name $STACK_NAME --region $REGION"
        echo ""
    fi
else
    echo ""
    echo "❌ Failed to delete stack. Check the error message above."
    echo ""
    echo "Common issues:"
    echo "  1. S3 buckets not empty - Run: ./scripts/empty-s3-buckets.sh $STACK_NAME"
    echo "  2. RDS deletion protection enabled"
    echo "  3. Resources in use by other services"
    echo ""
    echo "Check CloudFormation events:"
    echo "  aws cloudformation describe-stack-events --stack-name $STACK_NAME --region $REGION"
    echo ""
    exit 1
fi

# Optional: Verify all resources are deleted
echo "🔍 Verifying cleanup..."
echo ""

# Check if stack still exists
if aws cloudformation describe-stacks --stack-name $STACK_NAME --region $REGION &> /dev/null; then
    echo "⚠️  Stack still exists. Deletion may still be in progress."
else
    echo "✅ CloudFormation stack deleted successfully"
fi

echo ""
echo "📝 Cleanup complete! You can now:"
echo "   1. Verify billing in AWS Console: https://console.aws.amazon.com/billing"
echo "   2. Redeploy anytime by running: ./DEPLOY.sh"
echo ""


