#!/bin/bash

###############################################################################
# AWS Infrastructure Deployment Script
# Run this after adding AdministratorAccess to nastto-admin IAM user
###############################################################################

set -e

echo "🚀 Starting AWS Infrastructure Deployment..."
echo ""

# Change to CloudFormation directory
cd "$(dirname "$0")/cloudformation"

echo "📋 Parameters configured:"
echo "  - Your IP: 49.207.205.84/32"
echo "  - EC2 Instance: t2.micro"
echo "  - Database Password: IjhY3HEqWGjk0deZ (SAVE THIS!)"
echo "  - Domain: nashtto.com"
echo ""

echo "🔑 Checking SSH key pair..."
if [ -f "nastto-key.pem" ]; then
    echo "✅ Key pair exists"
else
    echo "⚠️  Key pair not found locally, will use existing AWS key pair"
fi
echo ""

echo "☁️  Deploying CloudFormation stack..."
echo "   This will take 25-60 minutes..."
echo ""

STACK_RESPONSE=$(aws cloudformation create-stack \
  --stack-name nashtto-infrastructure \
  --template-body file://infrastructure-stack.yaml \
  --parameters file://parameters.json \
  --capabilities CAPABILITY_NAMED_IAM \
  --region us-east-1 \
  --tags Key=Project,Value=Nashtto Key=Environment,Value=Production Key=ManagedBy,Value=CloudFormation 2>&1)

if [ $? -eq 0 ]; then
    STACK_ID=$(echo $STACK_RESPONSE | grep -o 'arn:aws:cloudformation[^"]*')
    echo ""
    echo "✅ Stack creation initiated successfully!"
    echo "Stack ID: $STACK_ID"
    echo ""
    echo "⏳ Monitoring deployment progress..."
    echo "   This will take 25-60 minutes. Press Ctrl+C to stop monitoring (deployment will continue)."
    echo ""
    sleep 3
    
    # Monitor deployment with periodic updates
    LAST_STATUS=""
    COUNTER=0
    
    while true; do
        # Get current stack status
        CURRENT_STATUS=$(aws cloudformation describe-stacks \
            --stack-name nashtto-infrastructure \
            --region us-east-1 \
            --query 'Stacks[0].StackStatus' \
            --output text 2>/dev/null)
        
        if [ "$CURRENT_STATUS" != "$LAST_STATUS" ]; then
            echo "📊 Status: $CURRENT_STATUS ($(date '+%H:%M:%S'))"
            LAST_STATUS=$CURRENT_STATUS
        fi
        
        # Show progress every 30 seconds
        if [ $((COUNTER % 6)) -eq 0 ]; then
            echo ""
            echo "🔨 Recent resources:"
            aws cloudformation describe-stack-events \
                --stack-name nashtto-infrastructure \
                --region us-east-1 \
                --max-items 5 \
                --query 'StackEvents[].[LogicalResourceId,ResourceType,ResourceStatus]' \
                --output table 2>/dev/null | tail -n +4
            echo ""
        fi
        
        # Check if deployment completed (success or failure)
        if [[ "$CURRENT_STATUS" == "CREATE_COMPLETE" ]]; then
            echo ""
            echo "🎉 =========================================================================="
            echo "🎉 SUCCESS! Infrastructure deployment completed successfully!"
            echo "🎉 =========================================================================="
            echo ""
            
            # Show all created resources
            echo "✅ Created Resources:"
            aws cloudformation list-stack-resources \
                --stack-name nashtto-infrastructure \
                --region us-east-1 \
                --query 'StackResourceSummaries[?ResourceStatus==`CREATE_COMPLETE`].[LogicalResourceId,ResourceType]' \
                --output table
            
            echo ""
            echo "📝 Stack Outputs:"
            aws cloudformation describe-stacks \
                --stack-name nashtto-infrastructure \
                --region us-east-1 \
                --query 'Stacks[0].Outputs' \
                --output table
            
            echo ""
            echo "🔐 IMPORTANT - Save These Details:"
            echo "   Database Password: IjhY3HEqWGjk0deZ"
            echo "   SSH Key Location: $(pwd)/nastto-key.pem"
            echo ""
            echo "📋 Next Steps:"
            echo "   1. Update GoDaddy nameservers with Route 53 NS records (see outputs above)"
            echo "   2. SSH into EC2: ssh -i nastto-key.pem ec2-user@<EC2_PUBLIC_IP>"
            echo "   3. Deploy your JAR: scp -i nastto-key.pem your-app.jar ec2-user@<IP>:/opt/nashtto/"
            echo ""
            break
            
        elif [[ "$CURRENT_STATUS" == "ROLLBACK_COMPLETE" ]] || [[ "$CURRENT_STATUS" == "CREATE_FAILED" ]]; then
            echo ""
            echo "❌ =========================================================================="
            echo "❌ DEPLOYMENT FAILED - Stack rolled back"
            echo "❌ =========================================================================="
            echo ""
            
            # Show failed resources
            echo "❌ Failed Resources:"
            aws cloudformation describe-stack-events \
                --stack-name nashtto-infrastructure \
                --region us-east-1 \
                --query 'StackEvents[?ResourceStatus==`CREATE_FAILED`].[LogicalResourceId,ResourceType,ResourceStatusReason]' \
                --output table
            
            echo ""
            echo "💡 Common Issues:"
            echo "   1. RDS backup retention (free tier = 1 day max)"
            echo "   2. Redis cluster configuration"
            echo "   3. SSH key pair doesn't exist"
            echo "   4. Invalid CIDR or parameters"
            echo ""
            echo "🔧 To Fix and Retry:"
            echo "   1. Check the errors above"
            echo "   2. Fix the issue in infrastructure-stack.yaml"
            echo "   3. Run: ./CLEANUP.sh"
            echo "   4. Run: ./DEPLOY.sh"
            echo ""
            exit 1
            
        elif [[ "$CURRENT_STATUS" == "ROLLBACK_IN_PROGRESS" ]]; then
            echo "⚠️  Rollback in progress due to errors..."
        fi
        
        COUNTER=$((COUNTER + 1))
        sleep 5
    done
    
else
    echo ""
    echo "❌ =========================================================================="
    echo "❌ Failed to initiate stack creation"
    echo "❌ =========================================================================="
    echo ""
    echo "$STACK_RESPONSE"
    echo ""
    echo "Common issues:"
    echo "  1. IAM user doesn't have AdministratorAccess"
    echo "  2. Stack already exists (run ./CLEANUP.sh first)"
    echo "  3. Invalid parameters in parameters.json"
    echo "  4. SSH key pair 'nastto-key' doesn't exist in us-east-1"
    echo ""
    exit 1
fi


