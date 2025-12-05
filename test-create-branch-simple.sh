#!/bin/bash

# Simple Vendor Branch Creation Test
# This script creates a new branch for an existing vendor

API_BASE_URL="http://localhost:8080/api/v1"
VENDOR_ID=1

echo "=== Creating New Vendor Branch ==="
echo "Vendor ID: $VENDOR_ID"
echo ""

# Generate unique branch code to avoid conflicts
BRANCH_CODE="TEST-BRANCH-$(date +%s)"

echo "Sending branch creation request..."
echo "Branch Code: $BRANCH_CODE"
echo ""

RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "$API_BASE_URL/vendors/$VENDOR_ID/branches" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 550e8400-e29b-41d4-a716-446655440000" \
  -d "{
    \"branchName\": \"Chai Express - Koramangala\",
    \"branchCode\": \"$BRANCH_CODE\",
    \"displayName\": \"Chai Express Koramangala\",
    \"city\": \"Bangalore\",
    \"address\": {
      \"street\": \"456 Koramangala 1st Block\",
      \"landmark\": \"Near Sony Signal\",
      \"area\": \"Koramangala\",
      \"city\": \"Bangalore\",
      \"state\": \"Karnataka\",
      \"pincode\": \"560034\",
      \"country\": \"India\"
    },
    \"latitude\": 12.9352,
    \"longitude\": 77.6245,
    \"branchPhone\": \"9876543211\",
    \"branchEmail\": \"koramangala@chaiexpress.com\",
    \"branchManagerName\": \"Rajesh Kumar\",
    \"branchManagerPhone\": \"9876543212\",
    \"preferences\": {
      \"auto_accept_orders\": true,
      \"max_orders_per_hour\": 60,
      \"delivery_radius_km\": 5,
      \"min_order_value\": 100,
      \"accepts_cash\": true,
      \"accepts_online_payment\": true,
      \"packing_time_minutes\": 15,
      \"commission_rate\": 18.0,
      \"priority_delivery\": false
    },
    \"operatingHours\": {
      \"MONDAY\": [{\"open\": \"08:00\", \"close\": \"22:00\"}],
      \"TUESDAY\": [{\"open\": \"08:00\", \"close\": \"22:00\"}],
      \"WEDNESDAY\": [{\"open\": \"08:00\", \"close\": \"22:00\"}],
      \"THURSDAY\": [{\"open\": \"08:00\", \"close\": \"22:00\"}],
      \"FRIDAY\": [{\"open\": \"08:00\", \"close\": \"23:00\"}],
      \"SATURDAY\": [{\"open\": \"09:00\", \"close\": \"23:00\"}],
      \"SUNDAY\": [{\"open\": \"09:00\", \"close\": \"22:00\"}]
    }
  }")

# Extract HTTP status
HTTP_STATUS=$(echo "$RESPONSE" | grep "HTTP_STATUS" | cut -d':' -f2)
BODY=$(echo "$RESPONSE" | sed '$d')

echo "HTTP Status: $HTTP_STATUS"
echo ""
echo "Response Body:"
echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
echo ""

if [ "$HTTP_STATUS" = "201" ]; then
  echo "✓ SUCCESS: Branch created successfully!"
  BRANCH_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
  echo "Branch ID: $BRANCH_ID"
else
  echo "✗ FAILED: Branch creation failed with HTTP status $HTTP_STATUS"
fi

