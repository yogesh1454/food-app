#!/bin/bash

# Start minimal infrastructure for Epic 2 development
# Only starts services needed for User Management and Notification services

echo "🚀 Starting minimal infrastructure for Epic 2 development..."
echo "Services to start: PostgreSQL, Redis, Kafka, User Management, Notification"

# Navigate to docker directory
cd "$(dirname "$0")"

# Stop all services first to ensure clean state
echo "📋 Stopping all services first..."
docker-compose down

# Start only the essential infrastructure services
echo "🔧 Starting essential infrastructure services..."
docker-compose up -d postgres redis zookeeper kafka

# Wait for infrastructure to be ready
echo "⏳ Waiting for infrastructure services to be healthy..."
sleep 10

# Check infrastructure health
echo "🔍 Checking infrastructure health..."
docker-compose ps postgres redis kafka zookeeper

# Start the Epic 2 specific services
echo "🎯 Starting Epic 2 services..."
docker-compose up -d user-management-service notification-service

echo "✅ Minimal Epic 2 setup complete!"
echo ""
echo "📊 Running services:"
echo "  - PostgreSQL (Database): localhost:5432"
echo "  - Redis (Cache/Sessions): localhost:6379"  
echo "  - Kafka (Messaging): localhost:9092"
echo "  - User Management Service: localhost:8081"
echo "  - Notification Service: localhost:8085"
echo ""
echo "🛑 Stopped services (to save resources):"

echo "  - Grafana"
echo "  - Prometheus"
echo "  - Order Catalog Service"
echo "  - Payment Management Service"
echo "  - Delivery Management Service"
echo ""
echo "🔧 To check service status: docker-compose ps"
echo "🔧 To view logs: docker-compose logs -f [service-name]"
echo "🔧 To start all services again: docker-compose up -d"
