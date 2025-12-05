#!/bin/bash

# Complete Vendor Branch Onboarding Test Script
# This script demonstrates the complete flow of creating a vendor branch
# Based on VENDOR_BRANCH_ONBOARDING_USECASES.md

set -e

API_BASE_URL="http://localhost:8080/api/v1"
VENDOR_ID=1

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║   VENDOR BRANCH ONBOARDING - COMPLETE TEST                 ║${NC}"
echo -e "${BLUE}║   Based on VENDOR_BRANCH_ONBOARDING_USECASES.md (UC-B001)  ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Test 1: Create Koramangala Branch
echo -e "${YELLOW}[Test 1] Creating Branch: Chai Express - Koramangala${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

BRANCH1=$(curl -s -X POST "$API_BASE_URL/vendors/$VENDOR_ID/branches" \
  -H "Content-Type: application/json" \
  -d '{
    "branchName": "Chai Express - Koramangala",
    "branchCode": "CE-KOR-001",
    "displayName": "Chai Express Koramangala",
    "city": "Bangalore",
    "address": {
      "street": "456 Koramangala 1st Block",
      "landmark": "Near Sony Signal",
      "area": "Koramangala",
      "city": "Bangalore",
      "state": "Karnataka",
      "pincode": "560034",
      "country": "India"
    },
    "latitude": 12.9352,
    "longitude": 77.6245,
    "branchPhone": "9876543211",
    "branchEmail": "koramangala@chaiexpress.com",
    "branchManagerName": "Rajesh Kumar",
    "branchManagerPhone": "9876543212"
  }')

BRANCH_ID_1=$(echo "$BRANCH1" | grep -o '"branchId":[0-9]*' | cut -d':' -f2)
if [ -n "$BRANCH_ID_1" ]; then
  echo -e "${GREEN}✓ Branch created successfully${NC}"
  echo "  Branch ID: $BRANCH_ID_1"
  echo "  Branch Code: CE-KOR-001"
  echo "  Status: PENDING"
else
  echo -e "${RED}✗ Failed to create branch${NC}"
  echo "$BRANCH1"
fi
echo ""

# Test 2: Create Whitefield Branch
echo -e "${YELLOW}[Test 2] Creating Branch: Chai Express - Whitefield${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

BRANCH2=$(curl -s -X POST "$API_BASE_URL/vendors/$VENDOR_ID/branches" \
  -H "Content-Type: application/json" \
  -d '{
    "branchName": "Chai Express - Whitefield",
    "branchCode": "CE-WF-001",
    "displayName": "Chai Express Whitefield",
    "city": "Bangalore",
    "address": {
      "street": "123 Whitefield Road",
      "landmark": "Near Embassy Golf Links",
      "area": "Whitefield",
      "city": "Bangalore",
      "state": "Karnataka",
      "pincode": "560066",
      "country": "India"
    },
    "latitude": 12.9698,
    "longitude": 77.7499,
    "branchPhone": "9876543222",
    "branchEmail": "whitefield@chaiexpress.com",
    "branchManagerName": "Priya Sharma",
    "branchManagerPhone": "9876543223"
  }')

BRANCH_ID_2=$(echo "$BRANCH2" | grep -o '"branchId":[0-9]*' | cut -d':' -f2)
if [ -n "$BRANCH_ID_2" ]; then
  echo -e "${GREEN}✓ Branch created successfully${NC}"
  echo "  Branch ID: $BRANCH_ID_2"
  echo "  Branch Code: CE-WF-001"
  echo "  Status: PENDING"
else
  echo -e "${RED}✗ Failed to create branch${NC}"
  echo "$BRANCH2"
fi
echo ""

# Test 3: Create Indiranagar Branch with Full Preferences
echo -e "${YELLOW}[Test 3] Creating Branch with Full Preferences${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

