# Vendor Branch Onboarding - API Usage Guide

## Overview

This guide demonstrates how to insert a new vendor branch into the order-catalog-service using the REST API, based on the `VENDOR_BRANCH_ONBOARDING_USECASES.md` specification.

## Prerequisites

1. **Order Catalog Service Running**: The service should be running on `http://localhost:8080`
2. **SSH Tunnel**: Ensure the SSH tunnel to AWS RDS is active:
   ```bash
   cd /Users/yogesh/Documents/ws/food-app
   bash connect-aws-tunnel.sh &
   ```
3. **Valid Vendor ID**: You need an existing vendor ID. The default test vendor ID is `1`.

## API Endpoint

### Create a New Branch

**Endpoint**: `POST /api/v1/vendors/{vendorId}/branches`

**Base URL**: `http://localhost:8080`

**Headers**:
- `Content-Type: application/json`
- `X-User-Id: 550e8400-e29b-41d4-a716-446655440000` (optional, for testing)

## Request Body Structure

```json
{
  "branchName": "String (required) - Full branch name",
  "branchCode": "String (optional) - Unique branch identifier",
  "displayName": "String (optional) - Display name for UI",
  "city": "String (required) - City name",
  "address": {
    "street": "String - Street address",
    "landmark": "String - Nearby landmark",
    "area": "String - Area/locality",
    "city": "String - City name",
    "state": "String - State name",
    "pincode": "String - PIN/Postal code",
    "country": "String - Country name"
  },
  "latitude": "Double (optional) - Geographic latitude",
  "longitude": "Double (optional) - Geographic longitude",
  "branchPhone": "String (optional) - Contact phone number",
  "branchEmail": "String (optional) - Contact email address",
  "branchManagerName": "String (optional) - Manager name",
  "branchManagerPhone": "String (optional) - Manager phone",
  "preferences": {
    "auto_accept_orders": "Boolean - Auto accept orders (default: false)",
    "max_orders_per_hour": "Integer - Maximum orders per hour (default: 50)",
    "delivery_radius_km": "Double - Delivery radius in KM (default: 5.0)",
    "min_order_value": "Integer - Minimum order value (default: 100)",
    "accepts_cash": "Boolean - Accept cash payments (default: true)",
    "accepts_online_payment": "Boolean - Accept online payments (default: true)",
    "packing_time_minutes": "Integer - Packing time in minutes (default: 15)",
    "commission_rate": "Double - Commission rate percentage (default: 18.0)",
    "priority_delivery": "Boolean - Priority delivery (default: false)"
  },
  "operatingHours": {
    "MONDAY": [{"open": "HH:MM", "close": "HH:MM"}],
    "TUESDAY": [{"open": "HH:MM", "close": "HH:MM"}],
    "WEDNESDAY": [{"open": "HH:MM", "close": "HH:MM"}],
    "THURSDAY": [{"open": "HH:MM", "close": "HH:MM"}],
    "FRIDAY": [{"open": "HH:MM", "close": "HH:MM"}],
    "SATURDAY": [{"open": "HH:MM", "close": "HH:MM"}],
    "SUNDAY": [{"open": "HH:MM", "close": "HH:MM"}]
  }
}
```

## Example Requests

### Example 1: Minimal Request (Only Required Fields)

```bash
curl -X POST "http://localhost:8080/api/v1/vendors/1/branches" \
  -H "Content-Type: application/json" \
  -d '{
    "branchName": "Chai Express - Koramangala",
    "branchCode": "CE-KOR-001",
    "city": "Bangalore",
    "address": {
      "street": "456 Koramangala 1st Block",
      "area": "Koramangala",
      "city": "Bangalore",
      "state": "Karnataka",
      "pincode": "560034"
    }
  }'
```

### Example 2: Complete Request (All Fields)

```bash
curl -X POST "http://localhost:8080/api/v1/vendors/1/branches" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 550e8400-e29b-41d4-a716-446655440000" \
  -d '{
    "branchName": "Chai Express - Whitefield",
    "branchCode": "CE-WF-2025",
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
    "branchManagerPhone": "9876543223",
    "preferences": {
      "auto_accept_orders": true,
      "max_orders_per_hour": 60,
      "delivery_radius_km": 5,
      "min_order_value": 100,
      "accepts_cash": true,
      "accepts_online_payment": true,
      "packing_time_minutes": 15,
      "commission_rate": 18.0,
      "priority_delivery": false
    },
    "operatingHours": {
      "MONDAY": [{"open": "08:00", "close": "22:00"}],
      "TUESDAY": [{"open": "08:00", "close": "22:00"}],
      "WEDNESDAY": [{"open": "08:00", "close": "22:00"}],
      "THURSDAY": [{"open": "08:00", "close": "22:00"}],
      "FRIDAY": [{"open": "08:00", "close": "23:00"}],
      "SATURDAY": [{"open": "09:00", "close": "23:00"}],
      "SUNDAY": [{"open": "09:00", "close": "22:00"}]
    }
  }'
```

