# Schema Migration Summary - Drop and Recreate Approach

**Date:** November 9, 2025  
**Story:** BE-003-16  
**Approach:** ✅ Drop and Recreate (Clean Slate)

---

## 🎯 Decision

Since the existing `orders` and `order_items` tables are **NOT in use**, we will:
- **DROP** both tables completely
- **CREATE** new FSM-ready schema from scratch

This is simpler, cleaner, and faster than incremental migration.

---

## 📊 What Changes

### **Tables Being Dropped:**
1. `order_items` (V6__Create_order_items_table.sql)
2. `orders` (V5__Create_orders_table.sql)

### **New Tables Being Created:**
1. `orders` - FSM-ready with UUID, multi-restaurant support
2. `order_items` - Enhanced with UUID foreign keys
3. `sub_orders` - NEW: Multi-restaurant order support
4. `deliveries` - NEW: Delivery FSM
5. `order_state_audit` - NEW: Order state tracking
6. `delivery_state_audit` - NEW: Delivery state tracking

---

## 🔑 Key Schema Changes

| Feature | Old Schema | New Schema |
|---------|------------|------------|
| **Primary Key** | `order_id` BIGSERIAL | `order_id` UUID |
| **State Column** | `order_status` VARCHAR(50) | `state` VARCHAR(32) |
| **Pricing** | Single `total_amount` | Breakdown: `item_total`, `delivery_charges`, `platform_fee`, `gst`, `discount` |
| **Multi-Restaurant** | Not supported | `order_type`, `parent_order_id`, `sub_orders` table |
| **Timestamps** | Basic (`created_at`, `updated_at`) | FSM-specific (`validated_at`, `accepted_at`, `preparing_started_at`, etc.) |
| **Delivery** | `delivery_partner_id` only | Complete `deliveries` table with FSM |
| **Audit Trail** | None | Dedicated audit tables |

---

## 📝 Migration Script

**File:** `V7__drop_and_recreate_orders_for_fsm.sql`

**Structure:**
```sql
-- 1. DROP existing tables
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;

-- 2. CREATE new orders table (FSM-ready)
CREATE TABLE orders (...);

-- 3. CREATE new order_items table
CREATE TABLE order_items (...);

-- 4. CREATE sub_orders table
CREATE TABLE sub_orders (...);

-- 5. CREATE deliveries table
CREATE TABLE deliveries (...);

-- 6. CREATE audit tables
CREATE TABLE order_state_audit (...);
CREATE TABLE delivery_state_audit (...);

-- 7. CREATE all indexes
CREATE INDEX ...;

-- 8. CREATE triggers
CREATE TRIGGER ...;
```

---

## ✅ Benefits of This Approach

1. **Simplicity**
   - Single migration script
   - No complex data migration logic
   - No backward compatibility concerns

2. **Clean Schema**
   - No deprecated columns
   - No legacy code
   - UUID from day 1

3. **Faster Implementation**
   - No gradual transition needed
   - Immediate FSM implementation
   - No future cleanup required

4. **Better Design**
   - Optimized for FSM from start
   - Proper multi-restaurant support
   - Complete audit trail

---

## 🔄 Rollback Plan

If migration fails or needs to be rolled back:

```sql
-- Rollback: Restore old schema
-- 1. Drop new tables
DROP TABLE IF EXISTS delivery_state_audit CASCADE;
DROP TABLE IF EXISTS order_state_audit CASCADE;
DROP TABLE IF EXISTS deliveries CASCADE;
DROP TABLE IF EXISTS sub_orders CASCADE;
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;

-- 2. Re-run old migrations
-- Run V5__Create_orders_table.sql
-- Run V6__Create_order_items_table.sql
```

---

## 📋 Implementation Checklist

### **Pre-Migration**
- [x] Confirmed tables are NOT in use
- [ ] Reviewed migration script
- [ ] Tested on development environment
- [ ] Documented rollback procedure

### **Migration**
- [ ] Run V7__drop_and_recreate_orders_for_fsm.sql
- [ ] Verify old tables dropped
- [ ] Verify new tables created
- [ ] Verify constraints and indexes
- [ ] Test schema structure

### **Post-Migration**
- [ ] Create new Order entity
- [ ] Create SubOrder entity
- [ ] Create Delivery entity
- [ ] Create OrderStateAudit entity
- [ ] Create DeliveryStateAudit entity
- [ ] Create repository interfaces
- [ ] Test CRUD operations
- [ ] Begin FSM implementation

---

## 🎯 Success Criteria

✅ Old `orders` table dropped  
✅ Old `order_items` table dropped  
✅ New `orders` table created with FSM support  
✅ New `order_items` table created with UUID keys  
✅ `sub_orders` table created  
✅ `deliveries` table created  
✅ Audit tables created  
✅ All indexes created  
✅ All constraints working  
✅ UUID generation working  
✅ Ready for FSM implementation  

---

## 📚 Related Documents

- [BE-003-16: PostgreSQL Schema for FSM](./BE-003-16-postgresql-fsm-schema-v2.md)
- [Migration Strategy](./MIGRATION_STRATEGY.md)
- [Order FSM Design](../../business-flows/02_ORDER_FSM_DESIGN.md)
- [Delivery FSM Design](../../business-flows/03_DELIVERY_FSM_DESIGN.md)

---

## 🚀 Next Steps

1. ✅ Review and approve migration approach
2. ⏳ Test migration on development database
3. ⏳ Create JPA entities for new schema
4. ⏳ Implement Order FSM (BE-003-18)
5. ⏳ Implement Delivery FSM (BE-003-22)