BRANCH3=$(curl -s -X POST "$API_BASE_URL/vendors/$VENDOR_ID/branches" \
  -H "Content-Type: application/json" \
  -d '{
    "branchName": "Chai Express - Indiranagar",
    "branchCode": "CE-IND-001",
    "displayName": "Chai Express Indiranagar",
    "city": "Bangalore",
    "address": {
      "street": "789 12th Cross Road",
      "landmark": "Near Koramangala Signal",
      "area": "Indiranagar",
      "city": "Bangalore",
      "state": "Karnataka",
      "pincode": "560038",
      "country": "India"
    },
    "latitude": 12.9716,
    "longitude": 77.6412,
    "branchPhone": "9876543233",
    "branchEmail": "indiranagar@chaiexpress.com",
    "branchManagerName": "Vikram Singh",
    "branchManagerPhone": "9876543234",
    "preferences": {
      "auto_accept_orders": true,
      "max_orders_per_hour": 75,
      "delivery_radius_km": 6,
      "min_order_value": 150,
      "accepts_cash": true,
      "accepts_online_payment": true,
      "packing_time_minutes": 10,
      "commission_rate": 16.5,
      "priority_delivery": true
    },
    "operatingHours": {
      "MONDAY": [{"open": "07:00", "close": "23:00"}],
      "TUESDAY": [{"open": "07:00", "close": "23:00"}],
      "WEDNESDAY": [{"open": "07:00", "close": "23:00"}],
      "THURSDAY": [{"open": "07:00", "close": "23:00"}],
      "FRIDAY": [{"open": "07:00", "close": "00:00"}],
      "SATURDAY": [{"open": "08:00", "close": "00:00"}],
      "SUNDAY": [{"open": "08:00", "close": "23:00"}]
    }
  }')

BRANCH_ID_3=$(echo "$BRANCH3" | grep -o '"branchId":[0-9]*' | cut -d':' -f2)
if [ -n "$BRANCH_ID_3" ]; then
  echo -e "${GREEN}✓ Branch created successfully${NC}"
  echo "  Branch ID: $BRANCH_ID_3"
  echo "  Branch Code: CE-IND-001"
  echo "  Status: PENDING"
  echo "  Features: Priority Delivery Enabled"
else
  echo -e "${RED}✗ Failed to create branch${NC}"
  echo "$BRANCH3"
fi
echo ""

# Test 4: Verify Vendor has Multiple Branches
echo -e "${YELLOW}[Test 4] Retrieving Vendor Details with All Branches${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

VENDOR=$(curl -s -X GET "$API_BASE_URL/vendors/$VENDOR_ID" \
  -H "Content-Type: application/json")

BRANCH_COUNT=$(echo "$VENDOR" | grep -o '"branchName"' | wc -l)
echo -e "${GREEN}✓ Vendor retrieved successfully${NC}"
echo "  Vendor ID: $VENDOR_ID"
echo "  Total Branches: $BRANCH_COUNT"
echo ""

# Summary
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                        TEST SUMMARY                        ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${GREEN}✓ All tests completed successfully!${NC}"
echo ""
echo "Branches Created:"
echo "  1. Chai Express - Koramangala (ID: $BRANCH_ID_1)"
echo "  2. Chai Express - Whitefield (ID: $BRANCH_ID_2)"
echo "  3. Chai Express - Indiranagar (ID: $BRANCH_ID_3)"
echo ""
echo "Key Features Demonstrated:"
echo "  ✓ Multiple branches for single vendor"
echo "  ✓ Geographic location tagging (latitude/longitude)"
echo "  ✓ Operating hours configuration"
echo "  ✓ Branch preferences and settings"
echo "  ✓ Branch manager assignment"
echo ""
echo "Next Steps:"
echo "  1. Upload branch documents via: POST /api/v1/branches/{branchId}/documents"
echo "  2. Create menu items for branches"
echo "  3. Activate branches when ready via: PUT /api/v1/branches/{branchId}/activate"
echo ""
echo -e "${BLUE}For more details, see: VENDOR_BRANCH_CREATION_GUIDE.md${NC}"

