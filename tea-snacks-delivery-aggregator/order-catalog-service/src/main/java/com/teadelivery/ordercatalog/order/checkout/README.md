# Checkout API

## Overview
The Checkout API provides a session-based checkout flow with real-time validation, transparent pricing calculation, and idempotent operations. It follows the design specified in `docs/business-flows/07_CHECKOUT_API_DESIGN.md`.

**Note:** The checkout component is part of the order domain and is located at `order/checkout/`.

## Architecture

### Components
```
order/checkout/
├── controller/
│   └── CheckoutController.java          # REST endpoints
├── service/
│   ├── CheckoutService.java             # Main orchestration
│   ├── PriceCalculationService.java     # Pricing logic
│   └── SessionManagementService.java    # Redis session management
├── dto/
│   ├── CheckoutRequest.java             # Request DTO
│   ├── CheckoutResponse.java            # Response DTO
│   └── CommitCheckoutRequest.java       # Commit request DTO
├── model/
│   └── CheckoutSession.java             # Redis session model
└── exception/
    └── CheckoutException.java           # Custom exception
```

## API Endpoints

### 1. Calculate Checkout
**POST** `/api/v1/checkout/calculate`

Creates or retrieves a checkout session with pricing calculation and validation.

**Request:**
```json
{
  "userId": "uuid",
  "vendorBranchId": 123,
  "deliveryAddress": {
    "addressLine1": "123 Main St",
    "city": "Mumbai",
    "state": "Maharashtra",
    "pincode": "400001"
  },
  "deliveryLocation": {
    "latitude": 19.0760,
    "longitude": 72.8777
  },
  "items": [
    {
      "menuItemId": 456,
      "quantity": 2,
      "customizations": {},
      "specialInstructions": "Extra spicy"
    }
  ],
  "paymentMethod": "UPI",
  "couponCode": "SAVE50",
  "contactlessDelivery": true,
  "deliveryInstructions": "Ring the bell"
}
```

**Response:**
```json
{
  "checkoutSessionId": "chk_1234567890_abcdef",
  "status": "READY_FOR_COMMIT",
  "expiresAt": "2024-01-15T10:30:00",
  "vendor": {
    "vendorId": "123",
    "vendorName": "Tea House",
    "vendorBranchId": 123,
    "branchName": "Andheri Branch",
    "estimatedPrepTime": 25,
    "isAcceptingOrders": true
  },
  "items": [
    {
      "menuItemId": 456,
      "name": "Masala Chai",
      "quantity": 2,
      "unitPrice": 50.00,
      "subtotal": 100.00,
      "isAvailable": true
    }
  ],
  "pricing": {
    "itemTotal": 100.00,
    "discount": 50.00,
    "discountDetails": {
      "couponCode": "SAVE50",
      "discountType": "PERCENTAGE",
      "discountValue": 50,
      "appliedDiscount": 50.00
    },
    "subtotalAfterDiscount": 50.00,
    "deliveryCharges": 37.50,
    "platformFee": 4.38,
    "gst": 4.59,
    "gstDetails": {
      "cgst": 2.30,
      "sgst": 2.29,
      "gstRate": 5
    },
    "totalAmount": 96.47,
    "currency": "INR"
  },
  "deliveryEstimate": {
    "estimatedDeliveryTime": "2024-01-15T11:15:00",
    "estimatedPrepTime": 25,
    "estimatedDeliveryDuration": 20,
    "totalEstimatedTime": 45
  },
  "validations": {
    "allItemsAvailable": true,
    "deliveryAddressValid": true,
    "deliveryZoneServiceable": true,
    "vendorAcceptingOrders": true,
    "paymentMethodSupported": true
  }
}
```

### 2. Get Checkout Session
**GET** `/api/v1/checkout/session/{sessionId}`

Retrieves an existing checkout session.

**Response:** Same as Calculate Checkout

### 3. Health Check
**GET** `/api/v1/checkout/health`

Returns service health status.

## Features

### 1. Idempotency
- Same cart + address + vendor = Same session ID
- Prevents duplicate calculations
- Session reuse within TTL window

### 2. Session Management
- **TTL:** 15 minutes for active sessions
- **Storage:** Redis with JSON serialization
- **Session ID Format:** `chk_{timestamp}_{hash}`
- **Hash Based On:** userId + vendorBranchId + items + deliveryAddress

### 3. Price Calculation
```
Item Total = Σ(item.price × item.quantity)
Discount = Applied coupon discount
Subtotal After Discount = Item Total - Discount
Delivery Charges = Base Fee + (Distance × Per KM Fee)
Platform Fee = (Subtotal + Delivery) × 5%
GST = (Subtotal + Delivery + Platform Fee) × 5%
Total Amount = Subtotal + Delivery + Platform Fee + GST
```

