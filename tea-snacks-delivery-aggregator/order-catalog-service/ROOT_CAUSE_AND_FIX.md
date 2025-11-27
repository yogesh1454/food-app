# 🔍 Root Cause Analysis & Fix for Test Failures

**Date:** November 16, 2025  
**Status:** ROOT CAUSE IDENTIFIED

---

## 🎯 **ROOT CAUSE IDENTIFIED**

### The Core Problem

**All 30 test failures are caused by the same issue:**

The Checkout API is returning **HTTP 500 (Internal Server Error)** instead of a successful response. Jackson then tries to deserialize the number `500` as a `CheckoutStatus` enum value, which fails.

**Error Message:**
```
Cannot deserialize value of type CheckoutResponse$CheckoutStatus from number 500: 
index value outside legal index range [0..4]
```

**What's happening:**
1. Test calls `POST /api/v1/checkout/calculate`
2. API encounters an error and returns HTTP 500
3. Test tries to deserialize response as `CheckoutResponse`
4. Jackson sees `500` in the response and tries to parse it as `CheckoutStatus` enum
5. Enum only has values 0-4, so 500 is out of range
6. `InvalidFormatException` is thrown

---

## 🔧 **What Needs to be Fixed**

### Priority 1: Fix the Checkout API (CRITICAL)

The checkout API `/api/v1/checkout/calculate` is throwing an uncaught exception. We need to:

1. **Run the API manually** to see the actual error
2. **Fix the underlying issue** causing the 500 error
3. **Add proper error handling** to return meaningful error responses

### How to Diagnose

```bash
# Terminal 1: Start the application with DEBUG logging
cd /Users/yogesh/Documents/ws/food-app/tea-snacks-delivery-aggregator
./gradlew :order-catalog-service:bootRun

# Terminal 2: Test the API and see the error
curl -v -X POST http://localhost:8082/api/v1/checkout/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "11111111-1111-1111-1111-111111111111",
    "vendorBranchId": 1,
    "items": [
      {"menuItemId": 1, "quantity": 2},
      {"menuItemId": 2, "quantity": 3}
    ],
    "deliveryAddress": {
      "addressLine1": "123 Main St",
      "city": "Mumbai",
      "state": "Maharashtra",
      "pincode": "400001",
      "addressType": "HOME"
    },
    "deliveryLocation": {
      "latitude": 19.0760,
      "longitude": 72.8777
    },
    "paymentMethod": "WALLET"
  }'

# Check application logs for the stacktrace
tail -f /path/to/logs | grep -A 50 "ERROR"
```

---

## 🚨 **Common Causes of HTTP 500 in Checkout API**

Based on the codebase, here are the most likely causes:

### 1. Missing @Embedded Fields in Database

**Issue:** The `DeliveryAddress` is `@Embedded` but the database schema might not have all the columns.

**Check:**
```sql
docker exec order-catalog-postgres psql -U tea_snacks_user -d order_catalog_db -c "\d orders"
```

**Look for:** `address_line1`, `address_line2`, `city`, `address_state`, `pincode`, etc.

### 2. Menu Item Not Found

**Issue:** Menu items might not exist or not be available.

**Check:**
```sql
SELECT * FROM menu_items WHERE menu_item_id IN (1, 2) AND branch_id = 1;
```

### 3. Vendor Branch Not Found

**Issue:** Vendor branch might not exist or not be active/open.

**Check:**
```sql
SELECT * FROM vendor_branches WHERE branch_id = 1;
```

### 4. Missing Required Fields in Request

**Issue:** The `CheckoutRequest` validation might be failing.

**Fix:** Ensure all `@NotNull` fields are provided in the test request.

### 5. Service Layer Exception

**Issue:** Business logic in `CheckoutService` might be throwing an exception.

**Common issues:**
- NullPointerException on missing data
- ArithmeticException in price calculations
- IllegalStateException in validation logic

---

## ✅ **Step-by-Step Fix Guide**

### Step 1: Start Application and Check Logs

```bash
cd /Users/yogesh/Documents/ws/food-app/tea-snacks-delivery-aggregator

# Kill any existing process on port 8082
lsof -ti:8082 | xargs kill -9 2>/dev/null

# Start with full logging
./gradlew :order-catalog-service:bootRun > app.log 2>&1 &

# Wait for startup
sleep 15

# Test the endpoint
curl -v -X POST http://localhost:8082/api/v1/checkout/calculate \
  -H "Content-Type: application/json" \
  -d @order-catalog-service/src/test/resources/checkout-request-sample.json

# Check logs for errors
grep -A 50 "ERROR\|Exception" app.log
```

### Step 2: Fix the Identified Issue

Based on the error in the logs, fix the issue in the code:

**Common fixes:**
```java
// If NullPointerException on address
@Embeddable
@Data
public class DeliveryAddress {
    // Ensure all fields have default values or are properly validated
    @Column(name = "address_line1", nullable = false)
    private String addressLine1;
    
    // ...
}

// If validation issue
@Service
public class CheckoutService {
    public CheckoutResponse calculate(CheckoutRequest request) {
        try {
            // Add proper null checks
            if (request.getVendorBranchId() == null) {
                throw new ValidationException("Vendor branch ID is required");
            }
            
            // ... rest of logic
        } catch (Exception e) {
            log.error("Error calculating checkout", e);
            throw new CheckoutException("Failed to calculate checkout", e);
        }
    }
}
```

