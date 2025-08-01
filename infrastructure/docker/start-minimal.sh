#!/bin/bash

# Start minimal infrastructure for Epic 2 development
# Only starts PostgreSQL, Redis, Kafka, and User Management Service

echo "🚀 Starting minimal infrastructure for Epic 2 (User Management Service) development..."
echo "Services: PostgreSQL, Redis, Kafka, User Management Service"
echo ""

# Change to docker directory
cd "$(dirname "$0")"

# Stop any existing containers
echo "🛑 Stopping any existing containers..."
docker-compose -f docker-compose-minimal.yml down

# Start minimal infrastructure
echo "🔧 Starting minimal infrastructure..."
docker-compose -f docker-compose-minimal.yml up -d

# Wait for services to be healthy
echo "⏳ Waiting for services to be healthy..."
echo "This may take 30-60 seconds..."

# Function to check service health
check_health() {
    local service=$1
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if docker-compose -f docker-compose-minimal.yml ps | grep -q "$service.*healthy"; then
            echo "✅ $service is healthy"
            return 0
        fi
        echo "⏳ Waiting for $service... (attempt $attempt/$max_attempts)"
        sleep 2
        ((attempt++))
    done
    
    echo "❌ $service failed to become healthy"
    return 1
}

# Check each service
check_health "postgres"
check_health "redis" 
check_health "kafka"
check_health "user-management"

echo ""
echo "🎉 Minimal infrastructure is ready!"
echo ""
echo "📊 Service Status:"
docker-compose -f docker-compose-minimal.yml ps
echo ""
echo "🔗 Available Services:"
echo "  • PostgreSQL: localhost:5432"
echo "  • Redis: localhost:6379" 
echo "  • Kafka: localhost:9092"
echo "  • User Management Service: http://localhost:8081"
echo ""
echo "💡 To stop services: ./stop-minimal.sh"
echo "💡 To view logs: docker-compose -f docker-compose-minimal.yml logs -f"
