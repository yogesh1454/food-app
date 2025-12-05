#!/bin/bash

###############################################################################
# Deploy Order Catalog Service to AWS EC2
# This script builds the JAR and deploys it to EC2
###############################################################################

set -e

# Configuration
EC2_IP="54.87.117.181"
EC2_USER="ec2-user"
SSH_KEY="infrastructure/cloudformation/nastto-key-new.pem"
SERVICE_NAME="nashtto-order-service"
APP_DIR="/opt/nashtto"

echo "🚀 Starting Deployment to AWS EC2..."
echo ""

# Step 1: Build JAR
echo "📦 Building JAR file..."
cd tea-snacks-delivery-aggregator
./gradlew :order-catalog-service:clean :order-catalog-service:bootJar

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

JAR_FILE="order-catalog-service/build/libs/order-catalog-service-0.0.1-SNAPSHOT.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR file not found: $JAR_FILE"
    exit 1
fi

echo "✅ JAR built successfully: $JAR_FILE"
echo ""

# Step 2: Copy JAR to EC2
echo "📤 Copying JAR to EC2..."

scp -i "../$SSH_KEY" \
    "$JAR_FILE" \
    "$EC2_USER@$EC2_IP:$APP_DIR/"

cd ..

if [ $? -ne 0 ]; then
    echo "❌ Failed to copy JAR to EC2!"
    exit 1
fi

echo "✅ JAR copied to EC2 (with embedded prod config)"
echo ""

# Step 3: Stop any existing process and restart application on EC2
echo "🔄 Deploying application on EC2..."

ssh -i "$SSH_KEY" "$EC2_USER@$EC2_IP" << 'ENDSSH'
    # Step 3a: Check and kill any process using port 8080
    echo "   Checking for processes on port 8080..."
    PORT_PID=$(sudo lsof -ti:8080 2>/dev/null || echo "")
    
    if [ ! -z "$PORT_PID" ]; then
        echo "   Found process(es) on port 8080: $PORT_PID"
        echo "   Killing existing process(es)..."
        sudo kill -9 $PORT_PID
        sleep 2
        echo "   ✅ Port 8080 freed"
    else
        echo "   ✅ Port 8080 is available"
    fi
    
    # Step 3b: Check if systemd service exists
    if sudo systemctl list-units --full -all | grep -q nashtto-order-service; then
        echo "   Stopping existing service..."
        sudo systemctl stop nashtto-order-service 2>/dev/null || true
        
        echo "   Starting service..."
        sudo systemctl start nashtto-order-service
        
        echo "   Waiting for application to start..."
        sleep 10
        
        # Check service status
        if sudo systemctl is-active --quiet nashtto-order-service; then
            echo "   ✅ Service is running"
        else
            echo "   ❌ Service failed to start"
            echo "   Recent logs:"
            journalctl -u nashtto-order-service -n 20 --no-pager
            exit 1
        fi
    else
        echo "   ⚠️  Service not found. Running JAR directly..."
        cd /opt/nashtto
        mkdir -p logs
        
        # Start application in background
        nohup java -jar -Dspring.profiles.active=prod \
             order-catalog-service-0.0.1-SNAPSHOT.jar > logs/application.log 2>&1 &
        
        # Get the PID
        APP_PID=$!
        echo "   Application started with PID: $APP_PID"
        
        # Wait a bit and verify it's running
        sleep 5
        if ps -p $APP_PID > /dev/null; then
            echo "   ✅ Application is running"
        else
            echo "   ❌ Application failed to start"
            echo "   Check logs: tail -f /opt/nashtto/logs/application.log"
            exit 1
        fi
        
        echo "   Note: Setup as systemd service for production (see DEPLOYMENT_GUIDE.md)"
    fi
    
    # Step 3c: Verify port 8080 is now listening
    echo "   Verifying application is listening on port 8080..."
    sleep 3
    if sudo lsof -i:8080 >/dev/null 2>&1; then
        echo "   ✅ Application is listening on port 8080"
    else
        echo "   ⚠️  Warning: No process listening on port 8080 yet"
        echo "   Application may still be starting up..."
    fi
ENDSSH

if [ $? -ne 0 ]; then
    echo "❌ Failed to restart application on EC2!"
    exit 1
fi

echo ""
echo "✅ Deployment completed successfully!"
echo ""
echo "📋 Next Steps:"
echo "   1. Check health: curl http://$EC2_IP:8080/actuator/health"
echo "   2. View logs: ssh -i $SSH_KEY $EC2_USER@$EC2_IP 'tail -f $APP_DIR/logs/application.log'"
echo "   3. Access Swagger UI: http://$EC2_IP:8080/swagger-ui.html"
echo ""
echo "🌐 Swagger UI: http://$EC2_IP:8080/swagger-ui.html"
echo ""

# Optional: Open Swagger UI in browser
read -p "Open Swagger UI in browser? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    open "http://$EC2_IP:8080/swagger-ui.html" 2>/dev/null || \
    xdg-open "http://$EC2_IP:8080/swagger-ui.html" 2>/dev/null || \
    echo "Please open: http://$EC2_IP:8080/swagger-ui.html"
fi

echo "✅ Done!"
