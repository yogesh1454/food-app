#!/bin/bash

echo "Testing Redis Integration for User Management Service"
echo "================================================="

# Base URL for the user management service
BASE_URL="http://localhost:8081"

echo "1. Testing Health Endpoints..."
echo "   - General Health:"
curl -s "$BASE_URL/actuator/health" | grep -o '"status":"[^"]*"' || echo "Failed"

echo "   - Redis Health:"
curl -s "$BASE_URL/actuator/health/redis" | grep -o '"status":"[^"]*"' || echo "Failed"

echo ""
echo "2. Testing Redis Connection via Actuator..."
curl -s "$BASE_URL/actuator/health" | grep -q "redis" && echo "   ✓ Redis component found in health check" || echo "   ✗ Redis component not found"

echo ""
echo "3. Testing Metrics Endpoint..."
curl -s "$BASE_URL/actuator/prometheus" | grep -q "redis" && echo "   ✓ Redis metrics available" || echo "   ✗ Redis metrics not found"

echo ""
echo "4. Testing Service Availability..."
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/actuator/health")
if [ "$HTTP_STATUS" = "200" ]; then
    echo "   ✓ User Management Service is responding (HTTP $HTTP_STATUS)"
else
    echo "   ✗ User Management Service not responding (HTTP $HTTP_STATUS)"
fi

echo ""
echo "5. Testing Redis Direct Connection..."
docker exec tea-snacks-redis redis-cli ping > /dev/null 2>&1 && echo "   ✓ Redis is responding to PING" || echo "   ✗ Redis not responding"

echo ""
echo "6. Testing Redis Keys (should be minimal on fresh start)..."
KEY_COUNT=$(docker exec tea-snacks-redis redis-cli DBSIZE 2>/dev/null || echo "0")
echo "   Current Redis key count: $KEY_COUNT"

echo ""
echo "Redis Integration Test Complete!"
echo "================================="
