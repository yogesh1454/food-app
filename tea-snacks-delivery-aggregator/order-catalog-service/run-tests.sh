#!/bin/bash
# Integration Test Runner for Order Catalog Service
# 
# Usage:
#   ./run-tests.sh              # Run all integration tests
#   ./run-tests.sh checkout     # Run CheckoutAPIIntegrationTest
#   ./run-tests.sh place-order  # Run PlaceOrderFromCheckoutIntegrationTest
#   ./run-tests.sh setup        # Setup Docker infrastructure

set -e  # Exit on error

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Project root
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo -e "${GREEN}═══════════════════════════════════════════════════${NC}"
echo -e "${GREEN}  Order Catalog Service - Integration Test Runner  ${NC}"
echo -e "${GREEN}═══════════════════════════════════════════════════${NC}"
echo ""

# Function to check Docker containers
check_docker() {
    echo -e "${YELLOW}[1/3] Checking Docker containers...${NC}"
    
    if ! docker-compose ps | grep -q "Up"; then
        echo -e "${RED}❌ Docker containers not running!${NC}"
        echo ""
        echo "Please start Docker containers first:"
        echo "  cd $PROJECT_ROOT"
        echo "  docker-compose up -d"
        echo ""
        echo "Or run: ./run-tests.sh setup"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Docker containers are running${NC}"
    docker-compose ps
    echo ""
}

# Function to setup Docker
setup_docker() {
    echo -e "${YELLOW}Setting up Docker infrastructure...${NC}"
    cd "$PROJECT_ROOT"
    
    docker-compose up -d
    
    echo ""
    echo -e "${GREEN}✓ Docker setup complete${NC}"
    echo ""
    docker-compose ps
    
    echo ""
    echo -e "${GREEN}Waiting 10 seconds for services to be ready...${NC}"
    sleep 10
    
    echo ""
    echo -e "${GREEN}✓ Ready to run tests${NC}"
    exit 0
}

# Function to run tests
run_tests() {
    local test_filter=$1
    local test_name=$2
    
    echo -e "${YELLOW}[2/3] Running integration tests...${NC}"
    echo "Test filter: $test_name"
    echo ""
    
    cd "$PROJECT_ROOT"
    
    # Run tests with output
    ./gradlew :order-catalog-service:test \
        --tests "$test_filter" \
        --info \
        2>&1 | tee test-output.log
    
    local exit_code=${PIPESTATUS[0]}
    
    echo ""
    echo -e "${YELLOW}[3/3] Test results${NC}"
    
    if [ $exit_code -eq 0 ]; then
        echo -e "${GREEN}✓ All tests passed!${NC}"
        
        # Show test report location
        echo ""
        echo "Test report available at:"
        echo "  $PROJECT_ROOT/order-catalog-service/build/reports/tests/test/index.html"
        
        return 0
    else
        echo -e "${RED}❌ Tests failed!${NC}"
        
        echo ""
        echo "Check the logs above for details."
        echo "Full log saved to: test-output.log"
        
        return 1
    fi
}

# Main script logic
case "${1:-all}" in
    setup)
        setup_docker
        ;;
    
    checkout)
        check_docker
        run_tests "CheckoutAPIIntegrationTest" "Checkout API Tests"
        ;;
    
    place-order)
        check_docker
        run_tests "PlaceOrderFromCheckoutIntegrationTest" "Place Order Tests"
        ;;
    
    all)
        check_docker
        run_tests "*IntegrationTest" "All Integration Tests"
        ;;
    
    *)
        echo "Usage: $0 [setup|checkout|place-order|all]"
        echo ""
        echo "Commands:"
        echo "  setup       - Setup Docker infrastructure"
        echo "  checkout    - Run Checkout API tests"
        echo "  place-order - Run Place Order tests"
        echo "  all         - Run all integration tests (default)"
        exit 1
        ;;
esac