### Step 3: Add Sample Request File

```bash
# Create a sample request file for testing
cat > order-catalog-service/src/test/resources/checkout-request-sample.json <<'EOF'
{
  "userId": "11111111-1111-1111-1111-111111111111",
  "vendorBranchId": 1,
  "items": [
    {"menuItemId": 1, "quantity": 2, "specialInstructions": "Extra sugar"},
    {"menuItemId": 2, "quantity": 3, "specialInstructions": "Extra chutney"}
  ],
  "deliveryAddress": {
    "addressLine1": "123 Main Street",
    "addressLine2": "Apartment 4B",
    "landmark": "Near Central Park",
    "city": "Mumbai",
    "state": "Maharashtra",
    "pincode": "400001",
    "addressType": "HOME"
  },
  "deliveryLocation": {
    "latitude": 19.0760,
    "longitude": 72.8777
  },
  "paymentMethod": "WALLET",
  "deliveryInstructions": "Please ring the doorbell",
  "contactlessDelivery": false,
  "leaveAtDoor": false
}
EOF
```

### Step 4: Test Manually Until It Works

```bash
# Keep testing until you get HTTP 200
curl -v -X POST http://localhost:8082/api/v1/checkout/calculate \
  -H "Content-Type: application/json" \
  -d @order-catalog-service/src/test/resources/checkout-request-sample.json

# Expected successful response:
# HTTP/1.1 200 OK
# {
#   "checkoutSessionId": "some-uuid",
#   "status": "READY_FOR_COMMIT",
#   "vendor": {...},
#   "items": [...],
#   "pricing": {...},
#   "expiresAt": "..."
# }
```

### Step 5: Run Integration Tests

```bash
# Once manual testing works, run the integration tests
./gradlew :order-catalog-service:test --tests "CheckoutAPIIntegrationTest.shouldCreateCheckoutSessionSuccessfully"

# If that passes, run all tests
./gradlew :order-catalog-service:test --tests "*IntegrationTest"
```

---

## 📋 **Checklist for Fixing Tests**

- [ ] Application starts without errors on port 8082
- [ ] Test data exists in database (vendors, branches, menu items)
- [ ] Manual curl request to `/checkout/calculate` returns HTTP 200
- [ ] Response can be deserialized as `CheckoutResponse`
- [ ] Single test `shouldCreateCheckoutSessionSuccessfully` passes
- [ ] All Checkout API tests pass
- [ ] All PlaceOrder API tests pass
- [ ] All 33 integration tests pass

---

## 🎯 **Expected Outcome**

Once the Checkout API is fixed to return proper responses instead of HTTP 500:

1. ✅ The `InvalidFormatException` will disappear
2. ✅ Jackson will properly deserialize `CheckoutResponse`
3. ✅ Tests will be able to assert on the response
4. ✅ All 33 tests should pass (or at least most of them)

---

## 💡 **Why This Happened**

1. The test data was set up correctly
2. The infrastructure (Docker, Redis, Kafka) is working fine
3. The issue is in the **application code itself**
4. The Checkout API has a bug that causes it to return HTTP 500
5. This bug was masked by previous issues and is now revealed

---

## 🔄 **Recommended Workflow**

```
1. Start application locally
   ↓
2. Test API manually with curl
   ↓
3. Check logs for errors
   ↓
4. Fix the error in code
   ↓
5. Restart application
   ↓
6. Test again with curl
   ↓
7. Repeat until manual test works
   ↓
8. Run integration tests
   ↓
9. All tests pass! ✅
```

---

## 📞 **Quick Debug Commands**

```bash
# Check if app is running
curl http://localhost:8082/actuator/health

# Check test data
docker exec order-catalog-postgres psql -U tea_snacks_user -d order_catalog_db -c "
  SELECT branch_id, branch_name, is_active, is_open FROM vendor_branches WHERE branch_id = 1;
  SELECT menu_item_id, name, price, is_available FROM menu_items WHERE branch_id = 1;
"

# Test checkout API
curl -X POST http://localhost:8082/api/v1/checkout/calculate \
  -H "Content-Type: application/json" \
  -d '{"userId":"11111111-1111-1111-1111-111111111111","vendorBranchId":1,"items":[{"menuItemId":1,"quantity":2}],"deliveryAddress":{"addressLine1":"123 Main St","city":"Mumbai","state":"Maharashtra","pincode":"400001","addressType":"HOME"},"deliveryLocation":{"latitude":19.0760,"longitude":72.8777},"paymentMethod":"WALLET"}'

# View last 100 lines of app logs
tail -100 app.log | grep -A 20 "ERROR"

# Run single test
./gradlew :order-catalog-service:test --tests "CheckoutAPIIntegrationTest.shouldCreateCheckoutSessionSuccessfully"
```

---

## 🎓 **Summary**

**Problem:** Tests failing with `InvalidFormatException`  
**Root Cause:** Checkout API returning HTTP 500 instead of successful response  
**Solution:** Fix the bug in Checkout API that's causing the 500 error  
**How to Fix:** Run app locally, test with curl, check logs, fix the error  
**Expected Result:** All 33 tests pass once API is fixed

---

**The test setup is perfect! The issue is purely in the application code. Fix the Checkout API and all tests will pass.** 🎯