## Success Response

### HTTP Status: 201 Created

```json
{
  "branchId": 1,
  "vendorId": 1,
  "branchName": "Chai Express - Koramangala",
  "branchCode": "CE-KOR-2025",
  "displayName": "Chai Express Koramangala",
  "address": {
    "street": "456 Koramangala 1st Block",
    "area": "Koramangala",
    "city": "Bangalore",
    "state": "Karnataka",
    "pincode": "560034"
  },
  "latitude": 12.9352,
  "longitude": 77.6245,
  "city": "Bangalore",
  "branchPhone": "9876543211",
  "branchEmail": "koramangala@chaiexpress.com",
  "branchManagerName": "Rajesh Kumar",
  "onboardingStatus": "PENDING",
  "isActive": false,
  "isOpen": false,
  "preferences": {
    "acceptsCash": true,
    "commissionRate": 18.0,
    "autoAcceptOrders": false,
    "deliveryRadiusKm": 5.0,
    "packingTimeMinutes": 15,
    "maxOrdersPerHour": 50,
    "acceptsOnlinePayment": true,
    "minOrderValue": 100
  },
  "operatingHours": {
    "MONDAY": {"open": "08:00", "close": "22:00"},
    "TUESDAY": {"open": "08:00", "close": "22:00"},
    "WEDNESDAY": {"open": "08:00", "close": "22:00"},
    "THURSDAY": {"open": "08:00", "close": "22:00"},
    "FRIDAY": {"open": "08:00", "close": "23:00"},
    "SATURDAY": {"open": "09:00", "close": "23:00"},
    "SUNDAY": {"open": "09:00", "close": "22:00"}
  },
  "images": {},
  "metadata": {},
  "createdAt": "2025-12-04T22:25:45.123456",
  "updatedAt": "2025-12-04T22:25:45.123456"
}
```

## Error Responses

### 400 Bad Request - Invalid Input

```json
{
  "timestamp": "2025-12-04T22:25:45.123456",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/vendors/1/branches",
  "validationErrors": {
    "branchName": "must not be blank",
    "city": "must not be blank"
  }
}
```

### 404 Not Found - Vendor Not Found

```json
{
  "timestamp": "2025-12-04T22:25:45.123456",
  "status": 404,
  "error": "Not Found",
  "message": "Vendor not found with ID: 999",
  "path": "/api/v1/vendors/999/branches"
}
```

### 409 Conflict - Branch Code Already Exists

```json
{
  "timestamp": "2025-12-04T22:25:45.123456",
  "status": 409,
  "error": "Conflict",
  "message": "Branch code CE-KOR-2025 already exists for this vendor",
  "path": "/api/v1/vendors/1/branches"
}
```

## Response Fields Explanation

| Field | Type | Description |
|-------|------|-------------|
| `branchId` | Long | Unique identifier for the branch |
| `vendorId` | Long | Associated vendor ID |
| `branchName` | String | Full name of the branch |
| `branchCode` | String | Unique code for the branch |
| `onboardingStatus` | String | Current onboarding status (PENDING, APPROVED, REJECTED, ACTIVE) |
| `isActive` | Boolean | Whether branch is active |
| `isOpen` | Boolean | Whether branch is currently open |
| `preferences` | Object | Branch preferences and settings |
| `operatingHours` | Map | Operating hours by day of week |
| `createdAt` | Timestamp | Branch creation timestamp |
| `updatedAt` | Timestamp | Last update timestamp |

## Branch Lifecycle

1. **PENDING** - Branch created, awaiting verification and document upload
2. **APPROVED** - Branch verified and approved by admin
3. **ACTIVE** - Branch is fully operational
4. **INACTIVE** - Branch is temporarily closed

## Next Steps After Branch Creation

1. **Upload Documents**: Upload business registration, tax certificates, etc.
2. **Set Menu**: Create and configure the menu items
3. **Activate Branch**: Once approved, activate the branch to start accepting orders
4. **Configure Operating Hours**: Fine-tune the operating hours as needed

## Testing the API

### Quick Test Script

```bash
#!/bin/bash

VENDOR_ID=1
API_URL="http://localhost:8080/api/v1/vendors/$VENDOR_ID/branches"

curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "branchName": "Test Branch",
    "branchCode": "TEST-'$(date +%s)'",
    "city": "Bangalore",
    "address": {
      "street": "Test Street",
      "city": "Bangalore",
      "state": "Karnataka",
      "pincode": "560001"
    }
  }' | python3 -m json.tool
```

## References

- Full specification: `VENDOR_BRANCH_ONBOARDING_USECASES.md` (UC-B001)
- Order Catalog Service: Running on `http://localhost:8080`
- API Documentation: Available at `http://localhost:8080/swagger-ui.html`

