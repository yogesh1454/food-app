# Create Order API - Comprehensive Requirements

**Document Version:** 1.0  
**Last Updated:** November 11, 2025  
**Epic:** Epic-4 - Order & Delivery Management  
**Status:** Active  
**Related Documents:**
- `02_ORDER_FSM_DESIGN.md` - Order FSM Design
- `07_CHECKOUT_API_DESIGN.md` - Checkout API Design
- `payment.md` - Payment Integration Requirements

---

## Table of Contents

1. [Overview](#overview)
2. [API Specification](#api-specification)
3. [Two-Step Checkout Flow](#two-step-checkout-flow)
4. [Detailed Business Logic](#detailed-business-logic)
5. [Payment Integration](#payment-integration)
6. [Error Handling](#error-handling)
7. [Implementation Requirements](#implementation-requirements)

---

## Overview

### Purpose

The Create Order API (`POST /api/v1/orders`) is the **second and transactional step** of a two-step checkout flow. It takes a validated and calculated checkout session, executes payment, and creates a persistent order record.

This is a **mission-critical, idempotent, atomic transaction** that must handle:
- Session locking and validation
- Payment execution (Wallet, GPay, COD)
- Order persistence
- Event publishing
- Comprehensive error handling

### Key Characteristics

| Characteristic | Description |
|----------------|-------------|
| **Idempotency** | Multiple calls with same session should not create duplicate orders |
| **Atomicity** | All steps must succeed or fail together |
| **Transactional** | Payment and order creation must be atomic |
| **Event-Driven** | Publishes events for downstream processing |
| **Auditable** | Complete audit trail of all actions |

---

## API Specification

### Endpoint

```
POST /api/v1/orders
```

### Request Body

```json
{
  "checkoutSessionId": "chk_1699876543210_abc123def456",
  "paymentToken": "tok_gpay_xyz789"  // Required only for GPay
}
```

| Field | Type | Required | Description | Constraints |
|-------|------|----------|-------------|-------------|
| `checkoutSessionId` | String | Yes | ID of the checkout session from Step 1 | Must exist and be in `READY_FOR_COMMIT` state |
| `paymentToken` | String | Conditional | Payment token for GPay transactions | Required if payment method is GPay |

### Success Response (HTTP 201 Created)

```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "orderNumber": "ORD-20251111-001",
  "status": "PENDING_ACCEPTANCE",
  "paymentStatus": "PAID",
  "totalAmount": 450.75,
  "estimatedDeliveryTime": "2025-11-11T19:30:00Z",
  "vendor": {
    "vendorId": "123",
    "vendorName": "Tea House",
    "branchId": 456
  },
  "items": [...],
  "pricing": {...},
  "deliveryAddress": {...},
  "createdAt": "2025-11-11T18:45:00Z"
}
```

---

## Two-Step Checkout Flow

### Step 1: Checkout Calculation (`POST /api/v1/checkout/calculate`)

**Purpose:** Validate cart, calculate pricing, create temporary session

**Actions:**
1. Validate vendor and menu items
2. Calculate complete pricing breakdown
3. Apply discounts/coupons
4. Calculate delivery fees
5. Create Redis session (15-min TTL)
6. Return session ID

**Output:** `checkoutSessionId` in `READY_FOR_COMMIT` state

### Step 2: Order Commitment (`POST /api/v1/orders`) ← **THIS API**

**Purpose:** Execute payment and create persistent order

**Actions:**
1. Lock checkout session
2. Final validation
3. Execute payment
4. Create order record
5. Publish events
6. Mark session as committed

**Output:** Order ID and order details

---

## Detailed Business Logic

### 6-Step Atomic Process

All steps must execute atomically. Failure at any step triggers rollback and appropriate error response.

#### Step 1: Session Lock & Retrieval

**Objective:** Acquire exclusive lock on checkout session to prevent duplicate orders

**Actions:**
1. Retrieve checkout session from Redis using `checkoutSessionId`
2. Validate session exists
3. Check session status is `READY_FOR_COMMIT`
4. Update status to `IN_PROGRESS` (atomic operation)
5. Verify session is not stale (< 5 minutes old)

**Success Conditions:**
- Session found in Redis
- Status is `READY_FOR_COMMIT`
- Session age < 5 minutes
- Status successfully updated to `IN_PROGRESS`

**Failure Scenarios:**
| Scenario | HTTP Status | Error Code |
|----------|-------------|------------|
| Session not found | 404 | `ERR_SESSION_NOT_FOUND` |
| Session expired | 409 | `ERR_SESSION_EXPIRED` |
| Session already in progress | 409 | `ERR_SESSION_ALREADY_IN_PROGRESS` |
| Session already committed | 409 | `ERR_SESSION_ALREADY_COMMITTED` |
| Session too stale | 409 | `ERR_SESSION_STALE` |

**Implementation:**
```java
// Atomic Redis operation
CheckoutSession session = sessionManagementService.lockSession(checkoutSessionId);
if (session == null) {
    throw new SessionNotFoundException();
}
if (session.getStatus() != CheckoutStatus.READY_FOR_COMMIT) {
    throw new SessionAlreadyProcessedException();
}
if (session.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(5))) {
    throw new SessionStaleException();
}
```

---

#### Step 2: Final Validation

**Objective:** Perform last-second checks before payment execution

**Actions:**
1. **Vendor Status Check**
   - Verify vendor branch is still active
   - Check vendor is accepting orders
   - Verify operating hours (if applicable)

2. **Menu Item Availability**
   - Revalidate all items are still available
   - Check stock levels (if tracked)

3. **Pricing Integrity**
   - Verify prices haven't changed significantly
   - Validate total amount matches session

4. **Delivery Zone**
   - Confirm delivery address is still serviceable
   - Verify delivery zone hasn't changed

**Success Conditions:**
- Vendor is active and accepting orders
- All items available
- Pricing unchanged or within tolerance
- Delivery zone serviceable

**Failure Scenarios:**
| Scenario | HTTP Status | Error Code |
|----------|-------------|------------|
| Vendor closed | 409 | `ERR_VENDOR_CLOSED` |
| Vendor not accepting orders | 409 | `ERR_VENDOR_NOT_ACCEPTING` |
| Item unavailable | 409 | `ERR_ITEM_UNAVAILABLE` |
| Price changed | 409 | `ERR_PRICE_CHANGED` |
| Delivery zone not serviceable | 409 | `ERR_DELIVERY_ZONE_INVALID` |

**Implementation:**
```java
// Vendor validation
VendorBranch vendor = vendorBranchRepository.findById(session.getVendorBranchId())
    .orElseThrow(() -> new VendorNotFoundException());
    
if (!vendor.getIsActive() || !vendor.isAcceptingOrders()) {
    throw new VendorNotAcceptingOrdersException();
}

// Menu item validation
for (CartItem item : session.getItems()) {
    MenuItem menuItem = menuService.getMenuItem(item.getMenuItemId());
    if (!menuItem.getIsAvailable()) {
        throw new ItemUnavailableException(menuItem.getName());
    }
}
```

---

#### Step 3: Execute Payment

**Objective:** Process payment based on payment method

**Payment Methods:**

##### A. Wallet Payment

**Flow:**
1. Call `PaymentService.deductBalance(userId, amount, orderId)`
2. Verify sufficient balance
3. Deduct amount atomically
4. Generate transaction ID
5. Update wallet balance

**Success:** Payment transaction ID returned

**Failure:**
- Insufficient funds → 402 Payment Required (`ERR_INSUFFICIENT_FUNDS`)
- Wallet service unavailable → 503 Service Unavailable

**Implementation:**
```java
PaymentTransaction transaction = paymentService.deductBalance(
    session.getUserId(),
    session.getPricing().getTotalAmount(),
    "ORDER_PAYMENT"
);
paymentTransactionId = transaction.getTransactionId();
paymentStatus = PaymentStatus.PAID;
```

##### B. GPay Payment

**Flow:**
1. Validate `paymentToken` is provided
2. Call `PaymentService.processGpayTransaction(paymentToken, amount, orderId)`
3. Forward to payment gateway
4. Wait for gateway response
5. Handle success/failure

**Success:** Payment transaction ID from gateway

**Failure:**
- Invalid token → 402 Payment Required (`ERR_INVALID_PAYMENT_TOKEN`)
- Gateway failure → 402 Payment Required (`ERR_PAYMENT_GATEWAY_FAILURE`)
- Timeout → 408 Request Timeout

**Implementation:**
```java
if (paymentToken == null || paymentToken.isBlank()) {
    throw new PaymentTokenRequiredException();
}

PaymentTransaction transaction = paymentService.processGpayTransaction(
    paymentToken,
    session.getPricing().getTotalAmount(),
    "ORDER_PAYMENT"
);
paymentTransactionId = transaction.getTransactionId();
paymentStatus = PaymentStatus.PAID;
```

##### C. Cash on Delivery (COD)

**Flow:**
1. Call `PaymentService.registerCodTransaction(userId, amount, orderId)`
2. Register pending payment
3. Generate transaction ID
4. Set payment status as PENDING

**Success:** Always succeeds (registers PENDING payment)

**Failure:** None (COD registration always succeeds)

**Implementation:**
```java
PaymentTransaction transaction = paymentService.registerCodTransaction(
    session.getUserId(),
    session.getPricing().getTotalAmount(),
    "ORDER_PAYMENT"
);
paymentTransactionId = transaction.getTransactionId();
paymentStatus = PaymentStatus.PENDING;  // Will be paid on delivery
```

**Payment Status Mapping:**

| Payment Method | Success Status | Failure Status |
|----------------|----------------|----------------|
| Wallet | `PAID` | `FAILED` |
| GPay | `PAID` | `FAILED` |
| COD | `PENDING` | N/A (always succeeds) |

---

#### Step 4: Order Creation

**Objective:** Create persistent order record in database

**Actions:**
1. Map checkout session to Order entity
2. Set initial state to `CREATED`
3. Set payment status from Step 3
4. Store payment transaction ID
5. Generate order number
6. Save to database
7. Trigger FSM transition to `VALIDATED`
8. Create audit record

**Order Entity Mapping:**

```java
Order order = Order.builder()
    .customerId(session.getUserId())
    .vendorId(vendor.getVendor().getVendorId())
    .vendorBranchId(session.getVendorBranchId())
    .state(OrderState.CREATED)
    .orderType(OrderType.SINGLE)
    .paymentStatus(paymentStatus)
    .paymentTransactionId(paymentTransactionId)
    .paymentMethod(session.getPaymentMethod())
    .deliveryAddress(session.getDeliveryAddress())
    .deliveryLatitude(session.getDeliveryLocation().getLatitude())
    .deliveryLongitude(session.getDeliveryLocation().getLongitude())
    .itemTotal(session.getPricing().getItemTotal())
    .deliveryCharges(session.getPricing().getDeliveryCharges())
    .platformFee(session.getPricing().getPlatformFee())
    .gst(session.getPricing().getGst())
    .discount(session.getPricing().getDiscount())
    .totalAmount(session.getPricing().getTotalAmount())
    .specialInstructions(session.getDeliveryInstructions())
    .contactlessDelivery(session.getContactlessDelivery())
    .leaveAtDoor(session.getLeaveAtDoor())
    .metadata(buildMetadata(session))
    .createdAt(LocalDateTime.now())
    .build();

// Add order items
session.getItems().forEach(cartItem -> {
    OrderItem item = OrderItem.builder()
        .menuItemId(cartItem.getMenuItemId())
        .quantity(cartItem.getQuantity())
        .priceAtOrder(cartItem.getUnitPrice())
        .customizations(cartItem.getCustomizations())
        .notes(cartItem.getSpecialInstructions())
        .build();
    order.addOrderItem(item);
});

// Save order
Order savedOrder = orderRepository.save(order);

// Trigger FSM validation
orderFSM.validateOrder(savedOrder);
```

**FSM Integration:**

After order creation, trigger FSM transitions:
1. `CREATED` → `VALIDATED` (automatic)
2. `VALIDATED` → `PAYMENT_CONFIRMED` (if payment successful)
3. `PAYMENT_CONFIRMED` → `PENDING_ACCEPTANCE` (notify restaurant)

**Success Conditions:**
- Order saved successfully
- FSM transitions executed
- Audit records created

**Failure Scenarios:**
| Scenario | HTTP Status | Error Code |
|----------|-------------|------------|
| Database error | 500 | `ERR_ORDER_CREATION_FAILED` |
| FSM transition error | 500 | `ERR_FSM_TRANSITION_FAILED` |

---

#### Step 5: Session Cleanup

**Objective:** Mark session as completed and schedule deletion

**Actions:**
1. Update session status to `COMMITTED`
2. Store order ID in session
3. Reduce TTL to 5 minutes (for reference)
4. Log completion

**Implementation:**
```java
sessionManagementService.updateSessionStatus(
    checkoutSessionId,
    CheckoutStatus.COMMITTED,
    savedOrder.getOrderId()
);
```

**Success:** Session marked as committed

**Failure:** Log warning (non-critical, order already created)

---

#### Step 6: Event Publishing

**Objective:** Publish events for downstream processing

**Events to Publish:**

##### 1. OrderPlacedEvent

**Purpose:** Notify vendor to start preparing

**Payload:**
```json
{
  "eventType": "ORDER_PLACED",
  "orderId": "uuid",
  "vendorId": "123",
  "vendorBranchId": 456,
  "customerId": "uuid",
  "totalAmount": 450.75,
  "items": [...],
  "estimatedPrepTime": 25,
  "timestamp": "2025-11-11T18:45:00Z"
}
```

**Destination:** `order-events` topic

##### 2. PaymentCompletedEvent

**Purpose:** Notify payment service of successful payment

**Payload:**
```json
{
  "eventType": "PAYMENT_COMPLETED",
  "orderId": "uuid",
  "transactionId": "txn_123",
  "amount": 450.75,
  "paymentMethod": "WALLET",
  "status": "PAID",
  "timestamp": "2025-11-11T18:45:00Z"
}
```

**Destination:** `payment-events` topic

##### 3. OrderStateChangedEvent

**Purpose:** Track order state changes

**Payload:**
```json
{
  "eventType": "ORDER_STATE_CHANGED",
  "orderId": "uuid",
  "previousState": "CREATED",
  "newState": "PENDING_ACCEPTANCE",
  "timestamp": "2025-11-11T18:45:00Z"
}
```

**Destination:** `order-events` topic

**Implementation:**
```java
// Publish OrderPlacedEvent
eventPublisher.publishOrderPlacedEvent(savedOrder);

// Publish PaymentCompletedEvent
eventPublisher.publishPaymentCompletedEvent(
    savedOrder.getOrderId(),
    paymentTransactionId,
    paymentStatus
);

// Publish OrderStateChangedEvent
eventPublisher.publishOrderStateChangedEvent(
    savedOrder.getOrderId(),
    OrderState.CREATED,
    OrderState.PENDING_ACCEPTANCE
);
```

**Success:** Events published to Kafka

**Failure:** Log error (non-critical, order already created)

---

## Payment Integration

### Payment Service Interface

```java
public interface PaymentService {
    
    /**
     * Deduct balance from wallet
     * @throws InsufficientFundsException if balance < amount
     * @throws PaymentServiceException if service unavailable
     */
    PaymentTransaction deductBalance(
        UUID userId,
        BigDecimal amount,
        String purpose
    );
    
    /**
     * Process GPay transaction
     * @throws InvalidPaymentTokenException if token invalid
     * @throws PaymentGatewayException if gateway fails
     */
    PaymentTransaction processGpayTransaction(
        String paymentToken,
        BigDecimal amount,
        String purpose
    );
    
    /**
     * Register COD transaction (always succeeds)
     */
    PaymentTransaction registerCodTransaction(
        UUID userId,
        BigDecimal amount,
        String purpose
    );
}
```

### Payment Transaction Model

```java
@Data
@Builder
public class PaymentTransaction {
    private String transactionId;
    private UUID userId;
    private BigDecimal amount;
    private String paymentMethod;
    private PaymentStatus status;
    private String gatewayResponse;
    private LocalDateTime createdAt;
}
```

---

## Error Handling

### Error Response Format

```json
{
  "error": {
    "code": "ERR_SESSION_NOT_FOUND",
    "message": "Checkout session not found or expired",
    "field": "checkoutSessionId",
    "timestamp": "2025-11-11T18:45:00Z",
    "traceId": "abc123"
  }
}
```

### Error Codes and HTTP Status

| HTTP Status | Error Code | Description | Rollback Action |
|-------------|------------|-------------|-----------------|
| 400 | `ERR_INVALID_INPUT` | Missing/invalid parameters | None |
| 402 | `ERR_INSUFFICIENT_FUNDS` | Wallet balance too low | Release session lock |
| 402 | `ERR_PAYMENT_GATEWAY_FAILURE` | GPay gateway failed | Release session lock |
| 402 | `ERR_INVALID_PAYMENT_TOKEN` | GPay token invalid | Release session lock |
| 404 | `ERR_SESSION_NOT_FOUND` | Session doesn't exist | None |
| 408 | `ERR_PAYMENT_TIMEOUT` | Payment gateway timeout | Release session lock |
| 409 | `ERR_SESSION_ALREADY_COMMITTED` | Session already processed | None |
| 409 | `ERR_SESSION_ALREADY_IN_PROGRESS` | Another request processing | None |
| 409 | `ERR_SESSION_EXPIRED` | Session TTL expired | None |
| 409 | `ERR_SESSION_STALE` | Session > 5 minutes old | Release session lock |
| 409 | `ERR_VENDOR_CLOSED` | Vendor not accepting orders | Release session lock |
| 409 | `ERR_ITEM_UNAVAILABLE` | Menu item not available | Release session lock |
| 409 | `ERR_PRICE_CHANGED` | Price changed since checkout | Release session lock |
| 500 | `ERR_ORDER_CREATION_FAILED` | Database error | Rollback payment |
| 503 | `ERR_PAYMENT_SERVICE_UNAVAILABLE` | Payment service down | Release session lock |
| 503 | `ERR_ORDER_SERVICE_UNAVAILABLE` | Order service down | Rollback payment |

### Rollback Strategy

| Failure Point | Rollback Actions |
|---------------|------------------|
| Step 1 (Lock) | Return error, no rollback needed |
| Step 2 (Validation) | Release session lock |
| Step 3 (Payment) | Release session lock, log payment attempt |
| Step 4 (Order Creation) | **Rollback payment**, release session lock |
| Step 5 (Session Cleanup) | Log warning, continue (order created) |
| Step 6 (Events) | Log error, continue (order created) |

**Critical:** If order creation fails after successful payment, must rollback payment or mark for manual reconciliation.

---

## Implementation Requirements

### 1. Transactional Boundaries

```java
@Transactional
public Order createOrderFromCheckout(CreateOrderFromCheckoutRequest request) {
    // Steps 1-4 must be in same transaction
    // Steps 5-6 can be async
}
```

### 2. Idempotency

**Mechanism:** Check if session already committed before processing

```java
CheckoutSession session = sessionManagementService.getSession(checkoutSessionId);
if (session.getStatus() == CheckoutStatus.COMMITTED) {
    // Return existing order
    return orderRepository.findById(session.getOrderId())
        .orElseThrow(() -> new OrderNotFoundException());
}
```

### 3. Timeout Handling

**Session Lock Timeout:** 30 seconds  
**Payment Timeout:** 15 seconds  
**Database Timeout:** 10 seconds

### 4. Retry Strategy

**Payment Failures:** No automatic retry (return error to client)  
**Event Publishing:** Retry 3 times with exponential backoff  
**Database Operations:** Retry 2 times for transient errors

### 5. Logging Requirements

**Log Levels:**
- INFO: Session locked, payment initiated, order created
- WARN: Validation failures, session expired
- ERROR: Payment failures, database errors, rollback triggered

**Required Fields:**
- `checkoutSessionId`
- `orderId` (after creation)
- `userId`
- `paymentTransactionId`
- `amount`
- `timestamp`

### 6. Monitoring Metrics

**Key Metrics:**
- Order creation success rate
- Payment success rate by method
- Average order creation time
- Session lock contention
- Rollback frequency

### 7. Audit Trail

**Required Audit Records:**
1. Session locked
2. Validation completed
3. Payment initiated
4. Payment completed/failed
5. Order created
6. FSM state transitions
7. Events published

---

## Implementation Checklist

- [ ] Create `CreateOrderFromCheckoutRequest` DTO
- [ ] Implement `SessionManagementService.lockSession()`
- [ ] Implement vendor validation
- [ ] Implement menu item validation
- [ ] Create `PaymentService` interface
- [ ] Implement wallet payment
- [ ] Implement GPay payment
- [ ] Implement COD payment
- [ ] Implement order entity mapping
- [ ] Integrate with OrderFSM
- [ ] Implement session cleanup
- [ ] Implement event publishing
- [ ] Implement error handling
- [ ] Implement rollback logic
- [ ] Add comprehensive logging
- [ ] Add metrics collection
- [ ] Write unit tests
- [ ] Write integration tests
- [ ] Add API documentation

---

## Testing Requirements

### Unit Tests

1. Session locking (success/failure scenarios)
2. Validation logic (all failure cases)
3. Payment execution (all methods, success/failure)
4. Order entity mapping
5. FSM integration
6. Event publishing
7. Error handling
8. Rollback logic

### Integration Tests

1. End-to-end order creation (all payment methods)
2. Concurrent session access
3. Payment service failures
4. Database failures
5. Event publishing failures
6. Idempotency verification

### Load Tests

1. Concurrent order creation (100 TPS)
2. Session lock contention
3. Payment service load
4. Database connection pool

---

## Success Criteria

1. ✅ Order creation success rate > 99.9%
2. ✅ Payment success rate > 99.5%
3. ✅ Average order creation time < 2 seconds
4. ✅ Zero duplicate orders
5. ✅ 100% audit trail coverage
6. ✅ Proper rollback on all failures
7. ✅ All events published successfully

---

## References

- [Order FSM Design](02_ORDER_FSM_DESIGN.md)
- [Checkout API Design](07_CHECKOUT_API_DESIGN.md)
- [Payment Integration](payment.md)
- [Event Schema Documentation](../events/order-events-schema.md)
