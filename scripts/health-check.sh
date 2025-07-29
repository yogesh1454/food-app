#!/bin/bash

# ANSI color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Counters
passed_checks=0
total_checks=0

# --- Helper Functions ---

# Checks if a command is available
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Generic check function
run_check() {
    local name="$1"
    local command="$2"
    total_checks=$((total_checks + 1))
    echo -n -e "${YELLOW}Checking ${name}...${NC}"
    if eval "$command" >/dev/null 2>&1; then
        echo -e "\r${GREEN}✓ ${name} is UP${NC}"
        passed_checks=$((passed_checks + 1))
    else
        echo -e "\r${RED}✗ ${name} is DOWN${NC}"
    fi
}

# --- Prerequisite Checks ---
echo "🔍 Running Prerequisite Checks..."

if ! command_exists docker-compose; then
    echo -e "${RED}Error: docker-compose is not installed. Please install it to continue.${NC}"
    exit 1
fi

if ! command_exists curl; then
    echo -e "${RED}Error: curl is not installed. Please install it to continue.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Prerequisites met.${NC}\n"

# --- Docker Container Status ---
echo "🐳 Checking Docker Container Status..."
if [ -f "infrastructure/docker/docker-compose.yml" ]; then
    cd infrastructure/docker || exit
    run_check "Docker Containers" "docker-compose ps | grep -q 'Up'"
    cd ../.. # Return to root
else
    echo -e "${RED}✗ docker-compose.yml not found in infrastructure/docker/. Skipping Docker checks.${NC}"
fi

echo ""

# --- Infrastructure Health ---
echo "📊 Checking Infrastructure Health..."
run_check "PostgreSQL" "nc -z localhost 5432"
if command_exists redis-cli;
    then run_check "Redis" "redis-cli -h localhost -p 6379 ping | grep -q PONG"
    else run_check "Redis" "(echo PING; sleep 1) | nc localhost 6379 | grep -q '+PONG'"
fi
run_check "Kafka" "nc -z localhost 9092"
run_check "Elasticsearch" "curl -s http://localhost:9200 | grep -q 'You Know, for Search'"
run_check "Prometheus" "curl -s http://localhost:9090/-/healthy | grep -q 'Prometheus Server is Healthy'"
run_check "Grafana" "curl -s http://localhost:3000/api/health | grep -q 'ok'"
echo ""

# --- Microservice Health ---
echo "🚀 Checking Microservice Health..."
run_check "User Management Service" "curl -s http://localhost:8081/actuator/health | grep -q '\"status\":\"UP\"'"
run_check "Order Catalog Service" "curl -s http://localhost:8082/actuator/health | grep -q '\"status\":\"UP\"'"
run_check "Payment Management Service" "curl -s http://localhost:8083/actuator/health | grep -q '\"status\":\"UP\"'"
run_check "Delivery Management Service" "curl -s http://localhost:8084/actuator/health | grep -q '\"status\":\"UP\"'"
run_check "Notification Service" "curl -s http://localhost:8085/actuator/health | grep -q '\"status\":\"UP\"'"
run_check "Search Discovery Service" "curl -s http://localhost:8086/actuator/health | grep -q '\"status\":\"UP\"'"
echo ""

# --- Summary --- 
echo "--- Health Check Summary ---"
if [ "$passed_checks" -eq "$total_checks" ]; then
    echo -e "${GREEN}✅ All $total_checks checks passed! The system is fully operational.${NC}"
else
    failed_checks=$((total_checks - passed_checks))
    echo -e "${RED}❌ $failed_checks out of $total_checks checks failed.${NC}"
    echo -e "${YELLOW}Please review the output above for details on the failed checks.${NC}"
    exit 1
fi

exit 0 