**Current Rates:**
- Base Delivery Fee: ₹20.00
- Per KM Fee: ₹5.00
- Platform Fee: 5%
- GST: 5% (split as 2.5% CGST + 2.5% SGST)

### 4. Validation
- ✅ Vendor branch exists and is active
- ✅ All menu items belong to the vendor
- ✅ All items are available
- ✅ Stock availability (TODO: integrate with inventory)
- ✅ Delivery address is valid
- ✅ Delivery zone is serviceable (TODO: integrate with delivery service)

### 5. Error Handling
**Error Response:**
```json
{
  "checkoutSessionId": null,
  "status": "VALIDATION_FAILED",
  "errors": [
    {
      "code": "ITEM_NOT_AVAILABLE",
      "message": "Masala Chai is currently not available",
      "field": "items[].menuItemId",
      "severity": "ERROR",
      "metadata": {
        "itemId": 456,
        "itemName": "Masala Chai"
      }
    }
  ]
}
```

**Error Codes:**
- `VENDOR_NOT_FOUND` - Vendor branch not found or inactive
- `ITEM_NOT_FROM_VENDOR` - Item doesn't belong to selected vendor
- `ITEM_NOT_AVAILABLE` - Item is not available
- `ITEM_OUT_OF_STOCK` - Insufficient stock
- `ITEM_VALIDATION_ERROR` - General validation error

## Integration Points

### Current Integrations
1. **MenuService** - Fetch menu items and prices
2. **VendorBranchRepository** - Validate vendor branches
3. **Redis** - Session storage

### Pending Integrations (TODO)
1. **PromotionService** - Validate and apply coupons
2. **DeliveryZoneService** - Calculate delivery fees and validate serviceability
3. **InventoryService** - Real-time stock validation
4. **OrderService** - Commit checkout to create order

## Usage Example

### Step 1: Calculate Checkout
```bash
curl -X POST http://localhost:8080/api/v1/checkout/calculate \
  -H "Content-Type: application/json" \
  -H "X-User-Id: user-uuid" \
  -d '{
    "userId": "user-uuid",
    "vendorBranchId": 123,
    "deliveryAddress": {...},
    "items": [...],
    "paymentMethod": "UPI"
  }'
```

### Step 2: Review Checkout
```bash
curl -X GET http://localhost:8080/api/v1/checkout/session/chk_1234567890_abcdef
```

### Step 3: Commit to Order
```bash
# TODO: Implement commit endpoint
curl -X POST http://localhost:8080/api/v1/checkout/commit \
  -H "Content-Type: application/json" \
  -d '{
    "checkoutSessionId": "chk_1234567890_abcdef"
  }'
```

## Configuration

### Redis Configuration
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms
```

### Application Properties
```properties
# Checkout Configuration
checkout.session.ttl=15m
checkout.platform.fee.rate=0.05
checkout.gst.rate=0.05
checkout.delivery.base.fee=20.00
checkout.delivery.per.km.fee=5.00
```

## Testing

### Unit Tests
- `PriceCalculationServiceTest` - Test pricing logic
- `SessionManagementServiceTest` - Test session operations
- `CheckoutServiceTest` - Test checkout flow

### Integration Tests
- `CheckoutControllerIntegrationTest` - Test API endpoints
- `CheckoutFlowIntegrationTest` - Test end-to-end flow

## Future Enhancements

1. **Dynamic Pricing**
   - Surge pricing during peak hours
   - Distance-based delivery tiers
   - Vendor-specific platform fees

2. **Advanced Validations**
   - Minimum order value checks
   - Maximum cart size limits
   - Delivery time slot validation

3. **Promotions**
   - Multiple coupon support
   - Auto-apply best offer
   - Loyalty points integration

4. **Analytics**
   - Checkout abandonment tracking
   - Price sensitivity analysis
   - Conversion funnel metrics

## Monitoring

### Key Metrics
- Checkout calculation latency
- Session creation rate
- Validation failure rate
- Cache hit/miss ratio
- Redis connection pool stats

### Logs
- All checkout calculations logged with session ID
- Validation failures logged with error details
- Session operations logged for audit trail

## Security

- ✅ Input validation using Jakarta Bean Validation
- ✅ SQL injection prevention via JPA
- ⚠️ TODO: JWT token validation for userId
- ⚠️ TODO: Rate limiting per user
- ⚠️ TODO: CSRF protection for state-changing operations
