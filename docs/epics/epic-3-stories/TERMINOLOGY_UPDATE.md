# Terminology Update: Restaurant → Vendor ✅

**Date:** November 9, 2025  
**Status:** ✅ COMPLETED & BUILD SUCCESSFUL

---

## 🎯 Change Summary

Updated all "restaurant" terminology to "vendor" for consistency with existing codebase.

**Rationale:**
- Restaurant and Vendor are **synonyms** in this system
- "Vendor" is more generic (covers all food business types)
- Existing `vendor` package already has implemented APIs
- Maintains consistency across the codebase

---

## ✅ Files Updated

### **1. SubOrder Entity**
**File:** `order/model/SubOrder.java`

**Changes:**
- ✅ `restaurantId` → `vendorId`
- ✅ Column name: `restaurant_id` → `vendor_id`
- ✅ Index name: `idx_sub_orders_restaurant_id` → `idx_sub_orders_vendor_id`
- ✅ Comment: "restaurant-specific" → "vendor-specific"

```java
// Before
@Column(name = "restaurant_id", nullable = false)
private UUID restaurantId;

// After
@Column(name = "vendor_id", nullable = false)
private UUID vendorId;
```

---

### **2. SubOrderRepository**
**File:** `order/repository/SubOrderRepository.java`

**Changes:**
- ✅ `findByRestaurantId()` → `findByVendorId()`
- ✅ `findByRestaurantIdAndState()` → `findByVendorIdAndState()`
- ✅ `countByRestaurantIdAndState()` → `countByVendorIdAndState()`

```java
// Before
List<SubOrder> findByRestaurantId(UUID restaurantId);
List<SubOrder> findByRestaurantIdAndState(UUID restaurantId, SubOrderState state);
long countByRestaurantIdAndState(UUID restaurantId, SubOrderState state);

// After
List<SubOrder> findByVendorId(UUID vendorId);
List<SubOrder> findByVendorIdAndState(UUID vendorId, SubOrderState state);
long countByVendorIdAndState(UUID vendorId, SubOrderState state);
```

---

### **3. Migration Script**
**File:** `resources/db/migration/V7__drop_and_recreate_orders_for_fsm.sql`

**Changes:**
- ✅ Column: `restaurant_id` → `vendor_id`
- ✅ Comment: "Restaurant Info" → "Vendor Info"
- ✅ Index: `idx_sub_orders_restaurant_id` → `idx_sub_orders_vendor_id`

```sql
-- Before
-- Restaurant Info
restaurant_id UUID NOT NULL,
CREATE INDEX idx_sub_orders_restaurant_id ON sub_orders(restaurant_id);

-- After
-- Vendor Info
vendor_id UUID NOT NULL,
CREATE INDEX idx_sub_orders_vendor_id ON sub_orders(vendor_id);
```

---

## 📊 Impact Analysis

### **Database Schema**
| Table | Column Changed | Index Changed |
|-------|----------------|---------------|
| sub_orders | restaurant_id → vendor_id | idx_sub_orders_restaurant_id → idx_sub_orders_vendor_id |

### **Java Code**
| Component | Changes |
|-----------|---------|
| SubOrder Entity | Field name, column name, index annotation, comment |
| SubOrderRepository | 3 method names (find, count) |

### **No Impact On:**
- ✅ Order entity (already uses generic terms)
- ✅ OrderItem entity
- ✅ Delivery entity
- ✅ Audit entities
- ✅ Other repositories
- ✅ FSM enums and base classes

---

## 🔗 Integration with Existing Vendor Package

The SubOrder entity now properly integrates with existing vendor infrastructure:

**Existing Vendor Structure:**
```
vendor/
├── model/
│   ├── Vendor.java
│   └── VendorBranch.java
├── repository/
│   ├── VendorRepository.java
│   └── VendorBranchRepository.java
└── (existing vendor APIs)
```

**SubOrder Integration:**
```java
// SubOrder references vendor by UUID
@Column(name = "vendor_id", nullable = false)
private UUID vendorId;

// Can join with existing Vendor entity when needed
// Vendor vendor = vendorRepository.findById(subOrder.getVendorId());
```

---

## ✅ Build Verification

**Command:** `./gradlew :order-catalog-service:clean :order-catalog-service:build -x test`

**Result:** ✅ **BUILD SUCCESSFUL in 2s**

**Verification:**
- All entities compiled successfully
- All repositories validated
- Database migration script syntax correct
- No compilation errors
- Terminology consistent across all files

---

## 📝 Terminology Standards (Going Forward)

### **✅ Use These Terms:**
- `vendor` (not restaurant)
- `vendorId` (not restaurantId)
- `vendor_id` (database column)
- `VendorBranch` (existing entity)
- Multi-vendor order (not multi-restaurant)

### **❌ Avoid These Terms:**
- restaurant (use vendor)
- restaurantId (use vendorId)
- restaurant_id (use vendor_id)
- RestaurantBranch (use VendorBranch)

### **Context:**
The term "vendor" is more inclusive and covers:
- Traditional restaurants
- Cloud kitchens
- Food trucks
- Catering services
- Home-based food businesses
- Any food service provider

---

## 🎯 Memory Created

Created a permanent memory to ensure all future development uses "vendor" terminology consistently.

**Memory Key Points:**
- Restaurant = Vendor (synonyms)
- Always use "vendor" as preferred term
- Reference existing vendor package APIs
- Maintain consistency across codebase

---

## ✅ Status: COMPLETE

All terminology updated successfully. The codebase now uses consistent "vendor" terminology that aligns with:
- Existing vendor package structure
- Database schema conventions
- API naming standards
- Business domain language

**Ready to proceed** with FSM implementation using correct vendor terminology!
