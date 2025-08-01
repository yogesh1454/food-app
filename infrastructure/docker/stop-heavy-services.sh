#!/bin/bash

# Stop resource-heavy services to optimize system performance for Epic 2 development
# Keeps only essential services: PostgreSQL, Redis, Kafka, User Management, Notification

echo "🛑 Stopping resource-heavy services to optimize system performance..."

# Navigate to docker directory
cd "$(dirname "$0")"

# Stop the resource-heavy services that aren't needed for Epic 2
echo "📋 Stopping non-essential services..."
docker-compose stop elasticsearch grafana prometheus
docker-compose stop order-catalog-service payment-management-service delivery-management-service

echo "✅ Stopped resource-heavy services!"
echo ""
echo "🟢 Still running (Epic 2 essentials):"
echo "  - PostgreSQL (Database): localhost:5432"
echo "  - Redis (Cache/Sessions): localhost:6379"
echo "  - Kafka + Zookeeper (Messaging): localhost:9092"
echo "  - User Management Service: localhost:8081"
echo "  - Notification Service: localhost:8085"
echo ""
echo "🔴 Stopped (to save resources):"
echo "  - Elasticsearch"
echo "  - Grafana"
echo "  - Prometheus"
echo "  - Order Catalog Service"
echo "  - Payment Management Service"
echo "  - Delivery Management Service"
echo ""
echo "🔧 To check what's running: docker-compose ps"
echo "🔧 To restart stopped services: docker-compose start [service-name]"
echo "🔧 To start all services: docker-compose up -d"
