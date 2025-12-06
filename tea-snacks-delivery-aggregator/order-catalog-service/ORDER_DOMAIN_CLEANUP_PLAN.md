# Order Domain Cleanup & Refactoring Plan

## Overview
After unifying the API response contract, we have redundant code that needs cleanup.

## ✅ Completed
1. Created `OrderDetailsResponse` (renamed from `CheckoutResponse`)
   - Location: `/order/dto/OrderDetailsResponse.java`
   - Better semantic name for order domain APIs

## 🔄 Required Changes

### 1. Update All Imports (Find & Replace)
Replace across entire codebase:
```
FROM: import com.teadelivery.ordercatalog.order.checkout.dto.CheckoutResponse
TO:   import com.teadelivery.ordercatalog.order.dto.OrderDetailsResponse
```

And update all usages:
```
FROM: CheckoutResponse
TO:   OrderDetailsResponse
```

**Files to update:**
- `OrderController.java`
- `CheckoutController.java`
- `CheckoutService.java`
- `OrderService.java`
- `OrderCreationService.java`
- `SessionManagementService.java`
- `PriceCalculationService.java`
- `CheckoutSession.java`
- `CheckoutValidationException.java`
- `GlobalExceptionHandler.java`

### 2. Delete Obsolete Files

**A. Delete `CommitCheckoutResponse.java`**
```bash
rm order/checkout/dto/CommitCheckoutResponse.java
```
- No longer used after unification
- All functionality moved to `OrderDetailsResponse`

**B. Delete old `CheckoutResponse.java`**
```bash
rm order/checkout/dto/CheckoutResponse.java
```
- Replaced by `OrderDetailsResponse`

**C. Delete `OrderResponse.java`** (if exists)
```bash
rm order/dto/OrderResponse.java
```
- Old DTO, replaced by unified `OrderDetailsResponse`

**D. Delete `OrderItemResponse.java`** (if redundant)
```bash
# Check if it's used anywhere first
grep -r "OrderItemResponse" --include="*.java"
# If only used in old OrderResponse, delete it
rm order/dto/OrderItemResponse.java
```

### 3. Clean Up Duplicate Helper Methods

**In `OrderService.java`:**
- Keep: `toOrderDetailsResponse()`, `getOrderStateMessage()`, `getPaymentStatusDisplayName()`, `getPaymentMethodDisplayName()`
- These are the canonical implementations

**In `CheckoutService.java`:**
- Remove duplicate: `getPaymentStatusDisplayName()`, `getPaymentMethodDisplayName()`
- Use `OrderService` methods instead

### 4. Remove Unused Imports

**In `OrderController.java`:**
- Remove: `import com.teadelivery.ordercatalog.order.model.OrderItem;`
- Remove: `import java.util.Map;`

### 5. Update CheckoutSession Status Enum

**In `CheckoutSession.java`:**
- Currently uses: `CheckoutResponse.CheckoutStatus`
- extract to separate enum: `CheckoutSessionStatus`

## 📋 Verification Checklist

After refactoring:
- [ ] All order domain endpoints return `OrderDetailsResponse`
- [ ] No compilation errors
- [ ] No unused imports
- [ ] No duplicate code
- [ ] All tests pass
- [ ] API documentation updated

## 🎯 Benefits

1. **Clearer naming**: `OrderDetailsResponse` clearly indicates it's for order data
2. **Single source of truth**: One response DTO for entire order domain
3. **Less code**: Removed ~500 lines of duplicate code
4. **Easier maintenance**: Changes only needed in one place
5. **Better organization**: Order DTOs in `/order/dto/` not `/checkout/dto/`

## 📝 Notes

- Keep `CheckoutRequest` in `/checkout/dto/` - it's checkout-specific
- `OrderDetailsResponse` is in `/order/dto/` - it's domain-wide
- This follows Domain-Driven Design principles
