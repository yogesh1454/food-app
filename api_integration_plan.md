# Implementation Plan: API Integration - Order Catalog & Delivery Management Service

## ✅ Goal
Integrate all **42 endpoints** from the *Order Catalog & Delivery Management Service API (v2.0.0)* into the **partner-frontend React Native application**.

This includes:
- Implementing missing endpoints
- Fixing incorrect implementations
- Creating **type-safe service clients**
- Full support for **FSM (Finite State Machine) logic**

---

## ❗ User Review Required

### 🔥 Breaking Changes
The following changes WILL break existing behavior:

- ❌ Deprecate/remove 5 incorrect methods in `vendorApiService.ts`
- ✅ Replace `ordersApiService.ts` placeholder with real implementation
- 🔐 Require header-based authentication for ALL API calls:
  - `X-Customer-Id`
  - `X-Rider-Id`
  - `X-Restaurant-Id`

---

## 📌 Scope Clarification
Since this is a **vendor app (partner-frontend)**, question:

Should we:
1. Implement ALL 42 endpoints (customer + rider + vendor)?
2. Or only vendor-specific?

### ✅ Recommendation
**Phase 1:** Vendor APIs only
- Vendor Orders
- Menu
- Branch

Optional later:
- Customer APIs
- Rider APIs

---

## ✅ Component 1: Type Definitions (`api.ts`)
Add full OpenAPI-matching types:

### Orders
- `OrderResponse` (13 FSM states)
- `OrderItemResponse`
- `DeliveryAddress`
- `AcceptOrderRequest`
- `RejectOrderRequest`
- `CancelOrderRequest`
- `CreateOrderFromCheckoutRequest`

### Delivery
- `DeliveryResponseDTO` (9 FSM states)
- `LocationDTO`
- `UpdateDeliveryStatusRequestDTO`
- `RejectDeliveryRequestDTO`

### Checkout
- `CheckoutRequest`
- `CheckoutResponse`
- `CommitCheckoutRequest`
- `CheckoutItem`
- `CartItemRequest`
- `PricingDetails`
- `DeliveryEstimate`
- `ValidationResults`

### Rider
- `RiderResponseDTO`
- `UpdateRiderRequestDTO`
- `EarningsDTO`
- `RiderInfoDTO`

### Customer
- `CustomerStatusResponseDTO`

### FSM Enums
- `OrderState` (13)
- `DeliveryState` (9)
- `PaymentStatus`

---

## ✅ Component 2: Service Clients

### ⚠️ Modify `vendorApiService.ts`
Remove/deprecate incorrect methods:
- `updateOperatingHours()`
- `getOperatingHours()`
- `checkBranchAvailability()`
- `uploadDocument()`
- `getBranchDocuments()`
- `getVendorBranches()`
- `deleteDocument()`
- `uploadImage()`
- `uploadDocumentFile()`

Keep valid methods:
- `getVendor()`
- `createVendor()`
- `updateVendor()`
- `createBranch()`
- `getBranch()`
- `updateBranch()`
- `toggleBranchStatus()`
- `uploadVendorFile()`

---

### ✅ NEW: `vendorOrdersApiService.ts`
Endpoints:
```
GET /api/v1/vendor/orders
POST /api/v1/vendor/orders/{orderId}/accept
POST /api/v1/vendor/orders/{orderId}/reject
POST /api/v1/vendor/orders/{orderId}/ready
```

Methods:
```
listPendingOrders()
acceptOrder()
rejectOrder()
markOrderReady()
```

---

### ✅ NEW: `ordersApiService.ts`
Endpoints:
```
GET /api/v1/orders
POST /api/v1/orders
GET /api/v1/orders/{orderId}
POST /api/v1/orders/{orderId}/cancel
```

---

### ✅ NEW: `checkoutApiService.ts`
Endpoints:
```
POST /api/v1/checkout/calculate
POST /api/v1/checkout/commit
GET /api/v1/checkout/session/{sessionId}
GET /api/v1/checkout/health
```

---

### ✅ NEW: `deliveryApiService.ts`
```
GET /api/v1/deliveries/{deliveryId}
GET /api/v1/orders/{orderId}/delivery
GET /api/v1/deliveries/{deliveryId}/location
```

---

### ➖ Rider APIs (optional)
- `riderOrdersApiService.ts`
- `riderDeliveriesApiService.ts`
- `riderStatusApiService.ts`

---

### ✅ NEW: `customerTrackingApiService.ts`
```
GET /api/v1/customers/{customerId}/orders/{orderId}/status
```

---

## ✅ Component 3: HTTP Client Enhancements

### Header Injection
Add to `httpClient.ts`:
```
setAuthHeaders(customerId?, riderId?, restaurantId?)
```

Inject:
- `X-Customer-Id`
- `X-Rider-Id`
- `X-Restaurant-Id`

Add ability to switch between:
- Header auth
- JWT auth

---

## ✅ Component 4: FSM Helpers (`fsmHelpers.ts`)
Add:
```
isOrderCancellable()
canAcceptOrder()
canRejectOrder()
canMarkOrderReady()
getNextValidOrderStates()

isDeliveryActive()
canUpdateDeliveryStatus()
getNextValidDeliveryStates()
```

---

## ✅ Component 5: Error Handling
Enhance `httpClient.ts`:
- FSM validation errors (400)
- Payment Failed (402)
- ValidationErrorResponse mapping

---

## ✅ Verification Plan

### ✅ Automated Tests
Unit test files:
```
src/core/api/__tests__/vendorOrdersApiService.test.ts
src/core/utils/__tests__/fsmHelpers.test.ts
```

---

## ✅ Manual Verification (Pending User Input)
Need answers for:

- Is backend running: `http://54.87.117.181:8080`?
- Test vendor ID?
- Test branch ID?
- Sample menu?
- Test orders?

Headers:
- `X-Restaurant-Id` value?
- `X-Customer-Id` value?

Testing preference:
- ✅ standalone script?
- ✅ UI updates?
- ✅ both?

---

## ✅ Proposed Manual Test Plan

### Vendor Management ✅
### Branch Management ✅
### Menu ✅

### NEW Vendor Orders
- Create order
- View pending
- Accept
- Mark ready
- Reject

### Checkout
- Calculate
- Commit
- Validate

### Delivery Tracking
- Track delivery
- Rider location

---

## ✅ Implementation Order
1. Type definitions
2. FSM helpers
3. HTTP client
4. Vendor Orders API
5. Cleanup vendorApiService
6. Orders & Checkout APIs
7. Delivery APIs
8. Rider APIs (optional)
9. Customer tracking API (optional)
10. Unit tests
11. Manual verification

---

## ✅ Status
Waiting for user input for manual testing details.

