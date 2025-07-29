#!/bin/bash

# Health Check Script for Tea & Snacks Delivery Aggregator
# This script validates that all infrastructure services are running correctly

echo "🔍 Health Check for Tea & Snacks Delivery Aggregator"
echo "=================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check service health
check_service() {
    local service_name=$1
    local check_command=$2
    local expected_output=$3
    
    echo -n "Checking $service_name... "
    
    if eval "$check_command" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ OK${NC}"
        return 0
    else
        echo -e "${RED}✗ FAILED${NC}"
        return 1
    fi
}

# Function to check port availability
check_port() {
    local service_name=$1
    local host=$2
    local port=$3
    
    echo -n "Checking $service_name on $host:$port... "
    
    if nc -z $host $port 2>/dev/null; then
        echo -e "${GREEN}✓ OK${NC}"
        return 0
    else
        echo -e "${RED}✗ FAILED${NC}"
        return 1
    fi
}

# Initialize counters
total_checks=0
passed_checks=0

echo ""
echo "📊 Infrastructure Health Check"
echo "-----------------------------"

# Check Docker Compose services
echo ""
echo "🐳 Docker Services:"
if docker-compose ps | grep -q "Up"; then
    echo -e "${GREEN}✓ Docker Compose services are running${NC}"
    ((passed_checks++))
else
    echo -e "${RED}✗ Docker Compose services are not running${NC}"
fi
((total_checks++))

# Check PostgreSQL
check_port "PostgreSQL" "localhost" "5432"
if [ $? -eq 0 ]; then
    ((passed_checks++))
fi
((total_checks++))

# Check Redis
check_port "Redis" "localhost" "6379"
if [ $? -eq 0 ]; then
    ((passed_checks++))
fi
((total_checks++))

# Check Kafka
check_port "Kafka" "localhost" "9092"
if [ $? -eq 0 ]; then
    ((passed_checks++))
fi
((total_checks++))

# Check Zookeeper
check_port "Zookeeper" "localhost" "2181"
if [ $? -eq 0 ]; then
    ((passed_checks++))
fi
((total_checks++))

# Check Elasticsearch
check_port "Elasticsearch" "localhost" "9200"
if [ $? -eq 0 ]; then
    ((passed_checks++))
fi
((total_checks++))

# Check Prometheus
check_port "Prometheus" "localhost" "9090"
if [ $? -eq 0 ]; then
    ((passed_checks++))
fi
((total_checks++))

# Check Grafana
check_port "Grafana" "localhost" "3000"
if [ $? -eq 0 ]; then
    ((passed_checks++))
fi
((total_checks++))

# Check Kafka UI
check_port "Kafka UI" "localhost" "8080"
if [ $? -eq 0 ]; then
    ((passed_checks++))
fi
((total_checks++))

echo ""
echo "🔍 Service Health Checks:"
echo "------------------------"

# Check PostgreSQL connection
check_service "PostgreSQL Connection" "docker exec tea-snacks-postgres pg_isready -U tea_snacks_user -d tea_snacks_db" ""
if [ $? -eq 0 ]; then
    ((passed_checks++))
fi
((total_checks++))

# Check Redis connection
check_service "Redis Connection" "docker exec tea-snacks-redis redis-cli ping" "PONG"
if [ $? -eq 0 ]; then
    ((passed_checks++))
fi
((total_checks++))

# Check Kafka topics
check_service "Kafka Topics" "docker exec tea-snacks-kafka kafka-topics --bootstrap-server localhost:9092 --list" ""
if [ $? -eq 0 ]; then
    ((passed_checks++))
fi
((total_checks++))

# Check Elasticsearch health
check_service "Elasticsearch Health" "curl -f http://localhost:9200/_cluster/health" ""
if [ $? -eq 0 ]; then
    ((passed_checks++))
fi
((total_checks++))

# Check Prometheus health
check_service "Prometheus Health" "curl -f http://localhost:9090/-/healthy" ""
if [ $? -eq 0 ]; then
    ((passed_checks++))
fi
((total_checks++))

# Check Grafana health
check_service "Grafana Health" "curl -f http://localhost:3000/api/health" ""
if [ $? -eq 0 ]; then
    ((passed_checks++))
fi
((total_checks++))

echo ""
echo "📈 Summary:"
echo "----------"
echo "Total checks: $total_checks"
echo "Passed: $passed_checks"
echo "Failed: $((total_checks - passed_checks))"

if [ $passed_checks -eq $total_checks ]; then
    echo -e "${GREEN}🎉 All services are healthy!${NC}"
    exit 0
else
    echo -e "${RED}⚠️  Some services are not healthy. Check the logs above.${NC}"
    echo ""
    echo "🔧 Troubleshooting:"
    echo "1. Check if Docker is running"
    echo "2. Run 'docker-compose logs' to see service logs"
    echo "3. Ensure ports are not in use by other applications"
    echo "4. Try 'docker-compose down && docker-compose up -d'"
    exit 1
fi 