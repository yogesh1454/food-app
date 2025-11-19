#!/bin/bash

###############################################################################
# Script: empty-s3-buckets.sh
# Description: Empties all S3 buckets created by CloudFormation stack
# Usage: ./empty-s3-buckets.sh <stack-name>
# Example: ./empty-s3-buckets.sh nashtto-infrastructure
###############################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if stack name provided
if [ -z "$1" ]; then
    echo -e "${RED}Error: Stack name required${NC}"
    echo "Usage: $0 <stack-name>"
    echo "Example: $0 nashtto-infrastructure"
    exit 1
fi

STACK_NAME=$1

echo -e "${GREEN}=== S3 Bucket Cleanup Script ===${NC}"
echo "Stack: $STACK_NAME"
echo ""

# Verify stack exists
echo -e "${YELLOW}Checking if stack exists...${NC}"
if ! aws cloudformation describe-stacks --stack-name $STACK_NAME &> /dev/null; then
    echo -e "${RED}Error: Stack '$STACK_NAME' not found${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Stack found${NC}"
echo ""

# Function to empty a bucket
empty_bucket() {
    local bucket_name=$1
    local bucket_display_name=$2
    
    if [ -z "$bucket_name" ]; then
        echo -e "${YELLOW}⊗ ${bucket_display_name}: Not found in stack outputs${NC}"
        return
    fi
    
    echo -e "${YELLOW}Processing: ${bucket_display_name}${NC}"
    echo "  Bucket: $bucket_name"
    
    # Check if bucket exists
    if ! aws s3api head-bucket --bucket "$bucket_name" 2>/dev/null; then
        echo -e "${YELLOW}  ⊗ Bucket does not exist or access denied${NC}"
        return
    fi
    
    # Get object count
    OBJECT_COUNT=$(aws s3 ls s3://$bucket_name --recursive --summarize 2>/dev/null | grep "Total Objects:" | awk '{print $3}')
    
    if [ -z "$OBJECT_COUNT" ] || [ "$OBJECT_COUNT" -eq 0 ]; then
        echo -e "${GREEN}  ✓ Bucket is already empty${NC}"
        return
    fi
    
    echo "  Objects found: $OBJECT_COUNT"
    
    # Delete all objects (non-versioned)
    echo "  Deleting objects..."
    aws s3 rm s3://$bucket_name --recursive --quiet
    
    # Check if versioning is enabled
    VERSIONING_STATUS=$(aws s3api get-bucket-versioning --bucket $bucket_name --query 'Status' --output text 2>/dev/null)
    
    if [ "$VERSIONING_STATUS" == "Enabled" ]; then
        echo "  Versioning enabled - deleting all versions..."
        
        # Delete all versions
        VERSIONS=$(aws s3api list-object-versions \
            --bucket $bucket_name \
            --query 'Versions[].{Key:Key,VersionId:VersionId}' \
            --output json)
        
        if [ "$VERSIONS" != "null" ] && [ "$VERSIONS" != "[]" ]; then
            aws s3api delete-objects \
                --bucket $bucket_name \
                --delete "{\"Objects\": $VERSIONS, \"Quiet\": true}" \
                --quiet 2>/dev/null || echo "    No more versions to delete"
        fi
        
        # Delete all delete markers
        DELETE_MARKERS=$(aws s3api list-object-versions \
            --bucket $bucket_name \
            --query 'DeleteMarkers[].{Key:Key,VersionId:VersionId}' \
            --output json)
        
        if [ "$DELETE_MARKERS" != "null" ] && [ "$DELETE_MARKERS" != "[]" ]; then
            aws s3api delete-objects \
                --bucket $bucket_name \
                --delete "{\"Objects\": $DELETE_MARKERS, \"Quiet\": true}" \
                --quiet 2>/dev/null || echo "    No more delete markers"
        fi
    fi
    
    # Verify bucket is empty
    REMAINING_COUNT=$(aws s3 ls s3://$bucket_name --recursive --summarize 2>/dev/null | grep "Total Objects:" | awk '{print $3}')
    if [ -z "$REMAINING_COUNT" ] || [ "$REMAINING_COUNT" -eq 0 ]; then
        echo -e "${GREEN}  ✓ Successfully emptied bucket${NC}"
    else
        echo -e "${RED}  ✗ Warning: $REMAINING_COUNT objects remain${NC}"
    fi
    
    echo ""
}

# Get bucket names from CloudFormation outputs
echo -e "${YELLOW}Retrieving bucket names from CloudFormation...${NC}"

STATIC_BUCKET=$(aws cloudformation describe-stacks \
    --stack-name $STACK_NAME \
    --query 'Stacks[0].Outputs[?OutputKey==`StaticAssetsBucketName`].OutputValue' \
    --output text 2>/dev/null)

UPLOADS_BUCKET=$(aws cloudformation describe-stacks \
    --stack-name $STACK_NAME \
    --query 'Stacks[0].Outputs[?OutputKey==`UserUploadsBucketName`].OutputValue' \
    --output text 2>/dev/null)

BACKUPS_BUCKET=$(aws cloudformation describe-stacks \
    --stack-name $STACK_NAME \
    --query 'Stacks[0].Outputs[?OutputKey==`BackupsBucketName`].OutputValue' \
    --output text 2>/dev/null)

LOGS_BUCKET=$(aws cloudformation describe-stacks \
    --stack-name $STACK_NAME \
    --query 'Stacks[0].Outputs[?OutputKey==`LogsBucketName`].OutputValue' \
    --output text 2>/dev/null)

echo -e "${GREEN}✓ Bucket names retrieved${NC}"
echo ""

# Confirm before proceeding
echo -e "${RED}WARNING: This will PERMANENTLY DELETE all objects in the following buckets:${NC}"
echo "  - Static Assets: $STATIC_BUCKET"
echo "  - User Uploads: $UPLOADS_BUCKET"
echo "  - Backups: $BACKUPS_BUCKET"
echo "  - Logs: $LOGS_BUCKET"
echo ""
echo -e "${YELLOW}This action CANNOT be undone!${NC}"
echo ""
read -p "Are you sure you want to continue? (type 'yes' to confirm): " CONFIRMATION

if [ "$CONFIRMATION" != "yes" ]; then
    echo -e "${YELLOW}Operation cancelled${NC}"
    exit 0
fi

echo ""
echo -e "${GREEN}=== Starting bucket cleanup ===${NC}"
echo ""

# Empty each bucket
empty_bucket "$STATIC_BUCKET" "Static Assets Bucket"
empty_bucket "$UPLOADS_BUCKET" "User Uploads Bucket"
empty_bucket "$BACKUPS_BUCKET" "Backups Bucket"
empty_bucket "$LOGS_BUCKET" "Logs Bucket"

echo -e "${GREEN}==================================${NC}"
echo -e "${GREEN}✓ All buckets have been processed${NC}"
echo -e "${GREEN}==================================${NC}"
echo ""
echo "You can now safely delete the CloudFormation stack:"
echo ""
echo "  aws cloudformation delete-stack --stack-name $STACK_NAME"
echo ""


