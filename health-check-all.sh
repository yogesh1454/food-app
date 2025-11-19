#!/bin/bash

echo "🏥 COMPREHENSIVE HEALTH CHECK - Tea & Snacks Delivery Aggregator"
echo "================================================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to check HTTP endpoint
check_http() {
    local url=$1
    local name=$2
    local timeout=${3:-5}
    
    if curl -s --max-time $timeout "$url" > /dev/null 2>&1; then
        echo -e "   ✅ ${GREEN}$name: UP${NC}"
        return 0
    else
        echo -e "   ❌ ${RED}$name: DOWN${NC}"
        return 1
    fi
}

# Function to check HTTP endpoint with JSON response
check_http_json() {
    local url=$1
    local name=$2
    local timeout=${3:-5}
    
    local response=$(curl -s --max-time $timeout "$url" 2>/dev/null)
    if [[ $? -eq 0 && -n "$response" ]]; then
        # Check if overall status is UP using grep instead of jq
        if echo "$response" | grep -q '"status":"UP"'; then
            echo -e "   ✅ ${GREEN}$name: UP${NC}"
            return 0
        else
            echo -e "   ⚠️  ${YELLOW}$name: DOWN or UNHEALTHY${NC}"
            return 1
        fi
    else
        echo -e "   ❌ ${RED}$name: DOWN${NC}"
        return 1
    fi
}

# Infrastructure Health Check
echo -e "${BLUE}🏗️  INFRASTRUCTURE SERVICES (Epic 2)${NC}"
echo "==========================================="

infra_healthy=0
total_infra=3

# PostgreSQL
if docker exec tea-snacks-postgres pg_isready -U tea_snacks_user > /dev/null 2>&1; then
    echo -e "   ✅ ${GREEN}PostgreSQL: UP${NC}"
    ((infra_healthy++))
else
    echo -e "   ❌ ${RED}PostgreSQL: DOWN${NC}"
fi

# Redis
if docker exec tea-snacks-redis redis-cli ping > /dev/null 2>&1; then
    echo -e "   ✅ ${GREEN}Redis: UP${NC}"
    ((infra_healthy++))
else
    echo -e "   ❌ ${RED}Redis: DOWN${NC}"
fi

# Kafka (check directly via docker container health)
if docker exec tea-snacks-kafka kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1; then
    echo -e "   ✅ ${GREEN}Kafka: UP${NC}"
    ((infra_healthy++))
else
    echo -e "   ❌ ${RED}Kafka: DOWN${NC}"
fi

echo ""
echo -e "${BLUE}📊 Infrastructure Health: $infra_healthy/$total_infra services UP${NC}"
echo ""

# Microservices Health Check
echo -e "${BLUE}🚀 MICROSERVICES (Epic 2)${NC}"
echo "==========================="

services_healthy=0
total_services=2

# User Management Service
if check_http_json "http://localhost:8081/actuator/health" "User Management (8081)" 5; then
    ((services_healthy++))
fi

# Notification Service
if check_http_json "http://localhost:8085/actuator/health" "Notification Service (8085)" 5; then
    ((services_healthy++))
fi

echo ""
echo -e "${BLUE}📊 Microservices Health: $services_healthy/$total_services services UP${NC}"
echo ""

# Redis Integration Specific Check (for current Epic 2 Story 1)
echo -e "${BLUE}🔴 REDIS INTEGRATION (Epic 2 Story 1)${NC}"
echo "====================================="

redis_integration_healthy=0
total_redis_checks=3

# Test Redis connectivity from user management service
user_health=$(curl -s --max-time 5 "http://localhost:8081/actuator/health" 2>/dev/null)
if [[ $? -eq 0 && -n "$user_health" ]]; then
    # Check if Redis component exists and is UP using grep instead of jq
    if echo "$user_health" | grep -q '"redis":{"status":"UP"'; then
        echo -e "   ✅ ${GREEN}Redis Connection: UP${NC}"
        ((redis_integration_healthy++))
        
        # Extract Redis version using grep and sed
        redis_version=$(echo "$user_health" | grep -o '"version":"[^"]*"' | sed 's/"version":"//' | sed 's/"//')
        echo -e "   📋 Redis Version: $redis_version"
    else
        echo -e "   ❌ ${RED}Redis Connection: DOWN or MISSING${NC}"
    fi
else
    echo -e "   ❌ ${RED}Redis Connection: FAILED${NC}"
fi

# Test Database connectivity from user management service
if echo "$user_health" | grep -q '"db":{"status":"UP"'; then
    echo -e "   ✅ ${GREEN}Database Connection: UP${NC}"
    ((redis_integration_healthy++))
else
    echo -e "   ❌ ${RED}Database Connection: DOWN${NC}"
fi

# Test Redis direct connectivity
if docker exec tea-snacks-redis redis-cli ping > /dev/null 2>&1; then
    echo -e "   ✅ ${GREEN}Redis Direct Access: UP${NC}"
    ((redis_integration_healthy++))
else
    echo -e "   ❌ ${RED}Redis Direct Access: DOWN${NC}"
fi

echo ""
echo -e "${BLUE}📊 Redis Integration Health: $redis_integration_healthy/$total_redis_checks checks PASSED${NC}"
echo ""

# Overall Summary
echo -e "${BLUE}🎯 OVERALL SYSTEM HEALTH${NC}"
echo "========================"
total_healthy=$((infra_healthy + services_healthy))
total_components=$((total_infra + total_services))

if [[ $total_healthy -eq $total_components ]]; then
    echo -e "   🎉 ${GREEN}EXCELLENT: All $total_components components are healthy!${NC}"
elif [[ $total_healthy -ge $((total_components * 3 / 4)) ]]; then
    echo -e "   ✅ ${GREEN}GOOD: $total_healthy/$total_components components are healthy${NC}"
elif [[ $total_healthy -ge $((total_components / 2)) ]]; then
    echo -e "   ⚠️  ${YELLOW}FAIR: $total_healthy/$total_components components are healthy${NC}"
else
    echo -e "   ❌ ${RED}POOR: Only $total_healthy/$total_components components are healthy${NC}"
fi

echo ""
echo -e "${BLUE}🔧 Epic 2 Story 1 (Redis Integration) Status:${NC}"
if [[ $redis_integration_healthy -ge 3 ]]; then
    echo -e "   ✅ ${GREEN}COMPLETED and OPERATIONAL${NC}"
else
    echo -e "   ⚠️  ${YELLOW}NEEDS ATTENTION${NC}"
fi

echo ""
echo "================================================================="
echo "Health check completed at $(date)"
echo "================================================================="
