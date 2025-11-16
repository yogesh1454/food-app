# Checkout API Design - Pre-Order Validation & Session Management

**Document Version:** 1.0  
**Last Updated:** November 11, 2025  
**Epic:** Epic-4 - Order & Delivery Management  
**Status:** Draft

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Business Requirements](#business-requirements)
3. [API Design](#api-design)
4. [Checkout Flow](#checkout-flow)
5. [Price Calculation Logic](#price-calculation-logic)
6. [Session Management](#session-management)
7. [Integration Points](#integration-points)
8. [Error Handling](#error-handling)
9. [Implementation Guidelines](#implementation-guidelines)

---

## Executive Summary

### Purpose

The Checkout API serves as a **pre-order validation and cost calculation layer** that sits between the cart and order creation. It ensures that all items are available, prices are current, and provides users with a final cost breakdown before committing to an order.

### Key Features

- ✅ **Idempotent** - Safe to call multiple times
- ✅ **Stateless Calculation** - No side effects on inventory
- ✅ **Session-based** - Creates temporary checkout session
- ✅ **Real-time Validation** - Verifies stock and prices
- ✅ **Cost Transparency** - Complete breakdown of charges

### Business Value

1. **Reduced Order Failures** - Catch issues before order creation
2. **Price Transparency** - Users see exact costs upfront
3. **Better UX** - Fast, repeatable calculations
4. **Fraud Prevention** - Validate before payment
5. **Inventory Accuracy** - Real-time stock checks

---

## Business Requirements

### Functional Requirements

#### FR-1: Cart Validation
- **Requirement:** Validate all items exist and are available
- **Priority:** P0 (Critical)
- **Acceptance Criteria:**
  - All menu item IDs must exist in vendor's menu
  - All items must be in stock (quantity available)
  - Vendor branch must be active and accepting orders
  - Items must belong to the specified vendor branch

#### FR-2: Price Calculation
- **Requirement:** Calculate accurate final price with breakdown
- **Priority:** P0 (Critical)
- **Acceptance Criteria:**
  - Use current menu prices (not cart prices)
  - Apply valid discount codes
  - Calculate delivery fee based on distance/zone
  - Include platform fee and taxes
  - Provide itemized breakdown

#### FR-3: Session Creation
- **Requirement:** Create temporary checkout session
- **Priority:** P0 (Critical)
- **Acceptance Criteria:**
  - Generate unique session ID
  - Store session for 15 minutes
  - Session contains all validated data
  - Session is idempotent (same input = same session)

#### FR-4: Delivery Zone Validation
- **Requirement:** Verify delivery address is serviceable
- **Priority:** P0 (Critical)
- **Acceptance Criteria:**
  - Check if pincode is in delivery zone
  - Calculate delivery distance
  - Determine delivery fee tier
  - Validate address completeness

---

## API Design

### Endpoint

```
POST /api/v1/checkout/calculate
```

### Request Body

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "vendorBranchId": 123,
  "deliveryAddress": {
    "addressLine1": "123 Main Street",
    "city": "Mumbai",
    "state": "Maharashtra",
    "pincode": "400001"
  },
  "items": [
    {
      "menuItemId": 456,
      "quantity": 2,
      "customizations": {}
    }
  ],
  "paymentMethod": "UPI",
  "couponCode": "FIRST50"
}
```

### Response (Success)

```json
{
  "checkoutSessionId": "chk_7a8b9c0d1e2f3g4h5i6j",
  "status": "READY_FOR_COMMIT",
  "expiresAt": "2025-11-11T17:45:00Z",
  "items": [
    {
      "menuItemId": 456,
      "name": "Masala Chai",
      "quantity": 2,
      "unitPrice": 30.00,
      "subtotal": 60.00
    }
  ],
  "pricing": {
    "itemTotal": 60.00,
    "discount": 30.00,
    "deliveryCharges": 25.00,
    "platformFee": 5.00,
    "gst": 3.00,
    "totalAmount": 63.00
  }
}
```

---

## Checkout Flow

### Step-by-Step Process

#### Step 1: Input Validation
**Responsibility:** Checkout Service  
**Duration:** ~10ms

**Actions:**
- Validate JWT token and extract user ID
- Check required fields
- Validate data types and formats

**Success Output:** Validated request

---

#### Step 2: Cart & Price Check
**Responsibility:** Menu/Vendor Domain  
**Duration:** ~100-200ms

**Actions:**
- Call `MenuService.validateCartItems(vendorBranchId, items)`
- Verify all item IDs exist
- Check stock availability
- Get current prices

**Success Output:** Confirmed Price List

**API Call:**
```java
MenuValidationResponse validateCartItems(
    Long vendorBranchId,
    List<CartItemRequest> items
);
```

---

#### Step 3: Final Calculation
**Responsibility:** Checkout Service  
**Duration:** ~10ms

**Actions:**
1. Calculate subtotal: `Σ(item.price × item.quantity)`
2. Apply discount (if coupon valid)
3. Calculate delivery fee
4. Add platform fee
5. Calculate GST
6. Determine final total

**Calculation Formula:**
```
itemTotal = Σ(price × quantity)
discount = min(itemTotal × discountRate, maxDiscount)
subtotalAfterDiscount = itemTotal - discount
deliveryCharges = baseFee + distanceFee
platformFee = subtotalAfterDiscount × platformFeeRate
taxableAmount = subtotalAfterDiscount + deliveryCharges + platformFee
gst = taxableAmount × gstRate
totalAmount = taxableAmount + gst
```

**Success Output:** Final Total

---

#### Step 4: Session Persistence
**Responsibility:** Checkout Service  
**Duration:** ~20-50ms

**Actions:**
1. Generate unique session ID
2. Store checkout data in Redis (15 min TTL)
3. Set status: `READY_FOR_COMMIT`
4. Return session ID

**Success Output:** checkoutSessionId

**Session Data:**
```json
{
  "checkoutSessionId": "chk_...",
  "status": "READY_FOR_COMMIT",
  "userId": "...",
  "vendorBranchId": 123,
  "items": [...],
  "pricing": {...},
  "expiresAt": "..."
}
```

---

## Price Calculation Logic

### Component Breakdown

#### 1. Item Total
```java
BigDecimal calculateItemTotal(List<CheckoutItem> items) {
    return items.stream()
        .map(item -> item.getUnitPrice()
            .multiply(BigDecimal.valueOf(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
}
```

#### 2. Discount Calculation
```java
BigDecimal discount = itemTotal
    .multiply(BigDecimal.valueOf(discountPercentage / 100.0))
    .min(maxDiscountCap);
```

#### 3. Delivery Fee
```java
BigDecimal baseFee = new BigDecimal("20.00");
BigDecimal perKmFee = new BigDecimal("5.00");
BigDecimal deliveryFee = baseFee.add(
    BigDecimal.valueOf(distance).multiply(perKmFee)
);
```

#### 4. Platform Fee (5%)
```java
BigDecimal platformFee = subtotalAfterDiscount
    .add(deliveryCharges)
    .multiply(new BigDecimal("0.05"))
    .setScale(2, RoundingMode.HALF_UP);
```

#### 5. GST (5%)
```java
BigDecimal taxableAmount = subtotalAfterDiscount
    .add(deliveryCharges)
    .add(platformFee);
BigDecimal gst = taxableAmount
    .multiply(new BigDecimal("0.05"))
    .setScale(2, RoundingMode.HALF_UP);
```

---

## Session Management

### Session Lifecycle

```
READY_FOR_COMMIT (15 min) ──▶ COMMITTED (Order Created)
         │                           │
         │                           ▼
         │                    [Session Deleted]
         │
         ▼ (Timeout)
    EXPIRED (Auto-deleted)
```

### Redis Storage

**Key Pattern:** `checkout:session:{sessionId}`  
**TTL:** 15 minutes (900 seconds)

### Idempotency

**Key Generation:**
```java
String generateIdempotencyKey(CheckoutRequest request) {
    String input = request.getUserId() + 
                   request.getVendorBranchId() +
                   request.getItems().toString();
    return "chk_" + SHA256(input).substring(0, 20);
}
```

---

## Integration Points

### 1. Menu/Vendor Domain

**Service:** `MenuService`

**Methods:**
```java
MenuValidationResponse validateCartItems(
    Long vendorBranchId,
    List<CartItemRequest> items
);

VendorBranchResponse getVendorBranch(Long branchId);

boolean isAcceptingOrders(Long branchId);
```

### 2. Promotion Service

**Service:** `PromotionService`

**Methods:**
```java
DiscountResponse validateAndApplyCoupon(
    String couponCode,
    UUID userId,
    BigDecimal itemTotal
);
```

---

## Error Handling

### Error Categories

#### Validation Errors (400)
- `MISSING_REQUIRED_FIELD`
- `INVALID_DATA_TYPE`
- `EMPTY_CART`

#### Business Logic Errors (422)
- `ITEM_NOT_FOUND`
- `ITEM_OUT_OF_STOCK`
- `DELIVERY_ZONE_NOT_SERVICEABLE`
- `INVALID_COUPON_CODE`
- `VENDOR_NOT_ACCEPTING_ORDERS`

#### System Errors (500, 503)
- `INTERNAL_SERVER_ERROR`
- `SERVICE_UNAVAILABLE`

### Error Response Format

```json
{
  "checkoutSessionId": null,
  "status": "VALIDATION_FAILED",
  "errors": [
    {
      "code": "ITEM_OUT_OF_STOCK",
      "message": "Masala Chai is currently out of stock",
      "field": "items[0].menuItemId"
    }
  ]
}
```

---

## Implementation Guidelines

### Service Structure

```
checkout-service/
├── controller/
│   └── CheckoutController.java
├── service/
│   ├── CheckoutService.java
│   ├── PriceCalculationService.java
│   └── SessionManagementService.java
├── dto/
│   ├── CheckoutRequest.java
│   ├── CheckoutResponse.java
│   └── CheckoutSession.java
├── integration/
│   ├── MenuServiceClient.java
│   └── PromotionServiceClient.java
└── repository/
    └── CheckoutSessionRepository.java (Redis)
```

### Key Classes

#### CheckoutService
```java
@Service
@Slf4j
@RequiredArgsConstructor
public class CheckoutService {
    
    private final MenuServiceClient menuClient;
    private final PromotionServiceClient promotionClient;
    private final PriceCalculationService priceCalculator;
    private final SessionManagementService sessionManager;
    
    @Transactional
    public CheckoutResponse calculateCheckout(CheckoutRequest request) {
        // Step 1: Validate input
        validateRequest(request);
        
        // Step 2: Validate cart items
        MenuValidationResponse menuValidation = 
            menuClient.validateCartItems(request);
        
        // Step 3: Calculate pricing
        PricingDetails pricing = 
            priceCalculator.calculate(menuValidation, request);
        
        // Step 4: Create session
        String sessionId = sessionManager.createSession(
            request, menuValidation, pricing
        );
        
        return buildResponse(sessionId, menuValidation, pricing);
    }
}
```

---

## Summary

The Checkout API provides a **critical validation layer** before order creation:

1. ✅ **Validates** all items are available
2. ✅ **Calculates** accurate pricing with breakdown
3. ✅ **Creates** temporary session for commitment
4. ✅ **Prevents** order failures and price discrepancies

**Next Steps:**
1. Implement CheckoutService
2. Integrate with Menu/Vendor Domain
3. Add Redis session management
4. Create comprehensive tests
5. Deploy and monitor

---

**Document Owner:** Architecture Team  
**Review Cycle:** Quarterly  
**Next Review:** February 2026
